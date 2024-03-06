package com.bitblizz.apiGateway.filters;


import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class Validator {

    public static final List<String> openEndpoints = List.of("auth/register", "auth/login", "eureka");

    public static final List<String> restrictedEndpoints = List.of("");

    public static final List<String> userEndpoints = List.of("/auth/delete", "/users");


    public Predicate<ServerHttpRequest> isSecured = serverHttpRequest -> openEndpoints.stream().noneMatch(uri -> serverHttpRequest.getURI().getPath().contains(uri));

    public Predicate<ServerHttpRequest> isAdmin = serverHttpRequest -> restrictedEndpoints.stream().anyMatch(uri -> serverHttpRequest.getURI().getPath().contains(uri));
    public Predicate<ServerHttpRequest> isUser = serverHttpRequest -> userEndpoints.stream().anyMatch(uri -> serverHttpRequest.getURI().getPath().contains(uri));
}
