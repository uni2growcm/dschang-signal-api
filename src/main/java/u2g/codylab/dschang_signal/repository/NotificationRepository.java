package u2g.codylab.dschang_signal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import u2g.codylab.dschang_signal.entity.Notification;
import u2g.codylab.dschang_signal.entity.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.read = false")
    long countUnreadByUser(@Param("user") User user);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user = :user AND n.read = false")
    int markAllAsReadByUser(@Param("user") User user);
}