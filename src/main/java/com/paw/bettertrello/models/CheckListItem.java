package com.paw.bettertrello.models;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "checkListItems")
public class CheckListItem {
    @Id
    @ApiModelProperty(notes = "The auto-generated ID of the checklistitem")
    String id;
    @ApiModelProperty(notes = "Data which describes done checklistitem")
    String data;
    @ApiModelProperty(notes = "Flag describing if checklistitem is done")
    boolean isDone;
    @ApiModelProperty(notes = "Id of board in which we got this checklistitem")
    String parentBoardId;
    @ApiModelProperty(notes = "Id of card in which we got this checklistitem")
    String parentCardId; //To remove dbref from parent card when checklistitem is removed
}
