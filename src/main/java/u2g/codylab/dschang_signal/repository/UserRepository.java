package u2g.codylab.dschang_signal.repository;

import u2g.codylab.dschang_signal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
}
