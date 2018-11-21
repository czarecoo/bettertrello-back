package com.paw.bettertrello.controllers;

import com.paw.bettertrello.models.Board;
import com.paw.bettertrello.models.Card;
import com.paw.bettertrello.models.CardList;
import com.paw.bettertrello.repositories.BoardRepository;
import com.paw.bettertrello.repositories.CardRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.security.Principal;
import java.util.Optional;

@RestController
@Api(description="Operations pertaining to cards in application")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class CardController {

    @Autowired
    CardRepository cardRepository;
    @Autowired
    BoardRepository boardRepository;

    @RequestMapping(method=RequestMethod.PATCH, value="/lists/{id}")
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
            ResponseEntity<?> authorizationCheckResult = checkAuthorization(username, foundCard, CardController.OkStatusBodyContent.EMPTY);
            if (authorizationCheckResult.getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResult;
            }
            return new ResponseEntity<>(cardRepository.save(ControllerUtils.patchObject(foundCard, patchData)), HttpStatus.OK);
        }
        return null;
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

    private ResponseEntity<?> checkAuthorization(String username, Card card, CardController.OkStatusBodyContent bodyContent) {
        if (card.getParentBoardId() == null || card.getParentBoardId().isEmpty()) {
            return new ResponseEntity<>("List does not contain parent board ID", HttpStatus.BAD_REQUEST);
        }
        Optional<Board> optionalBoard = boardRepository.findById(card.getParentBoardId());
        if (optionalBoard.isPresent()) {
            Board board = optionalBoard.get();
            if (board.getOwnerUsernames().contains(username)) {
                switch (bodyContent) {
                    case EMPTY:
                        return new ResponseEntity<>(HttpStatus.OK);
                    case CARD:
                        return new ResponseEntity<>(card, HttpStatus.OK);
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
