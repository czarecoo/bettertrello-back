package com.paw.bettertrello.controllers;


import com.paw.bettertrello.models.ActivityData;
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

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Api(description="Operations pertaining to lists of cards in application")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class CardListController {

    @Autowired
    CardListRepository cardListRepository;
    @Autowired
    BoardRepository boardRepository;
    @Autowired
    BoardController boardController;

    @ApiOperation(value = "Search a List with an ID",response = Optional.class)
    @RequestMapping(method=RequestMethod.GET, value="/lists/{id}")
    public ResponseEntity<?> getList(@PathVariable String id, Principal principal) {
        String username = principal.getName();
        Optional<CardList> optionalCardList = cardListRepository.findById(id);
        if (optionalCardList.isPresent()) {
            CardList cardList = optionalCardList.get();
            return checkAuthorization(username, cardList, OkStatusBodyContent.CARDLIST).getKey();
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
            return checkAuthorization(username, cardList, OkStatusBodyContent.CARDS).getKey();
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
            AbstractMap.SimpleEntry<ResponseEntity<?>, Board> authorizationCheckResult = checkAuthorization(username, cardList, OkStatusBodyContent.EMPTY);
            if (authorizationCheckResult.getKey().getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResult.getKey();
            }
            if (cardList.getCards() == null) {
                cardList.setCards(new ArrayList<>());
            }
            card.setParentBoardId(cardList.getParentBoardId());
            if (card.getActivities() == null) {
                ActivityData activityData = prepareCardCreationActivity(card, cardList, username);
                //Add card creation info to board------------------------------------
                boardController.addActivityToBoard(authorizationCheckResult.getValue(), activityData);
                //-------------------------------------------------------------------
                //Add card creation info to created card
                card.setActivities(new ArrayList<>(Arrays.asList(activityData)));
                //-------------------------------------------------------------------
            }
            if(card.getObserverUserNames() == null){
                card.setObserverUserNames(new HashSet<>());
            }
            if(card.getOwnerUsername() == null){
                card.setOwnerUsername(username);
            }
            if (card.getCheckListItems() == null) {
                card.setCheckListItems(new ArrayList<>());
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
            AbstractMap.SimpleEntry<ResponseEntity<?>, Board> authorizationCheckResult = checkAuthorization(username, foundCardList, OkStatusBodyContent.EMPTY);
            if (authorizationCheckResult.getKey().getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResult.getKey();
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
            AbstractMap.SimpleEntry<ResponseEntity<?>, Board> authorizationCheckResult = checkAuthorization(username, foundCardList, OkStatusBodyContent.EMPTY);
            if (authorizationCheckResult.getKey().getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResult.getKey();
            }

            //Add list rename info to board----------------------------------------
            if (patchData.getName() != null) {
                boardController.addActivityToBoard(authorizationCheckResult.getValue(), BoardController.prepareListRenameActivity(patchData, foundCardList, username));
            }
            //---------------------------------------------------------------------

            return new ResponseEntity<>(cardListRepository.save(ControllerUtils.patchObject(foundCardList, patchData)), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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

    public enum OkStatusBodyContent {
        EMPTY,
        CARDLIST,
        CARDS
    }

    //Returns pair of ResponseEntity (key) and parent board of object (value)
    public AbstractMap.SimpleEntry<ResponseEntity<?>, Board> checkAuthorization(String username, CardList cardList, OkStatusBodyContent bodyContent) {
        if (cardList.getParentBoardId() == null || cardList.getParentBoardId().isEmpty()) {
            return new AbstractMap.SimpleEntry<>(new ResponseEntity<>("List does not contain parent board ID", HttpStatus.BAD_REQUEST), null);
        }
        Optional<Board> optionalBoard = boardRepository.findById(cardList.getParentBoardId());
        if (optionalBoard.isPresent()) {
            Board board = optionalBoard.get();
            if (board.getUserPermissionsMap().containsKey(username)) {
                switch (bodyContent) {
                    case EMPTY:
                        return new AbstractMap.SimpleEntry<>(new ResponseEntity<>(HttpStatus.OK), board);
                    case CARDLIST:
                        return new AbstractMap.SimpleEntry<>(new ResponseEntity<>(cardList, HttpStatus.OK), board);
                    case CARDS:
                        List<Card> cardsToShow = new ArrayList<>(cardList.getCards()).stream().filter(card -> !card.isArchived()).collect(Collectors.toList());
                        return new AbstractMap.SimpleEntry<>(new ResponseEntity<>(cardsToShow, HttpStatus.OK), board);
                    default:
                        throw new IllegalArgumentException();
                }
            }
            return new AbstractMap.SimpleEntry<>(new ResponseEntity<>(HttpStatus.UNAUTHORIZED), null);
        }
        else {
            return new AbstractMap.SimpleEntry<>(new ResponseEntity<>("Parent board not found", HttpStatus.BAD_REQUEST), null);
        }
    }

    public static ActivityData prepareCardCreationActivity(Card card, CardList cardList, String username) {
        ActivityData activityData = new ActivityData();
        activityData.setOwnerUsername(username);
        activityData.setData(" added card " + card.getName() + " to " + cardList.getName());
        activityData.setDate(ControllerUtils.getCurrentDate());
        return activityData;
    }
}
