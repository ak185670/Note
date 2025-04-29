package com.Notes.Backend.model;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection="notes")
public class Note {
    @Id
    private String id;
    @NotBlank
    private String title;
    private String content;
    private List<String> tags;
    private String createdBy;
    private Map<String,AccessType> sharedWith=new HashMap<>();

    @CreatedDate
    private Date createdAt ;

    @LastModifiedDate
    private Date lastEdited ;

    private boolean isPinned=false;
    private boolean isArchived=false;


}
