package com.bitblizz.blockchainService;


import com.bitblizz.blockchainService.model.Blockchain;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
public class BlockchainServiceApp {
    public static void main(String[] args){
        SpringApplication.run(BlockchainServiceApp.class, args);
    }
}
