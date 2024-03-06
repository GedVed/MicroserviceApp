package com.bitblizz.authenticationService.service;

import com.bitblizz.authenticationService.model.User;
import com.bitblizz.authenticationService.utility.tokenSeed;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtService {


    public void validateToken(final String token){
        Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
    }

    public String generateToken(User user, String username){
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("roles", user.getUserRole());
        return createToken(claims, username);
    }

    public String getUsernameFromToken(String token){
        try{
            Claims claims = Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
            return claims.getSubject();
            } catch (Exception e){
            throw new RuntimeException("Invalid token");
        }
    }

    public Map<String, String> generateTokens(User user, String username){
        Map<String, String> tokens = new HashMap<>();
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("roles", user.getUserRole());
        String token = createToken(claims,username);
        String refreshedToken = refreshToken(username);
        tokens.put("accessToken", token);
        tokens.put("refreshToken", refreshedToken);
        return tokens;
    }


    public String createToken(Map<String, Object> claims, String username){
        return Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ 1000 *60 * 30)).signWith(getKey(), SignatureAlgorithm.HS256).compact();
    }

    public String refreshToken(String username){
        return Jwts.builder().setSubject(username).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+1000 * 50 * 60 * 2)).signWith(getKey(), SignatureAlgorithm.HS256).compact();
    }


    private Key getKey(){

        byte [] keyBytes = Decoders.BASE64.decode(tokenSeed.SECRET);
        return Keys.hmacShaKeyFor(keyBytes);

    }
}
