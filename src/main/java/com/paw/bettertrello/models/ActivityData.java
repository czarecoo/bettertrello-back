package com.paw.bettertrello.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

//Class used by both global activities (seen from board) and card's comments
@Data
@Document(collection = "activities")
public class ActivityData {
    @Id
    String id;
    String ownerUsername;
    String data;
    String date;
    Boolean isEditable; //Activity is not editable by default
    Boolean isEdited; //Marks activity as edited if it was edited (and can be edited)
    String parentBoardId;
    String parentCardId; //To remove dbref from parent card when activitydata is removed
}
