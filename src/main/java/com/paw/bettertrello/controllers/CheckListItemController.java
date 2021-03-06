package com.paw.bettertrello.controllers;

import com.paw.bettertrello.models.Board;
import com.paw.bettertrello.models.Card;
import com.paw.bettertrello.models.CheckListItem;
import com.paw.bettertrello.repositories.BoardRepository;
import com.paw.bettertrello.repositories.CardRepository;
import com.paw.bettertrello.repositories.CheckListItemRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.AbstractMap;
import java.util.Optional;

@RestController
@Api(description="Operations pertaining to checklistitems in application")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class CheckListItemController {

    @Autowired
    CheckListItemRepository checkListItemRepository;
    @Autowired
    CardRepository cardRepository;
    @Autowired
    BoardRepository boardRepository;

    @ApiOperation(value = "Updates checklistitem")
    @RequestMapping(method = RequestMethod.PATCH, value = "/checklistitems/{id}")
    public ResponseEntity<?> patchCheckListItem(@PathVariable String id, @RequestBody CheckListItem patchData, Principal principal) {
        String username = principal.getName();

        Optional<CheckListItem> optionalCheckListItem;

        if (!(patchData.getId() == null || patchData.getId().isEmpty())) {
            if (!patchData.getId().equals(id)) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            optionalCheckListItem = checkListItemRepository.findById(patchData.getId());
        } else {
            optionalCheckListItem = checkListItemRepository.findById(id);
        }

        if (optionalCheckListItem.isPresent()) {
            CheckListItem foundCheckListItem = optionalCheckListItem.get();
            AbstractMap.SimpleEntry<ResponseEntity<?>, Board> authorizationCheckResult = checkAuthorization(username, foundCheckListItem);
            if (authorizationCheckResult.getKey().getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResult.getKey();
            }

            foundCheckListItem = ControllerUtils.patchObject(foundCheckListItem, patchData);

            return new ResponseEntity<>(checkListItemRepository.save(foundCheckListItem), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Delete a checklistitem")
    @RequestMapping(method = RequestMethod.DELETE, value = "/checklistitems/{id}")
    public ResponseEntity<?> deleteCheckListItem(@PathVariable String id, Principal principal) {
        String username = principal.getName();

        Optional<CheckListItem> optionalCheckListItem = checkListItemRepository.findById(id);
        if (optionalCheckListItem.isPresent()) {
            CheckListItem foundCheckListItem = optionalCheckListItem.get();
            AbstractMap.SimpleEntry<ResponseEntity<?>, Board> authorizationCheckResult = checkAuthorization(username, foundCheckListItem);
            if (authorizationCheckResult.getKey().getStatusCode() != HttpStatus.OK) {
                return authorizationCheckResult.getKey();
            }

            //REMOVE DBREF FROM PARENT CARD
            Optional<Card> parentCard = cardRepository.findById(foundCheckListItem.getParentCardId());
            if (!parentCard.isPresent()) {
                return new ResponseEntity<>("Parent card not found", HttpStatus.BAD_REQUEST);
            }
            else {
                Card card = parentCard.get();
                card.getCheckListItems().removeIf(a -> a.getId().equals(foundCheckListItem.getId()));
                cardRepository.save(card);
            }

            checkListItemRepository.delete(foundCheckListItem);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private AbstractMap.SimpleEntry<ResponseEntity<?>, Board> checkAuthorization(String username, CheckListItem checkListItem) {
        if (checkListItem.getParentBoardId() == null || checkListItem.getParentBoardId().isEmpty()) {
            return new AbstractMap.SimpleEntry<>(new ResponseEntity<>("Checklist item does not contain parent board ID", HttpStatus.BAD_REQUEST), null);
        }
        Optional<Board> optionalBoard = boardRepository.findById(checkListItem.getParentBoardId());
        if (optionalBoard.isPresent()) {
            Board board = optionalBoard.get();
            if (board.getUserPermissionsMap().containsKey(username)) {
                return new AbstractMap.SimpleEntry<>(new ResponseEntity<>(HttpStatus.OK), board);
            }
            return new AbstractMap.SimpleEntry<>(new ResponseEntity<>(HttpStatus.UNAUTHORIZED), null);
        } else {
            return new AbstractMap.SimpleEntry<>(new ResponseEntity<>("Parent board not found", HttpStatus.BAD_REQUEST), null);
        }
    }
}