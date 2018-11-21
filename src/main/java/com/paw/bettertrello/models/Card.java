package com.paw.bettertrello.models;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "cards")
public class Card {
    @Id
    @ApiModelProperty(notes = "The auto-generated ID of the Card")
    String id;
    @ApiModelProperty(notes = "The name of the Card")
    String name;
    String parentBoardId;
}
