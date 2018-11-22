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
    boolean isEditable; //Activity is not editable by default
    String parentBoardId;
}
