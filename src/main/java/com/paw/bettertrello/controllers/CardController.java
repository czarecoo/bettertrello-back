package com.paw.bettertrello.controllers;

import com.paw.bettertrello.models.ActivityData;
import com.paw.bettertrello.models.Board;
import com.paw.bettertrello.models.Card;
import com.paw.bettertrello.repositories.BoardRepository;
import com.paw.bettertrello.repositories.CardRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.AbstractMap;
import java.util.List;
import java.util.Optional;

@RestController
@Api(description="Operations pertaining to cards in application")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class CardController {

    @Autowired
    CardRepository cardRepository;
    @Autowired
    BoardRepository boardRepository;
    @Autowired
    BoardController boardController;

    //TODO
    /*
    @ApiOperation(value = "Add an activity to card",response = Board.class)
    @RequestMapping(method= RequestMethod.POST, value="/cards/{id}/activities")
    public ResponseEntity<?> postActivityToCard(@RequestBody ActivityData activityData, @PathVariable String id, Principal principal) {
        String username = principal.getName();
        Optional<Card> optionalCard = cardRepository.findById(id);
        if (optionalCard.isPresent()) {
            Card card = optionalCard.get();
            ResponseEntity<?> authorizationCheckResult = checkAuthorization(username, card, OkStatusBodyContent.EMPTY);
            if (authorizationCheckResult.getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResult;
            }
            if (card.getActivities() == null) {
                card.setActivities(new ArrayList<>());
            }
            activityData.setParentBoardId(card.getParentBoardId());
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
    */

    @RequestMapping(method=RequestMethod.PATCH, value="/cards/{id}")
    public ResponseEntity<?> patchCard(@PathVariable String id, @RequestBody Card patchData, Principal principal) {

        String username = principal.getName();

        Optional<Card> optionalCard;

        if (!(patchData.getId() == null || patchData.getId().isEmpty())) {
            if (!patchData.getId().equals(id)) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            optionalCard = cardRepository.findById(patchData.getId());
        }
        else {
            optionalCard = cardRepository.findById(id);
        }

        if (optionalCard.isPresent()) {
            Card foundCard = optionalCard.get();
            AbstractMap.SimpleEntry<ResponseEntity<?>, Board> authorizationCheckResult = checkAuthorization(username, foundCard, CardController.OkStatusBodyContent.EMPTY);
            if (authorizationCheckResult.getKey().getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResult.getKey();
            }

            //Handle list of ActivityData from patchData (add date and user to the last element that we suppose it was appended)
            if (patchData.getActivities() != null) {
                handlePatchingActivityData(patchData, username);
            }

            foundCard = ControllerUtils.patchObject(foundCard, patchData);

            //Add the last activity to board-----------------------------------------
            ActivityData activityData = BoardController.prepareCommentCreationActivity(foundCard.getActivities().get(foundCard.getActivities().size() - 1), foundCard.getName());
            boardController.addActivityToBoard(authorizationCheckResult.getValue(), activityData);
            //-----------------------------------------------------------------------

            return new ResponseEntity<>(cardRepository.save(foundCard), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Delete a card")
    @RequestMapping(method=RequestMethod.DELETE, value="/cards/{id}")
    public String deleteCard(@PathVariable String id) {
        Optional<Card> optionalCard = cardRepository.findById(id);
        Card card = optionalCard.get();
        cardRepository.delete(card);

        return "";
    }

    private enum OkStatusBodyContent {
        EMPTY,
        CARD
    }

    //Returns pair of ResponseEntity (key) and parent board of object (value)
    private AbstractMap.SimpleEntry<ResponseEntity<?>, Board> checkAuthorization(String username, Card card, CardController.OkStatusBodyContent bodyContent) {
        if (card.getParentBoardId() == null || card.getParentBoardId().isEmpty()) {
            return new AbstractMap.SimpleEntry<>(new ResponseEntity<>("Card does not contain parent board ID", HttpStatus.BAD_REQUEST), null);
        }
        Optional<Board> optionalBoard = boardRepository.findById(card.getParentBoardId());
        if (optionalBoard.isPresent()) {
            Board board = optionalBoard.get();
            if (board.getOwnerUsernames().contains(username)) {
                switch (bodyContent) {
                    case EMPTY:
                        return new AbstractMap.SimpleEntry<>(new ResponseEntity<>(HttpStatus.OK), board);
                    case CARD:
                        return new AbstractMap.SimpleEntry<>(new ResponseEntity<>(card, HttpStatus.OK), board);
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

    private Card handlePatchingActivityData(Card patchData, String username) {
        List<ActivityData> activities = patchData.getActivities();
        ActivityData lastActivity = patchData.getActivities().get(activities.size() - 1);
        if (lastActivity.getOwnerUsername() == null || lastActivity.getOwnerUsername().isEmpty()) {
            lastActivity.setOwnerUsername(username);
        }
        if (lastActivity.getDate() == null || lastActivity.getDate().isEmpty()) {
            lastActivity.setDate(ControllerUtils.getCurrentDate());
        }
        if (lastActivity.getData() == null) {
            lastActivity.setData("");
        }
        lastActivity.setEditable(true);
        return patchData;
    }
}
