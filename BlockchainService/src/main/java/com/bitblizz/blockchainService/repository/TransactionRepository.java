package com.bitblizz.blockchainService.repository;

import com.bitblizz.blockchainService.model.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
    Mono<Transaction> findByTransactionId(long transactionId);
}
