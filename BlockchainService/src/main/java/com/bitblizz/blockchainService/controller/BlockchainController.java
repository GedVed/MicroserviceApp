package com.bitblizz.blockchainService.controller;


import com.bitblizz.blockchainService.model.Block;
import com.bitblizz.blockchainService.model.Blockchain;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Random;


@RestController
@Slf4j
@RequestMapping("/block")
public class BlockchainController {
        private static final ObjectMapper objectMapper = new ObjectMapper();
        private final Blockchain blockchain;
        final Random random = new Random();

        public BlockchainController(Blockchain blockchain) {
            this.blockchain = blockchain;
        }


        @GetMapping(path = "{blockHash}", produces = MediaType.APPLICATION_JSON_VALUE)
        public Mono<Block> getBlock(@PathVariable("blockHash") String blockHash) {
            return blockchain.getBlockByHash(blockHash)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Block Not Found")));

        }
        @PostMapping(path = "/mine", produces = MediaType.APPLICATION_JSON_VALUE)
        @ResponseStatus(code = HttpStatus.CREATED, reason = "Block created")
        Mono<Block> mine(@PathVariable("address") String address) {
            return blockchain.getAllTransactions().collectList()
                    .flatMap(ts -> blockchain.getLastBlock()
                            .map(b -> Block.builder()
                                    .transactions(ts)
                                    .nonce(0)
                                    .previousHash(b.getHash())
                                    .timestamp(Instant.now().getEpochSecond())
                                    .index(b.getIndex() + 1)
                                    .build()
                                    .withHash())
                            .flatMap(b -> blockchain.addBlock(b, true)));
        }
    }


