package u2g.codylab.dschang_signal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import u2g.codylab.dschang_signal.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
