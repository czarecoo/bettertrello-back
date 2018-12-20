package com.paw.bettertrello.models;

import com.paw.bettertrello.repositories.util.CascadeSave;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Document(collection = "cards")
public class Card {
    @Id
    @ApiModelProperty(notes = "The auto-generated ID of the Card")
    String id;
    @ApiModelProperty(notes = "The name of the Card")
    String name;
    @ApiModelProperty(notes = "Username of person whose created this card")
    String ownerUsername;
    @ApiModelProperty(notes = "Describes more about the task from this card")
    String description;
    @ApiModelProperty(notes = "Flag describing if card is archived")
    boolean isArchived;
    @ApiModelProperty(notes = "Date in which the task must be done")
    String cardDeadlineDate;
    @ApiModelProperty(notes = "Collection of usernames whose will get notification about new comment or different activity")
    Set<String> observerUserNames;
    @DBRef
    @CascadeSave
    @ApiModelProperty(notes = "Collections on activietes done on card")
    List<ActivityData> activities;
    @DBRef
    @CascadeSave
    @ApiModelProperty(notes = "Collection of smaller tasks to card")
    List<CheckListItem> checkListItems;
    @ApiModelProperty(notes = "Id of board in which we got this card")
    String parentBoardId;
}
