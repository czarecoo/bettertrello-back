package com.paw.bettertrello.controllers;


import com.paw.bettertrello.models.Board;
import com.paw.bettertrello.models.Card;
import com.paw.bettertrello.models.CardList;
import com.paw.bettertrello.repositories.BoardRepository;
import com.paw.bettertrello.repositories.CardListRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Optional;

@RestController
@Api(description="Operations pertaining to lists of cards in application")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class CardListController {

    @Autowired
    CardListRepository cardListRepository;
    @Autowired
    BoardRepository boardRepository;

    @ApiOperation(value = "Search a List with an ID",response = Optional.class)
    @RequestMapping(method=RequestMethod.GET, value="/lists/{id}")
    public ResponseEntity<?> getList(@PathVariable String id, Principal principal) {
        String username = principal.getName();
        Optional<CardList> optionalCardList = cardListRepository.findById(id);
        if (optionalCardList.isPresent()) {
            CardList cardList = optionalCardList.get();
            return checkAuthorization(username, cardList, OkStatusBodyContent.CARDLIST);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Search a list with an ID, a get's cards from there",response = Optional.class)
    @RequestMapping(method=RequestMethod.GET, value="/lists/{id}/cards")
    public ResponseEntity<?> getCardsFromList(@PathVariable String id, Principal principal) {
        String username = principal.getName();
        Optional<CardList> optionalCardList = cardListRepository.findById(id);
        if (optionalCardList.isPresent()) {
            CardList cardList = optionalCardList.get();
            return checkAuthorization(username, cardList, OkStatusBodyContent.CARDS);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Add a Card to List",response = Board.class)
    @RequestMapping(method=RequestMethod.POST, value="/lists/{id}/cards")
    public ResponseEntity<?> postCardToList(@RequestBody Card card, @PathVariable String id, Principal principal) {
        String username = principal.getName();
        Optional<CardList> optionalCardList = cardListRepository.findById(id);
        if (optionalCardList.isPresent()) {
            CardList cardList = optionalCardList.get();
            ResponseEntity<?> authorizationCheckResult = checkAuthorization(username, cardList, OkStatusBodyContent.EMPTY);
            if (authorizationCheckResult.getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResult;
            }
            if (cardList.getCards() == null) {
                cardList.setCards(new ArrayList<>());
            }
            card.setParentBoardId(cardList.getParentBoardId());
            if (card.getActivities() == null) {
                card.setActivities(new ArrayList<>());
            }
            cardList.getCards().add(card);
            return new ResponseEntity<>(cardListRepository.save(cardList), HttpStatus.CREATED);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Update a list")
    @RequestMapping(method=RequestMethod.PUT, value="/lists/{id}")
    public ResponseEntity<?> putList(@PathVariable String id, @RequestBody CardList cardList, Principal principal) {
        String username = principal.getName();
        Optional<CardList> optionalCardList;

        if (!(cardList.getId() == null || cardList.getId().isEmpty())) {
            if (!cardList.getId().equals(id)) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            optionalCardList = cardListRepository.findById(cardList.getId());
        }
        else {
            optionalCardList = cardListRepository.findById(id);
        }

        if (optionalCardList.isPresent()) {
            CardList foundCardList = optionalCardList.get();
            ResponseEntity<?> authorizationCheckResult = checkAuthorization(username, foundCardList, OkStatusBodyContent.EMPTY);
            if (authorizationCheckResult.getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResult;
            }
            if (foundCardList.equals(cardList)) {
                return new ResponseEntity<>(HttpStatus.FOUND);
            }
            else {
                return new ResponseEntity<>(cardListRepository.save(cardList), HttpStatus.OK);
            }
        }
        else {
            return new ResponseEntity<>(cardListRepository.save(cardList), HttpStatus.CREATED);
        }

    }

    @RequestMapping(method=RequestMethod.PATCH, value="/lists/{id}")
    public ResponseEntity<?> patchList(@PathVariable String id, @RequestBody CardList patchData, Principal principal) {

        String username = principal.getName();

        Optional<CardList> optionalList;

        if (!(patchData.getId() == null || patchData.getId().isEmpty())) {
            if (!patchData.getId().equals(id)) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            optionalList = cardListRepository.findById(patchData.getId());
        }
        else {
            optionalList = cardListRepository.findById(id);
        }

        if (optionalList.isPresent()) {
            CardList foundCardList = optionalList.get();
            ResponseEntity<?> authorizationCheckResult = checkAuthorization(username, foundCardList, OkStatusBodyContent.EMPTY);
            if (authorizationCheckResult.getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResult;
            }
            return new ResponseEntity<>(cardListRepository.save(ControllerUtils.patchObject(foundCardList, patchData)), HttpStatus.OK);
        }
        return null;
    }

    @ApiOperation(value = "Delete a list")
    @RequestMapping(method=RequestMethod.DELETE, value="/lists/{id}")
    public String deleteList(@PathVariable String id) {
        Optional<CardList> optionalCardList = cardListRepository.findById(id);
        CardList cardList = optionalCardList.get();
        cardListRepository.delete(cardList);

        return "";
    }

    private enum OkStatusBodyContent {
        EMPTY,
        CARDLIST,
        CARDS
    }

    private ResponseEntity<?> checkAuthorization(String username, CardList cardList, OkStatusBodyContent bodyContent) {
        if (cardList.getParentBoardId() == null || cardList.getParentBoardId().isEmpty()) {
            return new ResponseEntity<>("List does not contain parent board ID", HttpStatus.BAD_REQUEST);
        }
        Optional<Board> optionalBoard = boardRepository.findById(cardList.getParentBoardId());
        if (optionalBoard.isPresent()) {
            Board board = optionalBoard.get();
            if (board.getOwnerUsernames().contains(username)) {
                switch (bodyContent) {
                    case EMPTY:
                        return new ResponseEntity<>(HttpStatus.OK);
                    case CARDLIST:
                        return new ResponseEntity<>(cardList, HttpStatus.OK);
                    case CARDS:
                        return new ResponseEntity<>(cardList.getCards(), HttpStatus.OK);
                    default:
                        throw new IllegalArgumentException();
                }
            }
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        else {
            return new ResponseEntity<>("Parent board not found", HttpStatus.BAD_REQUEST);
        }
    }
}
