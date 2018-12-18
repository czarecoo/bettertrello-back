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
    String parentBoardId;
    @DBRef
    @CascadeSave
    @ApiModelProperty(notes = "List of Cards")
    @Getter(AccessLevel.NONE)
    List<Card> cards;

    public  List<Card> getCards() {
        List<Card> cardsToShow = new ArrayList<>();
        for(Card card: cards) {
            if(!card.isArchived())
                cardsToShow.add(card);
        }
        return cardsToShow;
    }

}
