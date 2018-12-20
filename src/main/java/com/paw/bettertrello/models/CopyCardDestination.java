package com.paw.bettertrello.models;


import lombok.Data;

@Data
public class CopyCardDestination {
    String newName;
    String listId;
    int listPosition;
}
