package com.Notes.Backend.service;
import com.Notes.Backend.dto.AuthResponse;
import com.Notes.Backend.model.User;

import java.util.*;

public interface UserService {

    User registerUser(User user);

    AuthResponse login(String username,String rawPassword);

    Optional<User> findById(String id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    void sendFriendRequest(String fromUserId, String toUserId);

    void cancelFriendRequest(String fromUserId, String toUserId);

    void acceptFriendRequest(String fromUserId, String toUserId);

    void rejectFriendRequest(String fromUserId, String toUserId);

    List<User> getFriends(String userId);

    List<User> getReceivedRequests(String userId);

    List<User> getSentRequests(String userId);



    List<String> getPinnedNotes(String userId);

    List<String> getArchivedNotes(String userId);

    List<String> getCreatedNotes(String userId);

    List<String> getSharedNotes(String userId);
}
