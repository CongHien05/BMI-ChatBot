package vn.vku.udn.hienpc.bmichatbot.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.vku.udn.hienpc.bmichatbot.dto.request.LoginRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.request.RegisterRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.response.JwtResponse;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.entity.UserRole;
import vn.vku.udn.hienpc.bmichatbot.service.JwtService;
import vn.vku.udn.hienpc.bmichatbot.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthApiController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthApiController(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Check if user already exists
        if (userService.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // Will be hashed in saveUser
        user.setFullName(request.getFullName());
        user.setRole(UserRole.USER);

        // Save user (password will be hashed with BCrypt)
        User savedUser = userService.saveUser(user);

        // Generate JWT token
        UserDetails userDetails = userService.loadUserByUsername(savedUser.getEmail());
        String token = jwtService.generateToken(userDetails);

        // Return response
        JwtResponse response = new JwtResponse(
                token,
                "Bearer",
                savedUser.getEmail(),
                savedUser.getUserId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Get user details
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername());

        // Generate JWT token
        String token = jwtService.generateToken(userDetails);

        // Return response
        JwtResponse response = new JwtResponse(
                token,
                "Bearer",
                user.getEmail(),
                user.getUserId()
        );

        return ResponseEntity.ok(response);
    }
}
