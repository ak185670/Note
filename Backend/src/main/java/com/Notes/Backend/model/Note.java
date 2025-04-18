package com.Notes.Backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection="notes")
public class Note {
    @Id
    private String id;
    private String title;
    private String content;
    private String tag;
    private String createdBy;
    private Map<String,AccessType> sharedWith=new HashMap<>();
    private Date createdAt =new Date();
    private Date lastEdited = new Date();



}
