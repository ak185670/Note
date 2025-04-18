package com.Notes.Backend.service.Impl;

import com.Notes.Backend.model.User;
import com.Notes.Backend.repository.UserRepository;
import com.Notes.Backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User registerUser(User user) {
        user.setCreatedAt(System.currentTimeMillis());
        return userRepository.save(user);
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
    public void pinNotes(String userId, String noteId) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getPinnedNoteIds().size() >= 20) {
                throw new RuntimeException("Cannot pin more than 20 notes");
            }
            user.getPinnedNoteIds().add(noteId);
            userRepository.save(user);
        });
    }

    @Override
    public void unPinNotes(String userId, String noteId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.getPinnedNoteIds().remove(noteId);
            userRepository.save(user);
        });
    }

    @Override
    public void archiveNotes(String userId, String noteId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.getArchivedNoteIds().add(noteId);
            userRepository.save(user);
        });
    }

    @Override
    public void unArchiveNotes(String userId, String noteId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.getArchivedNoteIds().remove(noteId);
            userRepository.save(user);
        });
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