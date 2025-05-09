package com.Notes.Backend.dto;

import lombok.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoteResponse {
    private String id;
    private String title;
    private String content;
    private List<String> tags;
    private String createdBy;
    private String createdUser;
    private boolean pinned;
    private boolean archived;
    private Date createdAt;
    private Date lastEdited;
    private boolean shared;
}