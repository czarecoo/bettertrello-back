package com.paw.bettertrello.controllers;

import com.paw.bettertrello.models.*;
import com.paw.bettertrello.repositories.BoardRepository;
import com.paw.bettertrello.repositories.CardListRepository;
import com.paw.bettertrello.repositories.CardRepository;
import com.paw.bettertrello.repositories.UserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@Api(description="Operations pertaining to cards in application")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class CardController {

    @Autowired
    CardRepository cardRepository;
    @Autowired
    CardListRepository cardListRepository;
    @Autowired
    CardListController cardListController;
    @Autowired
    BoardRepository boardRepository;
    @Autowired
    BoardController boardController;
    @Autowired
    UserRepository userRepository;


    @ApiOperation(value = "Add an checklist item to card",response = Board.class)
    @RequestMapping(method= RequestMethod.POST, value="/cards/{id}/checklist")
    public ResponseEntity<?> postCheckListItemToCard(@RequestBody CheckListItem checkListItem, @PathVariable String id, Principal principal) {
        String username = principal.getName();
        Optional<Card> optionalCard = cardRepository.findById(id);
        if (optionalCard.isPresent()) {
            Card card = optionalCard.get();
            AbstractMap.SimpleEntry<ResponseEntity<?>, Board> authorizationCheckResult = checkAuthorization(username, card, OkStatusBodyContent.EMPTY);
            if (authorizationCheckResult.getKey().getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResult.getKey();
            }
            if (card.getCheckListItems() == null) {
                card.setCheckListItems(new ArrayList<>());
            }
            checkListItem.setParentBoardId(card.getParentBoardId());
            checkListItem.setParentCardId(card.getId());
            card.getCheckListItems().add(checkListItem);

            ActivityData activityData = prepareCheckListCreationActivity(card, checkListItem, username);
            //Add card creation info to board------------------------------------
            boardController.addActivityToBoard(authorizationCheckResult.getValue(), activityData);
            //-------------------------------------------------------------------
            //Add card creation info to created card
            card.getActivities().add(activityData);
            //-------------------------------------------------------------------

            return new ResponseEntity<>(cardRepository.save(card), HttpStatus.CREATED);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(method=RequestMethod.POST, value="/cards/{id}/copy")
    public ResponseEntity<?> copyCardToList(@RequestBody CopyCardDestination copyCardDestination, @PathVariable String id, Principal principal){
        String username = principal.getName();
        Optional<Card> optionalCard = cardRepository.findById(id);
        Optional<CardList> optionalCardListToPaste = cardListRepository.findById(copyCardDestination.listId);
        if (optionalCard.isPresent() && optionalCardListToPaste.isPresent()) {
            Card card = optionalCard.get();
            CardList cardListToPaste = optionalCardListToPaste.get();
            AbstractMap.SimpleEntry<ResponseEntity<?>, Board> authorizationCheckResultForCard = checkAuthorization(username, card, OkStatusBodyContent.EMPTY);
            AbstractMap.SimpleEntry<ResponseEntity<?>, Board> authorizationCheckResultForCardList = cardListController.checkAuthorization(username, cardListToPaste, CardListController.OkStatusBodyContent.EMPTY);
            if (authorizationCheckResultForCard.getKey().getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResultForCard.getKey();
            }
            else if(authorizationCheckResultForCardList.getKey().getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResultForCardList.getKey();
            }
            card.setParentBoardId(cardListToPaste.getParentBoardId());
            card.setId(null);
            if(copyCardDestination.listPosition<0 || copyCardDestination.listPosition>cardListToPaste.getCards().size())
                return new ResponseEntity<>("Selected list position is out of bounds of selected list", HttpStatus.BAD_REQUEST);
            else{
                cardListToPaste.getCards().add(copyCardDestination.listPosition,card);
                return new ResponseEntity<>(cardListRepository.save(cardListToPaste), HttpStatus.CREATED);
            }
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Add an activity to card",response = Board.class)
    @RequestMapping(method= RequestMethod.POST, value="/cards/{id}/activities")
    public ResponseEntity<?> postActivityToCard(@RequestBody ActivityData activityData, @PathVariable String id, Principal principal) {
        String username = principal.getName();
        Optional<Card> optionalCard = cardRepository.findById(id);
        if (optionalCard.isPresent()) {
            Card card = optionalCard.get();
            AbstractMap.SimpleEntry<ResponseEntity<?>, Board> authorizationCheckResult = checkAuthorization(username, card, OkStatusBodyContent.EMPTY);
            if (authorizationCheckResult.getKey().getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResult.getKey();
            }
            if (card.getActivities() == null) {
                card.setActivities(new ArrayList<>());
            }
            activityData.setParentBoardId(card.getParentBoardId());
            activityData.setParentCardId(card.getId());
            card.getActivities().add(activityData);

            for(Iterator<String> iterator = card.getObserverUserNames().iterator();iterator.hasNext();){
                Optional<User> optionalUser = userRepository.findByUsername(iterator.next());
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    if(user.getUsername().equals(card.getOwnerUsername())){
                        continue;
                    }
                    activityData.setId(null);
                    user.getNotifications().add(activityData);
                }
                else{
                    iterator.remove();
                }
            }

            return new ResponseEntity<>(cardRepository.save(card), HttpStatus.CREATED);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(method=RequestMethod.POST, value="/cards/{id}/observe")
    public ResponseEntity<?> observeCard(@PathVariable String id, Principal principal) {
        String username = principal.getName();
        Optional<Card> optionalCard = cardRepository.findById(id);
        if (optionalCard.isPresent()) {
            Card card = optionalCard.get();
            AbstractMap.SimpleEntry<ResponseEntity<?>, Board> authorizationCheckResult = checkAuthorization(username, card, OkStatusBodyContent.EMPTY);
            if (authorizationCheckResult.getKey().getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResult.getKey();
            }
            if(card.getObserverUserNames() == null){
                card.setObserverUserNames(new HashSet<>());
            }
            card.getObserverUserNames().add(username);
            return new ResponseEntity<>(cardRepository.save(card), HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(method=RequestMethod.POST, value="/cards/{id}/unobserve")
    public ResponseEntity<?> unObserveCard(@PathVariable String id, Principal principal) {
        String username = principal.getName();
        Optional<Card> optionalCard = cardRepository.findById(id);
        if (optionalCard.isPresent()) {
            Card card = optionalCard.get();
            AbstractMap.SimpleEntry<ResponseEntity<?>, Board> authorizationCheckResult = checkAuthorization(username, card, OkStatusBodyContent.EMPTY);
            if (authorizationCheckResult.getKey().getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResult.getKey();
            }
            if(card.getObserverUserNames() == null){
                return new ResponseEntity<>("Can not unobserve an unobserved card", HttpStatus.BAD_REQUEST);
            }
            card.getObserverUserNames().remove(username);
            return new ResponseEntity<>(cardRepository.save(card), HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

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

    public static ActivityData prepareCheckListCreationActivity(Card card, CheckListItem checkListItem, String username) {
        ActivityData activityData = new ActivityData();
        activityData.setOwnerUsername(username);
        activityData.setData(" added checklist item " + checkListItem.getData() + " to " + card.getName());
        activityData.setDate(ControllerUtils.getCurrentDate());
        return activityData;
    }

    private class CopyCardDestination {
        String listId;
        int listPosition;
    }
}
