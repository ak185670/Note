package com.Notes.Backend.service.Impl;

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
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;


    @Override
    public Note createNote(@Valid Note note, String token) {
        String userId = jwtService.extractUserId(token);
        note.setCreatedAt(new Date());
        note.setLastEdited(new Date());
        note.setCreatedBy(userId);
        User user =userRepository.findById(userId).orElseThrow(()->new RuntimeException("user not found"));
        note.setCreatedUser(user.getUsername());
        if (note.getSharedWith() == null) note.setSharedWith(new ArrayList<>());

        Note savedNote = noteRepository.save(note);


        user.getNoteIds().add(savedNote.getId());
        userRepository.save(user);


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
                .filter(note -> note.getSharedWith() != null && note.getSharedWith().contains(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Note updateNoteContent(String noteId, String content, String title, List<String> tags) {
        Optional<Note> noteOpt = noteRepository.findById(noteId);
        if (noteOpt.isEmpty()) return null;

        Note note = noteOpt.get();
        note.setContent(content);
        note.setTitle(title);
        note.setTags(tags);
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
            if(note.getCreatedBy().equals(user.getId())) {

                user.getNoteIds().remove(noteId);
                user.getPinnedNoteIds().remove(noteId);
                user.getArchivedNoteIds().remove(noteId);
                userRepository.save(user);

                if (note.getSharedWith() != null) {
                    note.getSharedWith().forEach(sharedUserId -> {
                        userRepository.findById(sharedUserId).ifPresent(sharedUser -> {
                            sharedUser.getSharedNoteIds().remove(noteId);
                            userRepository.save(sharedUser);
                        });
                    });
                }

                noteRepository.deleteById(noteId);
            }
            else {
                note.getSharedWith().remove(userId);
                user.getSharedNoteIds().remove(noteId);
                noteRepository.save(note);
                userRepository.save(user);
            }
        }
    }

    @Override
    public void shareNote(String noteId, String targetUserId) {
        noteRepository.findById(noteId).ifPresent(note -> {
            if (note.getSharedWith() == null) {
                note.setSharedWith(new ArrayList<>());
            }
            if (!note.getSharedWith().contains(targetUserId)) {
                note.getSharedWith().add(targetUserId);
            }
            noteRepository.save(note);

            userRepository.findById(targetUserId).ifPresent(user -> {
                if (!user.getSharedNoteIds().contains(noteId)) {
                    user.getSharedNoteIds().add(noteId);
                    userRepository.save(user);
                }
            });
        });
    }

    @Override
    public void revokeShare(String noteId, String targetUserId) {
        noteRepository.findById(noteId).ifPresent(note -> {
            if (note.getSharedWith() != null) {
                note.getSharedWith().remove(targetUserId);
            }
            noteRepository.save(note);
        });

        userRepository.findById(targetUserId).ifPresent(user -> {
            user.getSharedNoteIds().remove(noteId);
            userRepository.save(user);
        });
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
    public List<NoteResponse> getDashboardNotes(String userId, String search, String sortBy, String tag) {
        List<Note> allNotes = new ArrayList<>();
        allNotes.addAll(getNotesCreatedBy(userId));
        allNotes.addAll(getNotesSharedWith(userId));

        allNotes = allNotes.stream()
                .filter(note -> !note.isArchived())
                .collect(Collectors.toList());

        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            allNotes = allNotes.stream()
                    .filter(note ->
                            (note.getTitle() != null && note.getTitle().toLowerCase().contains(searchLower)) ||
                                    (note.getContent() != null && note.getContent().toLowerCase().contains(searchLower)) ||
                                    (note.getTags() != null && note.getTags().stream().anyMatch(tagItem -> tagItem.toLowerCase().contains(searchLower)))
                    )
                    .collect(Collectors.toList());
        }

        if (tag != null && !tag.isEmpty()) {
            String tagLower = tag.toLowerCase();
            allNotes = allNotes.stream()
                    .filter(note -> note.getTags() != null && note.getTags().stream()
                            .anyMatch(tagItem -> tagItem.toLowerCase().equals(tagLower)))
                    .collect(Collectors.toList());
        }

        List<Note> pinnedNotes = allNotes.stream()
                .filter(Note::isPinned)
                .collect(Collectors.toList());

        List<Note> unpinnedNotes = allNotes.stream()
                .filter(note -> !note.isPinned())
                .collect(Collectors.toList());

        Comparator<Note> comparator = getNoteComparator(sortBy);
        pinnedNotes.sort(comparator);
        unpinnedNotes.sort(comparator);

        List<NoteResponse> finalNotes = new ArrayList<>();
        finalNotes.addAll(pinnedNotes.stream()
                .map(note -> convertToNoteResponse(note, !note.getCreatedBy().equals(userId)))
                .toList());
        finalNotes.addAll(unpinnedNotes.stream()
                .map(note -> convertToNoteResponse(note, !note.getCreatedBy().equals(userId)))
                .toList());

        return finalNotes;
    }

    private static Comparator<Note> getNoteComparator(String sortBy) {
        Comparator<Note> comparator = Comparator.comparing(Note::getLastEdited, Comparator.nullsLast(Comparator.naturalOrder())).reversed();

        if (sortBy != null) {
            comparator = switch (sortBy) {
                case "createdAt" ->
                        Comparator.comparing(Note::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                case "title" ->
                        Comparator.comparing(Note::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                case "lastEdited" ->
                        Comparator.comparing(Note::getLastEdited, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                default -> comparator;
            };
        }
        return comparator;
    }

    @Override
    public void togglePin(String userId, String noteId) {
        userRepository.findById(userId).ifPresent(user -> {
            noteRepository.findById(noteId).ifPresent(note -> {
                boolean willPin = !note.isPinned();

                if (willPin && user.getPinnedNoteIds().size() >= 20) {
                    throw new RuntimeException("Cannot pin more than 20 notes");
                }

                note.setPinned(willPin);
                note.setLastEdited(new Date());
                noteRepository.save(note);

                if (willPin) {
                    user.getPinnedNoteIds().add(noteId);
                } else {
                    user.getPinnedNoteIds().remove(noteId);
                }
                userRepository.save(user);
            });
        });
    }

    @Override
    public void toggleArchive(String userId, String noteId) {
        userRepository.findById(userId).ifPresent(user -> {
            noteRepository.findById(noteId).ifPresent(note -> {
                boolean willArchive = !note.isArchived();

                note.setArchived(willArchive);
                note.setLastEdited(new Date());
                noteRepository.save(note);

                if (willArchive) {
                    user.getArchivedNoteIds().add(noteId);
                    user.getPinnedNoteIds().remove(noteId);
                } else {
                    user.getArchivedNoteIds().remove(noteId);
                }
                userRepository.save(user);
            });
        });
    }

    @Override
    public List<NoteResponse> getArchivedNotes(String userId) {
        return userRepository.findById(userId)
                .map(user -> user.getArchivedNoteIds().stream()
                        .map(noteRepository::findById)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(note -> convertToNoteResponse(note, !note.getCreatedBy().equals(userId)))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<User> getSharedUsers(String noteId) {
        Optional<Note> noteOpt = noteRepository.findById(noteId);

        if (noteOpt.isEmpty() || noteOpt.get().getSharedWith() == null) {
            return Collections.emptyList();
        }

        List<String> sharedWithIds = noteOpt.get().getSharedWith();

        return sharedWithIds.stream()
                .map(userRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private NoteResponse convertToNoteResponse(Note note, boolean isShared) {
        return NoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .tags(note.getTags())
                .createdBy(note.getCreatedBy())
                .createdUser(note.getCreatedUser())
                .createdAt(note.getCreatedAt())
                .lastEdited(note.getLastEdited())
                .pinned(note.isPinned())
                .archived(note.isArchived())
                .shared(isShared)
                .build();
    }
}