package com.paw.bettertrello.models;

import com.paw.bettertrello.repositories.util.CascadeSave;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "cards")
public class Card {
    @Id
    @ApiModelProperty(notes = "The auto-generated ID of the Card")
    String id;
    @ApiModelProperty(notes = "The name of the Card")
    String name;
    String description;
    @DBRef
    @CascadeSave
    List<ActivityData> activities;
    String parentBoardId;
}
