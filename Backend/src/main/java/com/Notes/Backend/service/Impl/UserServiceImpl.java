package com.Notes.Backend.service.Impl;

import com.Notes.Backend.Security.JwtService;
import com.Notes.Backend.dto.AuthResponse;
import com.Notes.Backend.model.User;
import com.Notes.Backend.repository.UserRepository;
import com.Notes.Backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService  {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public User registerUser(User user) {
        user.setCreatedAt(System.currentTimeMillis());
        return userRepository.save(user);
    }

    @Override
    public AuthResponse login(String username, String rawPassword) {
        User user =userRepository.findByUsername(username)
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));

        if(!passwordEncoder.matches(rawPassword,user.getPassword()))
        {
            throw new RuntimeException("Invalid Credentials");
        }
        String token = jwtService.generateToken(user.getId());

        return new AuthResponse(token);
    }

    @Override
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<List<User>> searchUsers(String query) {
        return userRepository.findByUsernameContainingIgnoreCase(query);
    }

    @Override
    public String getUsername(String userId) {
        return userRepository.findById(userId)
                .map(User::getUsername)
                .orElse(null);
    }


    @Override
    public List<String> getPinnedNotes(String userId) {
        return userRepository.findById(userId)
                .map(user -> new ArrayList<>(user.getPinnedNoteIds()))
                .orElse(new ArrayList<>());
    }

    @Override
    public List<String> getArchivedNotes(String userId) {
        return userRepository.findById(userId)
                .map(user -> new ArrayList<>(user.getArchivedNoteIds()))
                .orElse(new ArrayList<>());
    }

    @Override
    public List<String> getCreatedNotes(String userId) {
        return userRepository.findById(userId)
                .map(user -> new ArrayList<>(user.getNoteIds()))
                .orElse(new ArrayList<>());
    }

    @Override
    public List<String> getSharedNotes(String userId) {
        return userRepository.findById(userId)
                .map(user -> new ArrayList<>(user.getSharedNoteIds()))
                .orElse(new ArrayList<>());
    }


}