package com.Notes.Backend.repository;

import com.Notes.Backend.model.Note;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.*;

public interface NoteRepository extends MongoRepository<Note,String> {


    List<Note> findAllByCreatedBy(String userId);

    List<Note> findAllBySharedWithContainingKey(String userId);




}
