package com.paw.bettertrello.models;

import com.paw.bettertrello.repositories.util.CascadeSave;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Document(collection = "boards")
public class Board {
    @Id
    @ApiModelProperty(notes = "The auto-generated ID of the Board")
    String id;
    @ApiModelProperty(notes = "The name of the Board")
    String name;
    @ApiModelProperty(notes = "Color layout for board icon")
    String color;
    @ApiModelProperty(notes = "Collection describing users and their permisions on board")
    Map<String, BoardAuthority> userPermissionsMap;
    @ApiModelProperty(notes = "Flag describing if board is archived")
    Boolean isArchived;
    @DBRef
    @CascadeSave
    @ApiModelProperty(notes = "Collections on activietes done on board")
    List<ActivityData> activities;
    @DBRef
    @CascadeSave
    @ApiModelProperty(notes = "List of CardLists")
    List<CardList> cardLists;
}