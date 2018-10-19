package com.paw.bettertrello.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "boards")
public class Board {
    @Id
    String id;
    String name;
    @DBRef
    List<CardList> cardLists;
}
