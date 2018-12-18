package com.paw.bettertrello.models;

import com.paw.bettertrello.repositories.util.CascadeSave;
import com.sun.org.apache.xpath.internal.operations.Bool;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "boards")
public class Board {
    @Id
    @ApiModelProperty(notes = "The auto-generated ID of the Board")
    String id;
    @ApiModelProperty(notes = "The name of the Board")
    String name;
    String color;
    List<String> ownerUsernames;
    boolean isArchived;
    @DBRef
    @CascadeSave
    List<ActivityData> activities;
    @DBRef
    @CascadeSave
    @ApiModelProperty(notes = "List of CardLists")
    List<CardList> cardLists;
}