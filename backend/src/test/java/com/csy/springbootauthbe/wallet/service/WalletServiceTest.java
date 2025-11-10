package com.csy.springbootauthbe.wallet.service;

import com.csy.springbootauthbe.admin.dto.AdminDashboardDTO;
import com.csy.springbootauthbe.wallet.dto.MonthlyEarningDTO;
import com.csy.springbootauthbe.wallet.entity.Wallet;
import com.csy.springbootauthbe.wallet.entity.WalletTransaction;
import com.csy.springbootauthbe.wallet.repository.WalletRepository;
import com.csy.springbootauthbe.wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceTest {

    @Mock
    private WalletRepository walletRepo;
    @Mock
    private WalletTransactionRepository txnRepo;
    @InjectMocks
    private WalletService walletService;

    private Wallet sampleWallet;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        sampleWallet = new Wallet("id1", "user1", new BigDecimal("100.00"),
                "SGD", null, false, LocalDateTime.now());
    }

    // ----------------------------------------------------------------------
    // getWallet()
    // ----------------------------------------------------------------------
    @Test
    void testGetWallet_existingWallet() {
        when(walletRepo.findByStudentId("user1")).thenReturn(Optional.of(sampleWallet));
        Wallet result = walletService.getWallet("user1");
        assertEquals(sampleWallet, result);
        verify(walletRepo, never()).save(any());
    }

    @Test
    void testGetWallet_createsNewWalletWhenAbsent() {
        when(walletRepo.findByStudentId("user1")).thenReturn(Optional.empty());
        when(walletRepo.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));
        Wallet result = walletService.getWallet("user1");
        assertEquals("user1", result.getStudentId());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        verify(walletRepo).save(any(Wallet.class));
    }

    // ----------------------------------------------------------------------
    // setWalletPin() and verifyWalletPin()
    // ----------------------------------------------------------------------
    @Test
    void testSetAndVerifyWalletPin_success() {
        when(walletRepo.findByStudentId("user1")).thenReturn(Optional.of(sampleWallet));
        when(walletRepo.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));
        walletService.setWalletPin("user1", "1234");
        assertTrue(sampleWallet.getPinSet());
        assertNotNull(sampleWallet.getWalletPin());
        assertTrue(walletService.verifyWalletPin("user1", "1234"));
    }

    @Test
    void testVerifyWalletPin_returnsFalseWhenPinNotSet() {
        sampleWallet.setWalletPin(null);
        when(walletRepo.findByStudentId("user1")).thenReturn(Optional.of(sampleWallet));
        assertFalse(walletService.verifyWalletPin("user1", "any"));
    }

    // ----------------------------------------------------------------------
    // addCredits()
    // ----------------------------------------------------------------------
    @Test
    void testAddCredits_incrementsBalanceAndCreatesTransaction() {
        when(walletRepo.findByStudentId("user1")).thenReturn(Optional.of(sampleWallet));
        walletService.addCredits("user1", new BigDecimal("50.00"), "REF123");
        assertEquals(new BigDecimal("150.00"), sampleWallet.getBalance());
        verify(txnRepo).save(any(WalletTransaction.class));
    }

    // ----------------------------------------------------------------------
    // deductCredits()
    // ----------------------------------------------------------------------
    @Test
    void testDeductCredits_successful() {
        when(walletRepo.findByStudentId("user1")).thenReturn(Optional.of(sampleWallet));
        walletService.deductCredits("user1", new BigDecimal("40.00"), "BKG1");
        assertEquals(new BigDecimal("60.00"), sampleWallet.getBalance());
        verify(txnRepo).save(any(WalletTransaction.class));
    }

    @Test
    void testDeductCredits_insufficientBalanceThrows() {
        sampleWallet.setBalance(new BigDecimal("10.00"));
        when(walletRepo.findByStudentId("user1")).thenReturn(Optional.of(sampleWallet));
        assertThrows(RuntimeException.class,
                () -> walletService.deductCredits("user1", new BigDecimal("50.00"), "BKG1"));
        verify(txnRepo, never()).save(any());
    }

    // ----------------------------------------------------------------------
    // holdCredits()
    // ----------------------------------------------------------------------
    @Test
    void testHoldCredits_success() {
        when(walletRepo.findByStudentId("user1")).thenReturn(Optional.of(sampleWallet));
        walletService.holdCredits("user1", new BigDecimal("30.00"), "BKG2");
        assertEquals(new BigDecimal("70.00"), sampleWallet.getBalance());
        verify(txnRepo).save(any(WalletTransaction.class));
    }

    @Test
    void testHoldCredits_insufficientFundsThrows() {
        sampleWallet.setBalance(BigDecimal.ZERO);
        when(walletRepo.findByStudentId("user1")).thenReturn(Optional.of(sampleWallet));
        assertThrows(RuntimeException.class,
                () -> walletService.holdCredits("user1", new BigDecimal("1.00"), "BKG2"));
    }

    // ----------------------------------------------------------------------
    // releaseToTutor()
    // ----------------------------------------------------------------------
    @Test
    void testReleaseToTutor_distributesFundsCorrectly() {
        Wallet tutor = new Wallet("id2", "tutor1", BigDecimal.ZERO, "SGD", null, false, LocalDateTime.now());
        Wallet company = new Wallet("id3", "COMPANY_WALLET", BigDecimal.ZERO, "SGD", null, false, LocalDateTime.now());

        when(walletRepo.findByStudentId("tutor1")).thenReturn(Optional.of(tutor));
        when(walletRepo.findByStudentId("COMPANY_WALLET")).thenReturn(Optional.of(company));

        walletService.releaseToTutor("student1", "tutor1", new BigDecimal("100.00"), "BKG3");

        assertEquals(new BigDecimal("95.0000"), tutor.getBalance());
        assertEquals(new BigDecimal("5.0000"), company.getBalance());
        verify(txnRepo, times(3)).save(any(WalletTransaction.class));
    }

    // ----------------------------------------------------------------------
    // refundStudent()
    // ----------------------------------------------------------------------
    @Test
    void testRefundStudent_addsAmount() {
        when(walletRepo.findByStudentId("user1")).thenReturn(Optional.of(sampleWallet));
        walletService.refundStudent("user1", new BigDecimal("20.00"), "BKG4");
        assertEquals(new BigDecimal("120.00"), sampleWallet.getBalance());
        verify(txnRepo).save(any(WalletTransaction.class));
    }

    // ----------------------------------------------------------------------
    // simulateWithdrawal()
    // ----------------------------------------------------------------------
    @Test
    void testSimulateWithdrawal_success() {
        when(walletRepo.findByStudentId("user1")).thenReturn(Optional.of(sampleWallet));
        Map<String, Object> result = walletService.simulateWithdrawal("user1");
        assertEquals("Withdrawal successful", result.get("message"));
        assertEquals(BigDecimal.ZERO, sampleWallet.getBalance());
        verify(txnRepo).save(any(WalletTransaction.class));
    }

    @Test
    void testSimulateWithdrawal_noFunds() {
        sampleWallet.setBalance(BigDecimal.ZERO);
        when(walletRepo.findByStudentId("user1")).thenReturn(Optional.of(sampleWallet));
        Map<String, Object> result = walletService.simulateWithdrawal("user1");
        assertEquals("No funds available to withdraw", result.get("message"));
        verify(txnRepo, never()).save(any());
    }

    // ----------------------------------------------------------------------
    // getTransactions()
    // ----------------------------------------------------------------------
    @Test
    void testGetTransactions() {
        List<WalletTransaction> txns = List.of(new WalletTransaction());
        when(txnRepo.findByStudentIdOrderByCreatedAtDesc("user1")).thenReturn(txns);
        assertEquals(txns, walletService.getTransactions("user1"));
    }

    // ----------------------------------------------------------------------
    // getTransactionMetrics()
    // ----------------------------------------------------------------------
    @Test
    void testGetTransactionMetrics_fullData() {
        WalletTransaction highest = new WalletTransaction("id1", "user1", "TYPE",
                new BigDecimal("100"), "desc", "ref", LocalDateTime.now());
        MonthlyEarningDTO dto = new MonthlyEarningDTO();
        dto.set_id(1);
        dto.setTotal(200.0);

        when(txnRepo.getTotalEarnings()).thenReturn(1000.0);
        when(txnRepo.getTotalCommission()).thenReturn(50.0);
        when(txnRepo.getHighestTransaction()).thenReturn(highest);
        when(txnRepo.getMonthlyEarnings()).thenReturn(List.of(dto));

        AdminDashboardDTO.TransactionMetrics metrics = walletService.getTransactionMetrics();
        assertEquals(1000.0, metrics.getTotalEarnings());
        assertEquals(50.0, metrics.getCommissionCollected());
        assertNotNull(metrics.getHighestTransaction());
        assertEquals("JANUARY", metrics.getMonthlyEarnings().get(0).getMonth());
    }

    @Test
    void testGetTransactionMetrics_handlesNullsAndEmptyData() {
        when(txnRepo.getTotalEarnings()).thenReturn(null);
        when(txnRepo.getTotalCommission()).thenReturn(null);
        when(txnRepo.getHighestTransaction()).thenReturn(null);
        when(txnRepo.getMonthlyEarnings()).thenReturn(List.of(new MonthlyEarningDTO()));

        AdminDashboardDTO.TransactionMetrics metrics = walletService.getTransactionMetrics();
        assertEquals(0.0, metrics.getTotalEarnings());
        assertEquals(0.0, metrics.getCommissionCollected());
        assertNull(metrics.getHighestTransaction());
        assertTrue(metrics.getMonthlyEarnings().isEmpty());
    }
}
