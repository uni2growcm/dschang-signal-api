package u2g.codylab.dschang_signal.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import u2g.codylab.dschang_signal.dto.NotificationApiDTO;
import u2g.codylab.dschang_signal.entity.Notification;
import u2g.codylab.dschang_signal.entity.User;
import u2g.codylab.dschang_signal.mapper.NotificationMapper;
import u2g.codylab.dschang_signal.service.NotificationService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserNotifications(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/notifications called by user: {}", currentUser.getEmail());

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notifications = notificationService.getUserNotifications(currentUser, pageable);
        Page<NotificationApiDTO> dtos = notifications.map(notificationMapper::toApiDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("content", dtos.getContent());
        response.put("totalElements", dtos.getTotalElements());
        response.put("totalPages", dtos.getTotalPages());
        response.put("number", dtos.getNumber());
        response.put("size", dtos.getSize());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal User currentUser) {

        log.info("GET /api/notifications/unread-count called by user: {}", currentUser.getEmail());

        long count = notificationService.getUnreadCount(currentUser);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationApiDTO> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        log.info("PATCH /api/notifications/{}/read called by user: {}", id, currentUser.getEmail());

        Notification notification = notificationService.markAsRead(id, currentUser);
        return ResponseEntity.ok(notificationMapper.toApiDTO(notification));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(
            @AuthenticationPrincipal User currentUser) {

        log.info("PATCH /api/notifications/read-all called by user: {}", currentUser.getEmail());

        int updatedCount = notificationService.markAllAsRead(currentUser);
        Map<String, Integer> response = new HashMap<>();
        response.put("updatedCount", updatedCount);

        return ResponseEntity.ok(response);
    }
}
