package u2g.codylab.dschang_signal.repository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import u2g.codylab.dschang_signal.entity.*;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportSpecification {

    private ReportSpecification() {}


    public static Specification<Report> adminFilter(
            String moderationStatus,
            String reportStatus,
            String category,
            OffsetDateTime fromDate,
            OffsetDateTime toDate
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (moderationStatus != null && !moderationStatus.isBlank()) {
                predicates.add(cb.equal(
                        root.get("moderationStatus"),
                        ModerationStatus.valueOf(moderationStatus)
                ));
            }

            if (reportStatus != null && !reportStatus.isBlank()) {
                predicates.add(cb.equal(
                        root.get("reportStatus"),
                        ReportStatus.valueOf(reportStatus)
                ));
            }

            if (category != null && !category.isBlank()) {
                Join<Report, Category> categoryJoin = root.join("categories", JoinType.INNER);
                predicates.add(cb.like(
                        cb.lower(categoryJoin.get("name")),
                        "%" + category.toLowerCase() + "%"
                ));
            }

            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        Timestamp.from(fromDate.toInstant())
                ));
            }

            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"),
                        Timestamp.from(toDate.toInstant())
                ));
            }


            if (query != null) query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


    public static Specification<Report> publicFilter(
            String status,
            String category
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();


            predicates.add(cb.equal(
                    root.get("moderationStatus"),
                    ModerationStatus.ACCEPTED
            ));

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(
                        root.get("reportStatus"),
                        ReportStatus.valueOf(status)
                ));
            }

            if (category != null && !category.isBlank()) {
                Join<Report, Category> categoryJoin = root.join("categories", JoinType.INNER);
                predicates.add(cb.like(
                        cb.lower(categoryJoin.get("name")),
                        "%" + category.toLowerCase() + "%"
                ));
            }

            if (query != null) query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Report> myReportsFilter(
            User currentUser,
            String status,
            String category
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();


            predicates.add(cb.equal(root.get("createdBy"), currentUser));

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(
                        root.get("reportStatus"),
                        ReportStatus.valueOf(status)
                ));
            }

            if (category != null && !category.isBlank()) {
                Join<Report, Category> categoryJoin = root.join("categories", JoinType.INNER);
                predicates.add(cb.like(
                        cb.lower(categoryJoin.get("name")),
                        "%" + category.toLowerCase() + "%"
                ));
            }

            if (query != null) query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}