package com.bitblizz.apiGateway.filters;

import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.bitblizz.apiGateway.utility.JwtUtility;
import java.util.List;
import java.util.Objects;



@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    public final Validator validator;
    public final JwtUtility jwtUtility;
    private static final String BEARER_PREFIX = "Bearer ";

    public AuthenticationFilter(Validator validator, JwtUtility jwtUtility){
        super(Config.class);
        this.validator = validator;
        this.jwtUtility = jwtUtility;
    }

    @Override
    public GatewayFilter apply(Config config){
        return ((exchange, chain) -> {
            if(validator.isSecured.test(exchange.getRequest())){
                if(!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authorization header");
                }

                String authenticationHeader = Objects.requireNonNull(Objects.requireNonNull(exchange.getRequest()).getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0);
                if(authenticationHeader != null && authenticationHeader.startsWith(BEARER_PREFIX)){
                    authenticationHeader = authenticationHeader.substring(BEARER_PREFIX.length());
                }
                try {
                    jwtUtility.validateToken(authenticationHeader);

                    List<String> role = jwtUtility.getRoles(authenticationHeader);
                    Mono<Void> result = user(exchange,chain, role);
                    if(result != null){
                        return result;
                    }
                }catch (Exception e){
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You don't have access to this resource");
                }
            }
            return chain.filter(exchange);
        });
    }


    private Mono<Void> user(ServerWebExchange serverWebExchange, GatewayFilterChain gatewayFilterChain, List<String> role){
        boolean isValid;
        if(role.contains("ADMIN")){
            isValid = validator.isAdmin.test(serverWebExchange.getRequest());
        }else if(role.contains("USER")){
            isValid = validator.isUser.test(serverWebExchange.getRequest());
        } else {
        serverWebExchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return serverWebExchange.getResponse().setComplete();
        }
        if (isValid){
            return gatewayFilterChain.filter(serverWebExchange);
        }else {
            serverWebExchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return serverWebExchange.getResponse().setComplete();
        }
    }






    public static class Config{

    }
}
