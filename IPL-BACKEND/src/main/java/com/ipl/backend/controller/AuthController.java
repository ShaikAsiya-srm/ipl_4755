package com.ipl.backend.controller;

import com.ipl.backend.model.User;
import com.ipl.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // Check current session
    @GetMapping
    public ResponseEntity<?> checkSession(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new HashMap<>());
        }
    }

    // Login or Register
    @PostMapping
    public ResponseEntity<?> authenticate(@RequestBody User user, @RequestParam String action, HttpSession session) {
        try {
            if ("register".equals(action)) {
                User registered = userService.register(user);
                session.setAttribute("user", registered);
                return ResponseEntity.ok(registered);
            } else if ("login".equals(action)) {
               User loggedIn = userService.login(user.getUsername(), user.getPassword());
                if (loggedIn != null) {
                    session.setAttribute("user", loggedIn);
                    return ResponseEntity.ok(loggedIn);
                } else {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Invalid credentials");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
                }
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Missing action parameter: login or register");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Logout
    @DeleteMapping
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    // Debug endpoint to check users
    @GetMapping("/debug/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            // This is just for debugging - remove in production
            java.sql.Connection conn = java.sql.DriverManager.getConnection(
                "jdbc:h2:mem:ipldb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", "sa", "");
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery("SELECT username, password FROM users");

            java.util.List<String> users = new java.util.ArrayList<>();
            while (rs.next()) {
                users.add(rs.getString("username") + ":" + rs.getString("password"));
            }

            rs.close();
            stmt.close();
            conn.close();

            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

}
