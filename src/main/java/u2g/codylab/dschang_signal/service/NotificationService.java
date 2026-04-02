package u2g.codylab.dschang_signal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import u2g.codylab.dschang_signal.entity.Notification;
import u2g.codylab.dschang_signal.entity.Report;
import u2g.codylab.dschang_signal.entity.ReportStatus;
import u2g.codylab.dschang_signal.entity.User;
import u2g.codylab.dschang_signal.exception.ForbiddenException;
import u2g.codylab.dschang_signal.exception.NotFoundException;
import u2g.codylab.dschang_signal.repository.NotificationRepository;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MessageSource messageSource;

    private static final String TYPE_REPORT_STATUS_CHANGED = "REPORT_STATUS_CHANGED";

    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        log.debug("Fetching notifications for user: {}", user.getEmail());
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        log.debug("Getting unread count for user: {}", user.getEmail());
        return notificationRepository.countUnreadByUser(user);
    }

    @Transactional
    public Notification markAsRead(Long notificationId, User currentUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException(
                        getMessage("notification.error.notFound", notificationId)
                ));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException(getMessage("notification.error.forbidden"));
        }

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Transactional
    public int markAllAsRead(User currentUser) {
        log.debug("Marking all notifications as read for user: {}", currentUser.getEmail());
        return notificationRepository.markAllAsReadByUser(currentUser);
    }

    @Transactional
    public void createReportStatusChangedNotification(Report report,
                                                      ReportStatus oldStatus,
                                                      ReportStatus newStatus) {
        log.debug("Creating notification for report {} status change: {} -> {}",
                report.getId(), oldStatus, newStatus);

        Locale locale = LocaleContextHolder.getLocale();

        String title = getMessage("notification.reportStatusChanged.title", locale);
        String message = generateStatusChangeMessage(report, oldStatus, newStatus, locale);

        Notification notification = new Notification();
        notification.setUser(report.getCreatedBy());
        notification.setReportId(report.getId());
        notification.setType(TYPE_REPORT_STATUS_CHANGED);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setOldStatus(oldStatus != null ? oldStatus.name() : null);
        notification.setNewStatus(newStatus != null ? newStatus.name() : null);

        notificationRepository.save(notification);
    }

    private String generateStatusChangeMessage(Report report, ReportStatus oldStatus, ReportStatus newStatus, Locale locale) {
        String oldStatusLabel = getStatusLabel(oldStatus, locale);
        String newStatusLabel = getStatusLabel(newStatus, locale);

        return getMessage("notification.reportStatusChanged.message",
                locale, report.getTitle(), oldStatusLabel, newStatusLabel);
    }

    private String getStatusLabel(ReportStatus status, Locale locale) {
        if (status == null) return getMessage("report.status.unknown", locale);
        return switch (status) {
            case PENDING -> getMessage("report.status.pending", locale);
            case IN_PROGRESS -> getMessage("report.status.inProgress", locale);
            case RESOLVED -> getMessage("report.status.resolved", locale);
        };
    }

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    private String getMessage(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }
}