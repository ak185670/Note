package com.Notes.Backend.Controller;

import com.Notes.Backend.model.User;
import com.Notes.Backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/friend-request/{fromUserId}/{toUserId}")
    public void sendFriendRequest(@PathVariable String fromUserId, @PathVariable String toUserId) {
        userService.sendFriendRequest(fromUserId, toUserId);
    }

    @PostMapping("/friend-request/accept/{userId}/{requesterId}")
    public void acceptFriendRequest(@PathVariable String userId, @PathVariable String requesterId) {
        userService.acceptFriendRequest(userId, requesterId);
    }

    @PostMapping("/friend-request/reject/{userId}/{requesterId}")
    public void rejectFriendRequest(@PathVariable String userId, @PathVariable String requesterId) {
        userService.rejectFriendRequest(userId, requesterId);
    }

    @PostMapping("/friend-request/cancel/{userId}/{toUserId}")
    public void cancelFriendRequest(@PathVariable String userId, @PathVariable String toUserId) {
        userService.cancelFriendRequest(userId, toUserId);
    }

    @GetMapping("/friends/{userId}")
    public List<User> getFriends(@PathVariable String userId) {
        return userService.getFriends(userId);
    }

    @GetMapping("/requests/received/{userId}")
    public List<User> getReceivedRequests(@PathVariable String userId) {
        return userService.getReceivedRequests(userId);
    }

    @GetMapping("/requests/sent/{userId}")
    public List<User> getSentRequests(@PathVariable String userId) {
        return userService.getSentRequests(userId);
    }
}