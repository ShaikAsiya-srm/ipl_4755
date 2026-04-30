package com.ipl.backend.controller;

import com.ipl.backend.model.User;
import com.ipl.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user, HttpSession session) {
        try {
            if (user == null || isBlank(user.getUsername()) || isBlank(user.getPassword())) {
                return ResponseEntity.badRequest().body(error("Username and password are required"));
            }

            User loggedIn = userService.login(user.getUsername(), user.getPassword());
            if (loggedIn == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Invalid username or password"));
            }

            session.setAttribute("user", loggedIn);
            return ResponseEntity.ok(loggedIn);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Map<String, String> error(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
