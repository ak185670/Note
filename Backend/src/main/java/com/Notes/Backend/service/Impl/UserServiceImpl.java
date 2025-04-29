package com.Notes.Backend.service.Impl;

import com.Notes.Backend.Security.JwtService;
import com.Notes.Backend.dto.AuthResponse;
import com.Notes.Backend.model.User;
import com.Notes.Backend.repository.UserRepository;
import com.Notes.Backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

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
    public void sendFriendRequest(String fromUserId, String toUserId) {
        Optional<User> fromOpt = userRepository.findById(fromUserId);
        Optional<User> toOpt = userRepository.findById(toUserId);

        if (fromOpt.isPresent() && toOpt.isPresent()) {
            User from = fromOpt.get();
            User to = toOpt.get();

            if (!from.getFriends().contains(toUserId) &&
                    !from.getSentRequests().contains(toUserId) &&
                    !to.getReceivedRequests().contains(fromUserId)) {

                from.getSentRequests().add(toUserId);
                to.getReceivedRequests().add(fromUserId);

                userRepository.save(from);
                userRepository.save(to);
            }
        }
    }

    @Override
    public void cancelFriendRequest(String fromUserId, String toUserId) {
        Optional<User> fromOpt = userRepository.findById(fromUserId);
        Optional<User> toOpt = userRepository.findById(toUserId);

        if (fromOpt.isPresent() && toOpt.isPresent()) {
            User from = fromOpt.get();
            User to = toOpt.get();

            from.getSentRequests().remove(toUserId);
            to.getReceivedRequests().remove(fromUserId);

            userRepository.save(from);
            userRepository.save(to);
        }
    }

    @Override
    public void acceptFriendRequest(String fromUserId, String toUserId) {
        Optional<User> fromOpt = userRepository.findById(fromUserId);
        Optional<User> toOpt = userRepository.findById(toUserId);

        if (fromOpt.isPresent() && toOpt.isPresent()) {
            User from = fromOpt.get();
            User to = toOpt.get();

            if (to.getReceivedRequests().remove(fromUserId)) {
                from.getSentRequests().remove(toUserId);

                from.getFriends().add(toUserId);
                to.getFriends().add(fromUserId);

                userRepository.save(from);
                userRepository.save(to);
            }
        }
    }

    @Override
    public void rejectFriendRequest(String fromUserId, String toUserId) {
        Optional<User> fromOpt = userRepository.findById(fromUserId);
        Optional<User> toOpt = userRepository.findById(toUserId);

        if (fromOpt.isPresent() && toOpt.isPresent()) {
            User from = fromOpt.get();
            User to = toOpt.get();

            from.getSentRequests().remove(toUserId);
            to.getReceivedRequests().remove(fromUserId);

            userRepository.save(from);
            userRepository.save(to);
        }
    }

    @Override
    public List<User> getFriends(String userId) {
        return userRepository.findById(userId)
                .map(user -> user.getFriends().stream()
                        .map(friendId -> userRepository.findById(friendId).orElse(null))
                        .filter(Objects::nonNull)
                        .toList())
                .orElse(Collections.emptyList());
    }

    @Override
    public List<User> getReceivedRequests(String userId) {
        return userRepository.findById(userId)
                .map(user -> user.getReceivedRequests().stream()
                        .map(requesterId -> userRepository.findById(requesterId).orElse(null))
                        .filter(Objects::nonNull)
                        .toList())
                .orElse(Collections.emptyList());
    }

    @Override
    public List<User> getSentRequests(String userId) {
        return userRepository.findById(userId)
                .map(user -> user.getSentRequests().stream()
                        .map(requesterId -> userRepository.findById(requesterId).orElse(null))
                        .filter(Objects::nonNull)
                        .toList())
                .orElse(Collections.emptyList());
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