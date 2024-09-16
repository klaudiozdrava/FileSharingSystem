package com.kzdrava.webapp.auth;

import com.kzdrava.webapp.entities.Role;
import com.kzdrava.webapp.entities.User;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



@Service
@Slf4j(topic = "AuthenticationService")
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<String> register(RegisterRequest request) {

        if(repository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(409)
                    .body("User already exists");
        }
        log.info("User email {}", request.getEmail());
        log.info("User password {}", request.getPassword());

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        log.info("User email {}", user.getEmail());
        log.info("User password {}", user.getPassword());

        repository.save(user);

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> authenticate(AuthenticateRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            return ResponseEntity.ok().build();
        }
        catch (DisabledException e) {
            log.warn("User account is disabled: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("User account is disabled");
        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials provided for user: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        } catch (AuthenticationException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication failed");
        }

    }


}
