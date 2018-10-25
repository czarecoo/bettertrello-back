package com.paw.bettertrello.controllers;


import com.paw.bettertrello.models.Board;
import com.paw.bettertrello.models.Card;
import com.paw.bettertrello.models.CardList;
import com.paw.bettertrello.repositories.CardListRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;

@RestController
@Api(description="Operations pertaining to lists of cards in application")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class CardListController {

    @Autowired
    CardListRepository cardListRepository;



    @ApiOperation(value = "Search a List with an ID",response = Optional.class)
    @RequestMapping(method=RequestMethod.GET, value="/lists/{id}")
    public Optional<CardList> getList(@PathVariable String id) {
        Optional<CardList> optionalCardList = cardListRepository.findById(id);
        return optionalCardList;
    }

    @ApiOperation(value = "Search a list with an ID, a get's cards from there",response = Optional.class)
    @RequestMapping(method=RequestMethod.GET, value="/lists/{id}/cards")
    public Iterable<Card> getCardsFromList(@PathVariable String id) {
        Optional<CardList> optionalCardList = cardListRepository.findById(id);
        return optionalCardList.get().getCards();
    }

    @ApiOperation(value = "Add a Card to List",response = Board.class)
    @RequestMapping(method=RequestMethod.POST, value="/lists/{id}/cards")
    public CardList postCardToList(@RequestBody Card card, @PathVariable String id) {
        Optional<CardList> optionalCardList = cardListRepository.findById(id);
        if(optionalCardList.isPresent()) {
            CardList cardList = optionalCardList.get();
            if (cardList.getCards() == null) {
                cardList.setCards(new ArrayList<Card>());
            }
            cardList.getCards().add(card);
            return cardListRepository.save(cardList);
        }
        else {
            return null;
        }
    }

    @ApiOperation(value = "Update a list")
    @RequestMapping(method=RequestMethod.PUT, value="/lists/{id}")
    public ResponseEntity<?> updateList(@PathVariable String id, @RequestBody CardList cardList) {

        Optional<CardList> optionalCardList;

        if (!(cardList.getId() == null || cardList.getId().isEmpty())) {
            if (!cardList.getId().equals(id)) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }

        optionalCardList = cardListRepository.findById(id);

        if (optionalCardList.isPresent()) {
            CardList foundCardList = optionalCardList.get();
            if(foundCardList.equals(cardList)) {
                return new ResponseEntity<>(null, HttpStatus.FOUND);
            }else {
                return new ResponseEntity<>(cardListRepository.save(cardList), HttpStatus.OK);
            }
        }
        else {
            return new ResponseEntity<>(cardListRepository.save(cardList), HttpStatus.CREATED);
        }

    }

    @ApiOperation(value = "Delete a list")
    @RequestMapping(method=RequestMethod.DELETE, value="/lists/{id}")
    public String deleteList(@PathVariable String id) {
        Optional<CardList> optionalCardList = cardListRepository.findById(id);
        CardList cardList = optionalCardList.get();
        cardListRepository.delete(cardList);

        return "";
    }

}
