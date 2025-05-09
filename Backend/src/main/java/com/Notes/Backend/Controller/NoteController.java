package com.Notes.Backend.Controller;

import com.Notes.Backend.Security.JwtService;
import com.Notes.Backend.dto.NoteResponse;
import com.Notes.Backend.model.AccessType;
import com.Notes.Backend.model.Note;
import com.Notes.Backend.model.User;
import com.Notes.Backend.repository.NoteRepository;
import com.Notes.Backend.repository.UserRepository;
import com.Notes.Backend.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private  final JwtService jwtService;
    @PostMapping
    public Note createNote(@Valid @RequestBody  Note note, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7):authHeader;
        return noteService.createNote(note,token);
    }

    @GetMapping()
    public List<NoteResponse> getNotes(@RequestHeader("Authorization") String authHeader , @RequestParam(value="search", required=false)
                               String search , @RequestParam(value="sortBy", required=false) String sortBy , @RequestParam(value="tag",required=false) String tag,
                                       @RequestParam(value="archived",required=false) Boolean archived) {

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7):authHeader;
        String userId= jwtService.extractUserId(token);
        if(archived) return noteService.getArchivedNotes(userId);

        return noteService.getDashboardNotes(userId,search,sortBy,tag);
    }

    @GetMapping("/{noteId}")
    public Optional<Note> getNote(@PathVariable String noteId) {
        return noteService.findById(noteId);
    }

    @GetMapping("/SharedUsers/{noteId}")
    public List<User> getSharedUsers(@PathVariable String noteId)
    {
        return noteService.getSharedUsers(noteId);
    }
    @GetMapping("/shared/{userId}")
    public List<Note> getSharedNotes(@PathVariable String userId) {
        return noteService.getNotesSharedWith(userId);
    }

    @PutMapping("/{noteId}")
    public Note updateNote(@PathVariable String noteId, @RequestBody Note note) {
        return noteService.updateNoteContent(noteId, note.getContent(), note.getTitle(), note.getTags());
    }

    @DeleteMapping("/delete/{noteId}")
    public void deleteNote(@PathVariable String noteId, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7):authHeader;
        String userId= jwtService.extractUserId(token);
        noteService.deleteNote(noteId, userId);
    }

    @GetMapping("/share/{noteId}/{targetUserId}")
    public void shareNote(@RequestHeader("Authorization") String authHeader ,@PathVariable String noteId, @PathVariable String targetUserId) {
        System.out.println("share called");
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7):authHeader;
        String userId= jwtService.extractUserId(token);
        noteService.shareNote(noteId, targetUserId);
    }

    @GetMapping("/revoke/{noteId}/{targetUserId}")
    public void revokeShare(@RequestHeader("Authorization") String authHeader ,@PathVariable String noteId, @PathVariable String targetUserId) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7):authHeader;
        String userId= jwtService.extractUserId(token);
        noteService.revokeShare(noteId, targetUserId);
    }


    @GetMapping("/pinned")
    public List<Note> getPinnedNotes(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7):authHeader;
        String userId= jwtService.extractUserId(token);
        return noteService.getPinnedNotesOrdered(userId);
    }

    @PostMapping("/togglePin/{noteId}")
    public void pinNote(@PathVariable String noteId , @RequestHeader("Authorization") String authHeader)
    {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7):authHeader;
        String userId= jwtService.extractUserId(token);
        noteService.togglePin(userId,noteId);
    }

    @PostMapping("/toggleArchive/{noteId}")
    public void archiveNote(@PathVariable String noteId , @RequestHeader("Authorization") String authHeader)
    {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7):authHeader;
        String userId= jwtService.extractUserId(token);
        noteService.toggleArchive(userId,noteId);
    }

}