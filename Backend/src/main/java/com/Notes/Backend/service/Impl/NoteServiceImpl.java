package com.Notes.Backend.service.Impl;

import com.Notes.Backend.model.AccessType;
import com.Notes.Backend.model.Note;
import com.Notes.Backend.model.User;
import com.Notes.Backend.repository.NoteRepository;
import com.Notes.Backend.repository.UserRepository;
import com.Notes.Backend.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @Override
    public Note createNote(Note note) {
        note.setCreatedAt(new Date());
        note.setLastEdited(new Date());
        Note savedNote = noteRepository.save(note);

        userRepository.findById(note.getCreatedBy()).ifPresent(user -> {
            user.getNoteIds().add(savedNote.getId());
            userRepository.save(user);
        });

        return savedNote;
    }

    @Override
    public Optional<Note> findById(String id) {
        return noteRepository.findById(id);
    }

    @Override
    public List<Note> getNotesCreatedBy(String userId) {
        return noteRepository.findAllByCreatedBy(userId);
    }

    @Override
    public List<Note> getNotesSharedWith(String userId) {
        return noteRepository.findAll().stream()
                .filter(note -> note.getSharedWith().containsKey(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Note updateNoteContent(String noteId, String content, String title, String tag) {
        Optional<Note> noteOpt = noteRepository.findById(noteId);
        if (noteOpt.isEmpty()) return null;

        Note note = noteOpt.get();
        note.setContent(content);
        note.setTitle(title);
        note.setTag(tag);
        note.setLastEdited(new Date());

        return noteRepository.save(note);
    }

    @Override
    public void deleteNote(String noteId, String userId) {
        Optional<Note> noteOpt = noteRepository.findById(noteId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (noteOpt.isPresent() && userOpt.isPresent()) {
            Note note = noteOpt.get();
            User user = userOpt.get();

            user.getNoteIds().remove(noteId);
            user.getPinnedNoteIds().remove(noteId);
            user.getArchivedNoteIds().remove(noteId);
            userRepository.save(user);

            note.getSharedWith().keySet().forEach(sharedUserId -> {
                userRepository.findById(sharedUserId).ifPresent(sharedUser -> {
                    sharedUser.getSharedNoteIds().remove(noteId);
                    userRepository.save(sharedUser);
                });
            });

            noteRepository.deleteById(noteId);
        }
    }

    @Override
    public void shareNote(String noteId, String targetUserId, AccessType accessType) {
        noteRepository.findById(noteId).ifPresent(note -> {
            note.getSharedWith().put(targetUserId, accessType);
            noteRepository.save(note);

            userRepository.findById(targetUserId).ifPresent(user -> {
                user.getSharedNoteIds().add(noteId);
                userRepository.save(user);
            });
        });
    }

    @Override
    public void revokeShare(String noteId, String targetUserId) {
        noteRepository.findById(noteId).ifPresent(note -> {
            note.getSharedWith().remove(targetUserId);
            noteRepository.save(note);
        });

        userRepository.findById(targetUserId).ifPresent(user -> {
            user.getSharedNoteIds().remove(noteId);
            userRepository.save(user);
        });
    }

    @Override
    public void UpdateAccess(String noteId, String userId, AccessType newAccess) {
        noteRepository.findById(noteId).ifPresent(note -> {
            if (note.getSharedWith().containsKey(userId)) {
                note.getSharedWith().put(userId, newAccess);
                noteRepository.save(note);
            }
        });
    }

    @Override
    public boolean canEdit(String noteId, String userId) {
        return noteRepository.findById(noteId)
                .map(note -> note.getCreatedBy().equals(userId) ||
                        note.getSharedWith().get(userId) == AccessType.EDIT)
                .orElse(false);
    }

    @Override
    public boolean canView(String noteId, String userId) {
        return noteRepository.findById(noteId)
                .map(note -> note.getCreatedBy().equals(userId) ||
                        note.getSharedWith().containsKey(userId))
                .orElse(false);
    }

    @Override
    public List<Note> getPinnedNotesOrdered(String userId) {
        return userRepository.findById(userId)
                .map(user -> user.getPinnedNoteIds().stream()
                        .map(noteRepository::findById)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<Note> getDashboardNotes(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return Collections.emptyList();

        User user = userOpt.get();

        Set<String> pinnedIds = user.getPinnedNoteIds();
        Set<String> archivedIds = user.getArchivedNoteIds();

        List<Note> pinned = pinnedIds.stream()
                .map(noteRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        List<Note> others = user.getNoteIds().stream()
                .filter(noteId -> !pinnedIds.contains(noteId) && !archivedIds.contains(noteId))
                .map(noteRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        List<Note> result = new ArrayList<>();
        result.addAll(pinned);
        result.addAll(others);

        return result;
    }
}