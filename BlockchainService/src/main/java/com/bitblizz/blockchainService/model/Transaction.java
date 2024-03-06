package com.bitblizz.blockchainService.model;

import com.bitblizz.blockchainService.utility.CryptoUtility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;


import java.util.List;


@lombok.Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {
    static ObjectMapper mapper = new ObjectMapper();
    @Id
    private String id;
    private long transactionId;
    private String hash;
    public Data data;


    public Transaction withHash() {
        hash = toHash();
        return this;
    }

    /**
     *
     * create a hash value from the transactionId and data
     *
     * @return the hash value
     */
    public String toHash() {
        try {
            return CryptoUtility.hash(transactionId + mapper.writeValueAsString(this.data));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json error", e);
        }
    }
    public void check(Transaction transaction) {

    }
    @lombok.Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data {
        private Input input;
        private List<Output> outputs;
    }
    @lombok.Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Input {
        private String contract;
        private String address;
        private String signature;
    }
    @lombok.Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Output {
        private String contract;
        private String address;
    }

}
