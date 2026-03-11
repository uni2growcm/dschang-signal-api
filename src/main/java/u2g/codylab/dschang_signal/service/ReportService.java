package u2g.codylab.dschang_signal.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import u2g.codylab.dschang_signal.dto.CreateReportRequestApiDTO;
import u2g.codylab.dschang_signal.entity.ModerationStatus;
import u2g.codylab.dschang_signal.entity.Report;
import u2g.codylab.dschang_signal.entity.User;
import u2g.codylab.dschang_signal.repository.ReportRepository;
import u2g.codylab.dschang_signal.repository.UserRepository;

import java.sql.Timestamp;


@Slf4j
@Transactional
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public ReportService(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    public Report createReport(CreateReportRequestApiDTO request) {

        // Récupération de l'utilisateur connecté
        User currentUser = getCurrentUser();

        Report report = new Report();
        report.setAuthor(currentUser);  // associe le report à l'utilisateur
        report.setTitle(request.getTitle());
        report.setDescription(request.getDescription());
        report.setCategory(request.getCategory());
        report.setLocationText(request.getLocationText());
        report.setPhotoUrl(request.getPhotoUrl());
        report.setModerationStatus(ModerationStatus.PENDING);
        report.setReportStatus("PENDING");
        report.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        report.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        return reportRepository.save(report);
    }

    // Méthode pour récupérer l'utilisateur connecté
    private User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        String email = authentication.getName(); // ou username selon ton JWT
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : "));
    }

}