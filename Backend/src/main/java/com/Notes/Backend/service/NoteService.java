package com.Notes.Backend.service;
import com.Notes.Backend.dto.NoteResponse;
import com.Notes.Backend.model.AccessType;
import com.Notes.Backend.model.Note;
import com.Notes.Backend.model.User;
import jakarta.validation.Valid;

import java.util.*;
public interface NoteService {

    Note createNote(@Valid Note note, String token);

    Optional<Note> findById(String id);

    List<Note> getNotesCreatedBy(String userId);

    List<Note> getNotesSharedWith(String userId);

    Note updateNoteContent(String noteId, String content, String title, List<String> tags);

    void deleteNote(String noteId, String userId);

    void shareNote(String noteId, String targetUserId);

    void revokeShare(String noteId, String targetUserId);


    List<Note> getPinnedNotesOrdered( String userId);

    List<NoteResponse> getDashboardNotes(String userId, String search , String sortBy, String tag);

    void togglePin(String userId, String noteId);

    void toggleArchive(String userId, String noteId);

    List<NoteResponse> getArchivedNotes(String userId);

    List<User> getSharedUsers(String noteId);

}
