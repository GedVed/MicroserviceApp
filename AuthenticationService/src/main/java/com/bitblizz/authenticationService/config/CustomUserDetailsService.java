package com.bitblizz.authenticationService.config;



import com.bitblizz.authenticationService.model.User;
import com.bitblizz.authenticationService.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> userCredentials = userRepository.findByUsername(username);
        return userCredentials.map(CustomUserDetails::new).orElseThrow(() -> new UsernameNotFoundException("User with " + username + " not found"));


    }

}
