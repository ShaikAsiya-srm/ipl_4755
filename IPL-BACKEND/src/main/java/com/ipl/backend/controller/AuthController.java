package com.ipl.backend.controller;

import com.ipl.backend.model.LoginRequest;
import com.ipl.backend.model.RegisterRequest;
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
            User safeUser = new User();
            safeUser.setId(user.getId());
            safeUser.setUsername(user.getUsername());
            safeUser.setEmail(user.getEmail());
            safeUser.setPoints(user.getPoints());
            return ResponseEntity.ok(safeUser);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new HashMap<>());
        }
    }

    // Legacy login/register endpoint (kept for backward compatibility)
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

    // Clean login endpoint: POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            User loggedIn = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
            if (loggedIn != null) {
                session.setAttribute("user", loggedIn);
                User safeUser = new User();
                safeUser.setId(loggedIn.getId());
                safeUser.setUsername(loggedIn.getUsername());
                safeUser.setEmail(loggedIn.getEmail());
                safeUser.setPoints(loggedIn.getPoints());
                return ResponseEntity.ok(safeUser);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid username or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Register endpoint: POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest, HttpSession session) {
        try {
            User newUser = new User();
            newUser.setUsername(registerRequest.getUsername());
            newUser.setPassword(registerRequest.getPassword());
            newUser.setEmail(registerRequest.getEmail());
            
            User registered = userService.register(newUser);
            session.setAttribute("user", registered);
            
            User safeUser = new User();
            safeUser.setId(registered.getId());
            safeUser.setUsername(registered.getUsername());
            safeUser.setEmail(registered.getEmail());
            safeUser.setPoints(registered.getPoints());
            return ResponseEntity.ok(safeUser);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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
