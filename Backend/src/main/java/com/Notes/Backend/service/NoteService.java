package com.Notes.Backend.service;
import com.Notes.Backend.model.AccessType;
import com.Notes.Backend.model.Note;

import java.util.*;
public interface NoteService {

    Note createNote(Note note);

    Optional<Note> findById(String id);

    List<Note> getNotesCreatedBy(String userId);

    List<Note> getNotesSharedWith(String userId);

    Note updateNoteContent(String noteId, String content, String title, String tag);

    void deleteNote(String noteId, String userId);

    void shareNote(String noteId, String targetUserId, AccessType accessType);

    void revokeShare(String noteId, String targetUserId);

    void UpdateAccess(String noteId, String userId, AccessType newAccess);

    boolean canEdit( String noteId, String userId);

    boolean canView( String noteId, String userId);

    List<Note> getPinnedNotesOrdered( String userId);

    List<Note> getDashboardNotes(String userId);
}
