package com.kzdrava.webapp.controllers;


import com.kzdrava.webapp.auth.AuthenticateRequest;
import com.kzdrava.webapp.auth.AuthenticationService;
import com.kzdrava.webapp.auth.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j(topic = "AuthenticationController")
public class AuthenticationController {

    private final AuthenticationService service;

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String getRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequest registerRequest, RedirectAttributes redirectAttributes) {
        ResponseEntity<String> response = service.register(registerRequest);
        if(response.getStatusCode().is2xxSuccessful()) {
            return "redirect:/login";
        }else {
            return "redirect:/register?error";
        }
    }

    @PostMapping("/authenticate")
    public String authenticate(@ModelAttribute AuthenticateRequest authenticateRequest, RedirectAttributes redirectAttributes) {
        ResponseEntity<String> response = service.authenticate(authenticateRequest);
        if(response.getStatusCode().is2xxSuccessful()) {
            return "redirect:/login?success";
        }else {
            return "redirect:/login";
        }
    }

    @ModelAttribute("authenticateRequest")
    public AuthenticateRequest authenticateRequest() {
        return new AuthenticateRequest();
    }

    @ModelAttribute("registerRequest")
    public RegisterRequest registerRequest() {
        return new RegisterRequest();
    }

}
