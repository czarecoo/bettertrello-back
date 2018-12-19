package com.paw.bettertrello.controllers;

import com.paw.bettertrello.models.ActivityData;
import com.paw.bettertrello.models.Board;
import com.paw.bettertrello.models.Card;
import com.paw.bettertrello.models.CardList;
import com.paw.bettertrello.repositories.BoardRepository;
import com.paw.bettertrello.repositories.CardActivityDataRepository;
import com.paw.bettertrello.repositories.CardRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Optional;

@RestController
@Api(description="Operations pertaining to cards in application")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class CardActivityDataController {

    @Autowired
    CardActivityDataRepository cardActivityDataRepository;
    @Autowired
    CardRepository cardRepository;
    @Autowired
    BoardRepository boardRepository;

    @RequestMapping(method=RequestMethod.PATCH, value="/activities/{id}")
    public ResponseEntity<?> patchActivity(@PathVariable String id, @RequestBody ActivityData patchData, Principal principal) {
        String username = principal.getName();

        Optional<ActivityData> optionalActivityData;

        if (!(patchData.getId() == null || patchData.getId().isEmpty())) {
            if (!patchData.getId().equals(id)) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            optionalActivityData = cardActivityDataRepository.findById(patchData.getId());
        }
        else {
            optionalActivityData = cardActivityDataRepository.findById(id);
        }

        if (optionalActivityData.isPresent()) {
            ActivityData foundActivityData = optionalActivityData.get();
            AbstractMap.SimpleEntry<ResponseEntity<?>, Board> authorizationCheckResult = checkAuthorization(username, foundActivityData);
            if (authorizationCheckResult.getKey().getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResult.getKey();
            }

            //Check if an activity can be editable
            if (!foundActivityData.isEditable()) {
                return new ResponseEntity<>(HttpStatus.LOCKED);
            }

            foundActivityData = ControllerUtils.patchObject(foundActivityData, patchData);
            foundActivityData.setEdited(true);

            return new ResponseEntity<>(cardActivityDataRepository.save(foundActivityData), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(method=RequestMethod.DELETE, value="/activities/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable String id, Principal principal) {
        String username = principal.getName();

        Optional<ActivityData> optionalActivityData = cardActivityDataRepository.findById(id);
        if (optionalActivityData.isPresent()) {
            ActivityData foundActivityData = optionalActivityData.get();
            AbstractMap.SimpleEntry<ResponseEntity<?>, Board> authorizationCheckResult = checkAuthorization(username, foundActivityData);
            if (authorizationCheckResult.getKey().getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResult.getKey();
            }

            //REMOVE DBREF FROM PARENT CARD
            Optional<Card> parentCard = cardRepository.findById(foundActivityData.getParentCardId());
            if (!parentCard.isPresent()) {
                return new ResponseEntity<>("Parent card not found", HttpStatus.BAD_REQUEST);
            }
            else {
                Card card = parentCard.get();
                card.getActivities().remove(foundActivityData);
            }

            cardActivityDataRepository.delete(foundActivityData);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private AbstractMap.SimpleEntry<ResponseEntity<?>, Board> checkAuthorization(String username, ActivityData activityData) {
        if (!activityData.getOwnerUsername().equals(username)) {
            return new AbstractMap.SimpleEntry<>(new ResponseEntity<>(HttpStatus.UNAUTHORIZED), null);
        }
        if (activityData.getParentBoardId() == null || activityData.getParentBoardId().isEmpty()) {
            return new AbstractMap.SimpleEntry<>(new ResponseEntity<>("Activity does not contain parent board ID", HttpStatus.BAD_REQUEST), null);
        }
        Optional<Board> optionalBoard = boardRepository.findById(activityData.getParentBoardId());
        if (optionalBoard.isPresent()) {
            Board board = optionalBoard.get();
            if (board.getOwnerUsernames().contains(username)) {
                    return new AbstractMap.SimpleEntry<>(new ResponseEntity<>(HttpStatus.OK), board);
            }
            return new AbstractMap.SimpleEntry<>(new ResponseEntity<>(HttpStatus.UNAUTHORIZED), null);
        }
        else {
            return new AbstractMap.SimpleEntry<>(new ResponseEntity<>("Parent board not found", HttpStatus.BAD_REQUEST), null);
        }
    }
}

