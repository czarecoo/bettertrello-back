package com.paw.bettertrello.models;

import com.paw.bettertrello.repositories.util.CascadeSave;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "cardLists")
public class CardList {
    @Id
    @ApiModelProperty(notes = "The auto-generated ID of the CardList")
    String id;
    @ApiModelProperty(notes = "The name of the CardList")
    String name;
    @ApiModelProperty(notes = "Id of board in which we got this cardlist")
    String parentBoardId;
    @DBRef
    @CascadeSave
    @ApiModelProperty(notes = "List of Cards")
    List<Card> cards;
}
