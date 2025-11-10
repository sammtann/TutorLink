package com.csy.springbootauthbe.wallet.controller;

import com.csy.springbootauthbe.wallet.entity.Wallet;
import com.csy.springbootauthbe.wallet.entity.WalletTransaction;
import com.csy.springbootauthbe.wallet.service.WalletService;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = WalletController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    // Mock JWT dependencies (same pattern as TutorControllerTest)
    @MockBean
    private com.csy.springbootauthbe.config.JWTAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private com.csy.springbootauthbe.config.JWTService jwtService;

    @MockBean
    private com.csy.springbootauthbe.common.wrapper.UserDetailsServiceWrapper userDetailsServiceWrapper;

    private Wallet sampleWallet;

    @BeforeEach
    void setup() {
        sampleWallet = new Wallet("id1", "stu1", new BigDecimal("100.00"), "SGD",
                "hashed", true, null);
    }

    // ----------------------------------------------------------------------
    // GET /{studentId}
    // ----------------------------------------------------------------------
    @Test
    void testGetWallet_success() throws Exception {
        when(walletService.getWallet("stu1")).thenReturn(sampleWallet);

        mockMvc.perform(get("/api/v1/wallet/stu1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value("stu1"))
                .andExpect(jsonPath("$.balance").value(100.00));
    }



    // ----------------------------------------------------------------------
    // POST /deduct
    // ----------------------------------------------------------------------
    @Test
    void testDeduct_success() throws Exception {
        when(walletService.deductCredits(anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(sampleWallet);

        String body = """
                {"studentId":"stu1","amount":25,"bookingId":"BKG1"}
                """;

        mockMvc.perform(post("/api/v1/wallet/deduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value("stu1"));
    }

    // ----------------------------------------------------------------------
    // GET /transactions/{studentId}
    // ----------------------------------------------------------------------
    @Test
    void testGetTransactions_success() throws Exception {
        WalletTransaction txn = new WalletTransaction("id1", "stu1", "TYPE",
                new BigDecimal("10.00"), "desc", "ref", null);
        when(walletService.getTransactions("stu1")).thenReturn(List.of(txn));

        mockMvc.perform(get("/api/v1/wallet/transactions/stu1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value("stu1"));
    }

    // ----------------------------------------------------------------------
    // POST /set-pin
    // ----------------------------------------------------------------------
    @Test
    void testSetWalletPin_success() throws Exception {
        String body = """
                {"studentId":"stu1","pin":"1234"}
                """;

        mockMvc.perform(post("/api/v1/wallet/set-pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Wallet PIN set successfully"));
    }

    // ----------------------------------------------------------------------
    // POST /verify-pin
    // ----------------------------------------------------------------------
    @Test
    void testVerifyWalletPin_true() throws Exception {
        when(walletService.verifyWalletPin("stu1", "1234")).thenReturn(true);

        String body = """
                {"studentId":"stu1","pin":"1234"}
                """;

        mockMvc.perform(post("/api/v1/wallet/verify-pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    void testVerifyWalletPin_false() throws Exception {
        when(walletService.verifyWalletPin("stu1", "0000")).thenReturn(false);

        String body = """
                {"studentId":"stu1","pin":"0000"}
                """;

        mockMvc.perform(post("/api/v1/wallet/verify-pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(false));
    }

    // ----------------------------------------------------------------------
    // POST /withdraw
    // ----------------------------------------------------------------------
    @Test
    void testWithdraw_success() throws Exception {
        when(walletService.verifyWalletPin("stu1", "1234")).thenReturn(true);
        when(walletService.simulateWithdrawal("stu1"))
                .thenReturn(Map.of("message", "Withdrawal successful"));

        String body = """
                {"studentId":"stu1","pin":"1234"}
                """;

        mockMvc.perform(post("/api/v1/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Withdrawal successful"));
    }

    @Test
    void testWithdraw_invalidPin() throws Exception {
        when(walletService.verifyWalletPin("stu1", "9999")).thenReturn(false);

        String body = """
                {"studentId":"stu1","pin":"9999"}
                """;

        mockMvc.perform(post("/api/v1/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid wallet PIN"));
    }

    // ----------------------------------------------------------------------
    // POST /create-checkout-session
    // ----------------------------------------------------------------------
    @Test
    void testCreateCheckoutSession_success() throws Exception {
        Session mockSession = new Session();
        mockSession.setUrl("https://stripe.test/session123");

        try (MockedStatic<Session> mocked = Mockito.mockStatic(Session.class)) {
            mocked.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);

            String body = """
                    {"studentId":"stu1","amount":50}
                    """;

            mockMvc.perform(post("/api/v1/wallet/create-checkout-session")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.url").value("https://stripe.test/session123"));
        }
    }


}
