package com.bitblizz.authenticationService.unit;

import com.bitblizz.authenticationService.model.Role;
import com.bitblizz.authenticationService.model.User;
import com.bitblizz.authenticationService.service.JwtService;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.security.Key;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class JwtServiceTest {


    @Autowired
    private JwtService jwtService;

    private User user;

    private String username;

    private static final String SECRET = "1263315F2D83DBE132C1EBC5C615DC3456CD5EEBA4E59BD33DEDC5529D";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        user = new User();
        user.setUserRole(List.of(Role.USER));
        username = "testUser";
    }

    @Test
    void generateToken(){

        String token = jwtService.generateToken(user, username);
        assertNotNull(token);
        assertTrue(token.length() != 0);
        Map<String, Object> claims = getClaims(token);
        assertNotNull(claims);
        assertTrue(claims.containsKey("username"));
        assertTrue(claims.containsKey("roles"));
        assertTrue(claims.get("roles") instanceof Iterable);
        assertEquals(claims.get("username"), username);
        assertEquals("USER",(((Iterable<?>) claims.get("roles")).iterator().next()));
    }

    @Test
    void validateToken(){
        String token = jwtService.generateToken(user, username);
        assertNotNull(token);
        jwtService.validateToken(token);
    }

    @Test
    void testGetUsername() {
        String token = jwtService.generateToken(user, username);
        assertEquals(username, jwtService.getUsernameFromToken(token));
    }

    @Test
    void testInvalidToken(){
        assertThrows(RuntimeException.class, () -> jwtService.getUsernameFromToken("wrongToken"));
    }

    /*
    @Test
    void testGenerateMultipleTokens(){
        Map<String, String> tokens = jwtService.generateTokens(user, username);

        assertNotNull(tokens);
        assertEquals(2, tokens.size());
        String accessToken = tokens.get("accessToken");
        assertNotNull(accessToken);
        String[] splitAccessToken = accessToken.split("\\.");
        assertEquals(3, splitAccessToken.length);
        String refreshToken = tokens.get("refreshToken");
        assertNotNull(refreshToken);
        String[] splitRefreshToken = refreshToken.split("\\.");
        assertEquals(3, splitRefreshToken.length);

    }
*/

    private Key getKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims getClaims(final String token){
        return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
    }
}
