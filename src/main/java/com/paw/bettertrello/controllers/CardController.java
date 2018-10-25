package com.paw.bettertrello.controllers;

import com.paw.bettertrello.models.Card;
import com.paw.bettertrello.models.CardList;
import com.paw.bettertrello.repositories.CardRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@Api(description="Operations pertaining to cards in application")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class CardController {

    @Autowired
    CardRepository cardRepository;

    @ApiOperation(value = "Delete a card")
    @RequestMapping(method=RequestMethod.DELETE, value="/cards/{id}")
    public String deleteCard(@PathVariable String id) {
        Optional<Card> optionalCard = cardRepository.findById(id);
        Card card = optionalCard.get();
        cardRepository.delete(card);

        return "";
    }
}
