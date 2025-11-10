package com.csy.springbootauthbe.admin.dto;

import com.csy.springbootauthbe.tutor.dto.TutorDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminDashboardDTOTest {

    @Test
    void testBuilderAndFields() {
        TutorDTO tutor = new TutorDTO();
        AdminDashboardDTO.TransactionSummary summary =
            AdminDashboardDTO.TransactionSummary.builder()
                .description("Highest Transaction")
                .amount(500.0)
                .build();

        AdminDashboardDTO.MonthlyEarnings monthly =
            AdminDashboardDTO.MonthlyEarnings.builder()
                .month("January")
                .total(2000.0)
                .build();

        AdminDashboardDTO.TransactionMetrics metrics =
            AdminDashboardDTO.TransactionMetrics.builder()
                .totalEarnings(10000.0)
                .commissionCollected(2000.0)
                .highestTransaction(summary)
                .monthlyEarnings(List.of(monthly))
                .build();

        AdminDashboardDTO dto = AdminDashboardDTO.builder()
            .totalUsers(10)
            .activeUsers(8)
            .suspendedUsers(2)
            .totalTutors(5)
            .activeTutors(3)
            .suspendedTutors(1)
            .unverifiedTutors(1)
            .totalStudents(5)
            .activeStudents(4)
            .suspendedStudents(1)
            .totalAdmins(2)
            .activeAdmins(2)
            .suspendedAdmins(0)
            .pendingTutors(List.of(tutor))
            .transactionMetrics(metrics)
            .build();

        assertEquals(10, dto.getTotalUsers());
        assertEquals(8, dto.getActiveUsers());
        assertEquals(1, dto.getUnverifiedTutors());
        assertEquals(5, dto.getTotalTutors());
        assertEquals(4, dto.getActiveStudents());
        assertEquals(2, dto.getTotalAdmins());
        assertEquals(metrics, dto.getTransactionMetrics());
        assertEquals(1, dto.getPendingTutors().size());
        assertTrue(dto.toString().contains("totalUsers"));
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        AdminDashboardDTO dto = new AdminDashboardDTO();
        dto.setTotalUsers(15);
        dto.setActiveUsers(10);
        dto.setSuspendedUsers(5);
        dto.setTotalTutors(4);
        dto.setActiveTutors(2);
        dto.setSuspendedTutors(1);
        dto.setUnverifiedTutors(1);
        dto.setTotalStudents(20);
        dto.setActiveStudents(18);
        dto.setSuspendedStudents(2);
        dto.setTotalAdmins(3);
        dto.setActiveAdmins(3);
        dto.setSuspendedAdmins(0);

        assertEquals(15, dto.getTotalUsers());
        assertEquals(5, dto.getSuspendedUsers());
        assertEquals(4, dto.getTotalTutors());
        assertEquals(20, dto.getTotalStudents());
        assertEquals(3, dto.getTotalAdmins());
    }

    @Test
    void testTransactionSummaryEquality() {
        AdminDashboardDTO.TransactionSummary s1 =
            new AdminDashboardDTO.TransactionSummary("desc", 500.0);
        AdminDashboardDTO.TransactionSummary s2 =
            new AdminDashboardDTO.TransactionSummary("desc", 500.0);

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
        assertTrue(s1.toString().contains("desc"));
    }

    @Test
    void testMonthlyEarningsEquality() {
        AdminDashboardDTO.MonthlyEarnings m1 =
            new AdminDashboardDTO.MonthlyEarnings("Feb", 1000.0);
        AdminDashboardDTO.MonthlyEarnings m2 =
            new AdminDashboardDTO.MonthlyEarnings("Feb", 1000.0);

        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
        assertTrue(m1.toString().contains("Feb"));
    }

    @Test
    void testTransactionMetricsEquality() {
        AdminDashboardDTO.TransactionSummary summary =
            new AdminDashboardDTO.TransactionSummary("Top", 1000.0);

        AdminDashboardDTO.MonthlyEarnings earning =
            new AdminDashboardDTO.MonthlyEarnings("March", 1200.0);

        AdminDashboardDTO.TransactionMetrics m1 =
            new AdminDashboardDTO.TransactionMetrics(12000.0, 2400.0, summary, List.of(earning));

        AdminDashboardDTO.TransactionMetrics m2 =
            new AdminDashboardDTO.TransactionMetrics(12000.0, 2400.0, summary, List.of(earning));

        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
        assertTrue(m1.toString().contains("totalEarnings"));
    }

    @Test
    void testBuilderChainingForNestedClasses() {
        AdminDashboardDTO.TransactionSummary summary =
            AdminDashboardDTO.TransactionSummary.builder()
                .description("Transaction A")
                .amount(99.9)
                .build();

        AdminDashboardDTO.MonthlyEarnings earning =
            AdminDashboardDTO.MonthlyEarnings.builder()
                .month("April")
                .total(888.8)
                .build();

        AdminDashboardDTO.TransactionMetrics metrics =
            AdminDashboardDTO.TransactionMetrics.builder()
                .totalEarnings(10000.0)
                .commissionCollected(1200.0)
                .highestTransaction(summary)
                .monthlyEarnings(List.of(earning))
                .build();

        assertEquals("Transaction A", metrics.getHighestTransaction().getDescription());
        assertEquals(99.9, metrics.getHighestTransaction().getAmount());
        assertEquals(1, metrics.getMonthlyEarnings().size());
        assertEquals("April", metrics.getMonthlyEarnings().get(0).getMonth());
        assertTrue(metrics.toString().contains("commissionCollected"));
    }
}
