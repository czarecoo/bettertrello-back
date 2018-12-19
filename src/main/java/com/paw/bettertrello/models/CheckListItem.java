package com.paw.bettertrello.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "checkListItems")
public class CheckListItem {
    @Id
    String id;
    String data;
    boolean isDone;
    String parentBoardId;
}
