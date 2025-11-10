package com.csy.springbootauthbe.wallet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wallets")
public class Wallet {
    @Id
    private String id;
    private String studentId;
    private BigDecimal balance = BigDecimal.ZERO;
    private String currency = "SGD";
    private String walletPin;
    private Boolean pinSet = false;
    private LocalDateTime updatedAt;
}

