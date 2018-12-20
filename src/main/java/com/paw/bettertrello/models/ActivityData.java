package com.paw.bettertrello.models;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

//Class used by both global activities (seen from board) and card's comments
@Data
@Document(collection = "activities")
public class ActivityData {
    @Id
    @ApiModelProperty(notes = "The auto-generated ID of the activity")
    String id;
    @ApiModelProperty(notes = "Username of person whose created this activity")
    String ownerUsername;
    @ApiModelProperty(notes = "Data which describes done activity")
    String data;
    @ApiModelProperty(notes = "Date of creation of this activity")
    String date;
    @ApiModelProperty(notes = "Describes if activity can be edited")
    Boolean isEditable; //Activity is not editable by default
    @ApiModelProperty(notes = "Marks activity as edited if it was edited (and can be edited)")
    Boolean isEdited; //Marks activity as edited if it was edited (and can be edited)
    @ApiModelProperty(notes = "Id of board in which we got this activity")
    String parentBoardId;
    @ApiModelProperty(notes = "Id of card in which we got this activity")
    String parentCardId; //To remove dbref from parent card when activitydata is removed
}
