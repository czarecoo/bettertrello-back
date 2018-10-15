package com.paw.bettertrello.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "boards")
public class Board {
    @Id
    String id;
    String name;
    ArrayList<CardList> cardLists;

    public Board() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<CardList> getCardLists() {
        return cardLists;
    }

    public void setCardLists(ArrayList<CardList> cardLists) {
        this.cardLists = cardLists;
    }
}
