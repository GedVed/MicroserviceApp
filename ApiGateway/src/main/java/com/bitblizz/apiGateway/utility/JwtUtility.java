package com.bitblizz.apiGateway.utility;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;

import java.util.List;

@Component
public class JwtUtility {


    public Claims extractClaims(final String token){
        return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
    }

    public void validateToken(final String token){
        Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
    }

    public List<String> getRoles(final String token){
        Claims claims = extractClaims(token);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(claims.get("roles"), new TypeReference<>(){});
    }


    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(tokenSeed.SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
