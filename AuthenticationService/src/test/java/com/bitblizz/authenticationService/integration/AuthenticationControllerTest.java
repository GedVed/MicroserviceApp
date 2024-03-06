package com.bitblizz.authenticationService.integration;

import com.bitblizz.authenticationService.dto.DeleteDto;
import com.bitblizz.authenticationService.dto.UserDto;
import com.bitblizz.authenticationService.request.AuthenticationRequest;
import com.bitblizz.authenticationService.request.AuthenticationResponse;
import org.apache.hc.client5.http.ssl.HttpsSupport;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

public class AuthenticationControllerTest {

    @LocalServerPort
    private int port;
    private String baseUrl = "http://localhost";

    private static RestTemplate restTemplate;

    @BeforeEach
    public void setUp() throws URISyntaxException {
        baseUrl = baseUrl.concat(":").concat(port + "").concat("/auth");
    }

    public static RestTemplate getRestTemplate() {
        return restTemplate;
    }

    @BeforeAll
    public static void init() {
        restTemplate = new RestTemplate();
    }



    @Test
    void testToken(){

        UserDto userDto = new UserDto("John", "Doe","testusername","testpassword");

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(baseUrl + "/register", userDto, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setUsername("testusername");
        authenticationRequest.setPassword("testpassword");

        ResponseEntity<AuthenticationResponse> response = restTemplate.postForEntity(baseUrl + "/login", authenticationRequest, AuthenticationResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        String token = Objects.requireNonNull(response.getBody()).getToken();
        String validate = baseUrl + "/validate?token=" + token;

        ResponseEntity<String> validResponse = restTemplate.getForEntity(validate, String.class);
        assertEquals(HttpStatus.OK, validResponse.getStatusCode());
        assertEquals("Valid token", validResponse.getBody());

    }

/*
    @Test
    void testAddUser() {

        UserDto addUserDto = new UserDto("John", "Doe","testusername","testpassword");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        HttpEntity<UserDto> entity = new HttpEntity<>(addUserDto,headers);


        ResponseEntity<String> response = restTemplate.exchange(baseUrl.concat("/register") ,
                HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User has been added to the system", response.getBody());
    }
*/
    @Test
    void testDeleteSelf(){
        UserDto userDto = new UserDto("John", "Doe","testusername","testpassword");
        restTemplate.postForObject(baseUrl + "/register", userDto, String.class);

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setUsername("testusername");
        authenticationRequest.setPassword("testpassword");

        restTemplate.postForObject(baseUrl + "/login", authenticationRequest, AuthenticationResponse.class);

        DeleteDto deleteDto =  new DeleteDto(userDto.getUsername(), userDto.getPassword());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DeleteDto> httpEntity = new HttpEntity<>(deleteDto, httpHeaders);

        ResponseEntity<Boolean> response = restTemplate.exchange(baseUrl + "/" + "delete", HttpMethod.POST, httpEntity, Boolean.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertEquals(Boolean.TRUE, response.getBody());

    }

/*
    @Test
    void testUserAlreadyExists(){

        UserDto addUserDto = new UserDto("John", "Doe","testusername","testpassword");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserDto> httpEntity = new HttpEntity<>(addUserDto,headers);


        ResponseEntity<String> responseEntity = restTemplate.exchange(baseUrl.concat("/register"), HttpMethod.POST, httpEntity, String.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User has been added to the system", responseEntity.getBody());

        ResponseEntity<String> response = restTemplate.exchange(baseUrl.concat("/register"), HttpMethod.POST, httpEntity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Username taken", response.getBody());
    }
*/
    @Test
    void testUserLogin(){
        UserDto userDto = new UserDto("John", "Doe","testusername","testpassword");

        restTemplate.postForObject(baseUrl + "/register", userDto, String.class);

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setUsername("testusername");
        authenticationRequest.setPassword("testpassword");

        ResponseEntity<AuthenticationResponse> response = restTemplate.postForEntity(baseUrl + "/login", authenticationRequest, AuthenticationResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        AuthenticationResponse authenticationResponse = response.getBody();
        assertNotNull(authenticationResponse);
        assertNotNull(authenticationResponse.getToken());
        assertNotNull(authenticationResponse.getUserRole());
    }

    @Test
    void wrongToken(){


        String invalidToken = "something";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(baseUrl.concat("/validate?token=")
                            .concat(invalidToken),
                    HttpMethod.GET, httpEntity, String.class);
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }

    @Test
    void wrongPassword(){
        UserDto userDto = new UserDto("John", "Doe","testusername","testpassword");

        restTemplate.postForObject(baseUrl + "/register", userDto, String.class);

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setUsername("testusername");
        authenticationRequest.setPassword("invalidpassword");

        try {
            restTemplate.postForObject(baseUrl+"/login", authenticationRequest, AuthenticationResponse.class);
        }catch (HttpClientErrorException e){
            assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
        }
    }

}



