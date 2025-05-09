package com.Notes.Backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection="users")
public class User {
    @Id
    private String id;
    private String username;
    private String email;
    private String password;
    private Set<String> noteIds = new HashSet<>();
    private Set<String> sharedNoteIds = new HashSet<>();
    private Set<String> pinnedNoteIds = new HashSet<>();
    private Set<String> archivedNoteIds = new HashSet<>();
    private Long createdAt = System.currentTimeMillis();

}
