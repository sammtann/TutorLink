package com.csy.springbootauthbe.wallet.repository;

import com.csy.springbootauthbe.wallet.dto.MonthlyEarningDTO;
import com.csy.springbootauthbe.wallet.entity.WalletTransaction;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WalletTransactionRepository extends MongoRepository<WalletTransaction, String> {
    List<WalletTransaction> findByStudentIdOrderByCreatedAtDesc(String studentId);

    @Aggregation(pipeline = {
            "{ '$match': { 'type': { '$exists': true, '$ne': null, '$in': ['BOOKING_PAYMENT_TUTOR', 'BOOKING_COMMISSION'] } } }",
            "{ '$addFields': { 'amountNum': { '$toDecimal': '$amount' } } }",
            "{ '$group': { '_id': null, 'totalCommission': { '$sum': '$amountNum' } } }"
    })
    Double getTotalEarnings();

    @Aggregation(pipeline = {
            "{ '$match': { 'type': { '$exists': true, '$ne': null, '$eq': 'BOOKING_COMMISSION' } } }",
            "{ '$addFields': { 'amountNum': { '$toDecimal': '$amount' } } }",
            "{ '$group': { '_id': null, 'totalCommission': { '$sum': '$amountNum' } } }"
    })
    Double getTotalCommission();

    @Aggregation(pipeline = {
            "{ '$match': { 'type': { '$exists': true, '$ne': null, '$in': ['BOOKING_PAYMENT_TUTOR', 'BOOKING_COMMISSION'] } } }",
            "{ '$sort': { 'amount': -1 } }",
            "{ '$limit': 1 }"
    })
    WalletTransaction getHighestTransaction();

    @Aggregation(pipeline = {
            "{ '$match': { 'type': 'BOOKING_COMMISSION', 'createdAt': { '$ne': null } } }",
            "{ '$group': { '_id': { '$month': '$createdAt' }, 'total': { '$sum': { '$toDouble': { '$ifNull': ['$amount', 0] } } } } }",
            "{ '$sort': { '_id': 1 } }"
    })
    List<MonthlyEarningDTO> getMonthlyEarnings();
}