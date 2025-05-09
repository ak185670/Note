package com.Notes.Backend.Controller;

import com.Notes.Backend.Security.JwtService;
import com.Notes.Backend.model.User;
import com.Notes.Backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {



    private final UserService userService;
    private final JwtService jwtService;

    @GetMapping()
    public String getUsername(@RequestHeader("Authorization") String authHeader)
    {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7):authHeader;
        String userId = jwtService.extractUserId(token);
        return userService.getUsername(userId);
    }
    @GetMapping("/search")
    public Optional<List<User>> searchUsers(@RequestParam String query)
    {
        return userService.searchUsers(query);
    }

}