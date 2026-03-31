package u2g.codylab.dschang_signal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

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
                .orElseThrow(() -> new NotFoundException("Notification not found: " + notificationId));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("This notification does not belong to you");
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
    public Notification createReportStatusChangedNotification(Report report,
                                                              ReportStatus oldStatus,
                                                              ReportStatus newStatus) {
        log.debug("Creating notification for report {} status change: {} -> {}",
                report.getId(), oldStatus, newStatus);

        String title = "Report status updated";
        String message = String.format(
                "Your report \"%s\" changed from %s to %s.",
                report.getTitle(),
                oldStatus != null ? oldStatus.name() : "N/A",
                newStatus != null ? newStatus.name() : "N/A"
        );

        Notification notification = new Notification();
        notification.setUser(report.getCreatedBy());
        notification.setReportId(report.getId());
        notification.setType(TYPE_REPORT_STATUS_CHANGED);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setOldStatus(oldStatus != null ? oldStatus.name() : null);
        notification.setNewStatus(newStatus != null ? newStatus.name() : null);

        return notificationRepository.save(notification);
    }
}