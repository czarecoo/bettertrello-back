package com.paw.bettertrello.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "cards")
public class Card {
    @Id
    String id;
    String name;
}
