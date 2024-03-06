package com.bitblizz.blockchainService.model;

import com.bitblizz.blockchainService.utility.CryptoUtility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document
public class Block {
    static ObjectMapper mapper = new ObjectMapper();
    public static Block genesis = Block.builder()
            .index(0)
            .previousHash("0")
            .timestamp(0)
            .nonce(0)
            .transactions(Collections.emptyList())
            .build()
            .withHash();
    @Id
    private String id;
    private long index;
    private String previousHash;
    private long timestamp;
    private long nonce;
    private String hash;
    private List<Transaction> transactions;

    /**
     *
     * Add a 'wither' for hash since it must be calculate after the block is built
     *
     * @return the block with a hash added
     */
    public Block withHash() {
        hash = toHash();
        return this;
    }

    /**
     *
     * create a hash value from the index, previousHash, timestamp, nonce, and transactions
     *
     * @return the hash value
     */
    public String toHash() {
        try {
            return CryptoUtility.hash(this.index + this.previousHash + this.timestamp + mapper.writeValueAsString(this.transactions) + this.nonce);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json decoding error", e);
        }
    }


    public long calculateDifficulty() {
        return -1l;
    }
}