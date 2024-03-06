package com.bitblizz.blockchainService.model;

import com.bitblizz.blockchainService.error.BlockAssertionError;
import com.bitblizz.blockchainService.error.TransactionAssertionError;
import com.bitblizz.blockchainService.repository.BlockRepository;
import com.bitblizz.blockchainService.repository.TransactionRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@Component
public class Blockchain {
    public static final String TOPIC = "blockchain";

    final BlockRepository blocks;
    final TransactionRepository transactions;
    final Random random = new Random();


    public Blockchain(BlockRepository blocks,
                      TransactionRepository transactionRepository) {
        // Some places uses the emitter to act after some data is changed
        this.blocks = blocks;
        this.transactions = transactionRepository;
        blocks.findAll()
                .flatMap(this::removeBlockTransactionsFromTransactions)
                .switchIfEmpty(Mono.from(blocks.save(Block.genesis))
                        .map(result -> Block.genesis))
                .subscribe(b -> log.info("inspecting block {} on startup", b),
                        e -> log.error("error inspecting blocks on startup", e));
    }


    public Flux<Block> getAllBlocks() {
        return blocks.findAll();
    }


    public Mono<Block> getBlockByHash(String hash) {
        return blocks.findByHash(hash);
    }


    public Mono<Block> getLastBlock() {
        // todo figure out a better way to get max from mongo
        AtomicLong max = new AtomicLong(-1L);
        return blocks.findAll()
                .doOnNext(b -> max.getAndUpdate(i -> Math.max(i, b.getIndex())))
                .last()
                .flatMap(b -> blocks.findByIndex(max.get()));
    }


    public long getDifficulty(long index) {
        return 0L;
    }

    public Flux<Transaction> getAllTransactions() {
        return transactions.findAll();
    }


    public Mono<Transaction> getTransactionById(long id) {
        return transactions.findByTransactionId(id);
    }


    public Flux<Block> findTransactionInChain(long transactionId, Flux<Block> referenceBlockchain) {
        return referenceBlockchain
                .filter(b -> b.getTransactions()
                        .stream()
                        .anyMatch(t -> t.getTransactionId() == transactionId)
                );
    }


    public Mono<Block> addBlock(Block newBlock, boolean emit) {
        log.info("adding block {}", newBlock);
        return getLastBlock()
                .doOnNext(b -> log.info("in addBlock last block = {}", b))
                .flatMap(l -> checkBlock(newBlock, l, getAllBlocks())
                        .map(t -> newBlock))
                .flatMap(ignore -> blocks.save(newBlock))
                .map(ignore -> newBlock);
    }



    public Mono<Transaction> addTransaction(Transaction newTransaction, boolean emit) {
        return checkTransaction(newTransaction, getAllBlocks())
                .map(b -> newTransaction)
                .flatMap(t -> transactions.findByTransactionId(t.getTransactionId()))
                .doOnNext(t -> log.info("transaction is already present"))
                .switchIfEmpty(transactions.save(newTransaction)
                        .doOnNext(t -> log.info("saved transaction {}", t)));
    }

    public Mono<Block> removeBlockTransactionsFromTransactions(Block newBlock) {
        if(newBlock.getTransactions().isEmpty()) {
            return Mono.just(newBlock);
        }
        return Flux.fromStream(newBlock.getTransactions().stream())
                .flatMap(t -> transactions.findByTransactionId(t.getTransactionId()))
                .doOnNext(t -> log.info("removing transaction {} from loose transactions", t))
                .onErrorReturn(Transaction.builder().transactionId(0).build())
                .filter(t -> t.getTransactionId() != 0)
                .flatMap(t -> transactions.delete(t))
                .map(ignore -> newBlock)
                .switchIfEmpty(Flux.just(newBlock))
                .last();
    }


    public Mono<Block> checkBlock(Block newBlock, Block previousBlock, Flux<Block> referenceBlockchain) {
        log.info("checking block {}", newBlock);
        final String blockHash = newBlock.toHash();

        if (previousBlock.getIndex() + 1 != newBlock.getIndex()) { // Check if the block is the last onelog.error("Invalid index: expected {} got {}", previousBlock.getIndex() + 1, newBlock.getIndex());
            return Mono.error(() -> new BlockAssertionError("Invalid index: expected "
                    + (previousBlock.getIndex() + 1)
                    + " got "
                    + newBlock.getIndex()));
        } else if (!previousBlock.getHash().equals(newBlock.getPreviousHash())) { // Check if the previous block is correct
            return Mono.error(() -> new BlockAssertionError("Invalid previoushash: expected "
                    + previousBlock.getHash()
                    + " got "
                    + newBlock.getPreviousHash()));
        } else if (!blockHash.equals(newBlock.getHash())) { // Check if the hash is correct
            return Mono.error(() -> new BlockAssertionError("Invalid hash: expected "
                    + blockHash
                    + " got "
                    + newBlock.getHash()));
        } else if (newBlock.calculateDifficulty() >= getDifficulty(newBlock.getIndex())) { // If the difficulty level of the proof-of-work challenge is correct
            return Mono.error(() ->  new BlockAssertionError("Invalid proof-of-work difficulty: expected "
                    + newBlock.calculateDifficulty()
                    + " be smaller than "
                    + getDifficulty(newBlock.getIndex())));
        }
        return Flux.fromStream(newBlock.getTransactions().stream())
                .flatMap(t -> checkTransaction(t, referenceBlockchain))
                .switchIfEmpty(Flux.just(Transaction.builder().build()))
                .last()
                .map(ignore -> newBlock);
    }


    public Mono<Transaction> checkTransaction(Transaction transaction, Flux<Block> referenceBlockchain) {
        log.info("checking transaction {}", transaction);
        transaction.check(transaction);
        log.info("transaction checked out");
        return findTransactionInChain(transaction.getTransactionId(), referenceBlockchain)
                .map(ignore -> transaction)
                .flatMap(ignore -> Flux.<Transaction>error(() ->
                        new TransactionAssertionError("block found with transaction")))
                .switchIfEmpty(Flux.just(transaction))
                .last();
    }
}


