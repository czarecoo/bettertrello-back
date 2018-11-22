package com.paw.bettertrello.controllers;

import com.paw.bettertrello.models.ActivityData;
import com.paw.bettertrello.models.Board;
import com.paw.bettertrello.models.Card;
import com.paw.bettertrello.models.CardList;
import com.paw.bettertrello.repositories.BoardRepository;
import com.paw.bettertrello.repositories.CardListRepository;
import com.paw.bettertrello.repositories.CardRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;


@RestController
@Api(description="Operations pertaining to boards in application")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class BoardController {

    @Autowired
    BoardRepository boardRepository;

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved board")
    }
    )

    @ApiOperation(value = "Search all boards",response = Optional.class)
    @RequestMapping(method=RequestMethod.GET, value="/boards")
    public ResponseEntity<?> getBoards(Principal principal) {
        return new ResponseEntity<>(boardRepository.findAllByOwnerUsernamesContaining(principal.getName()), HttpStatus.OK);
    }

    @ApiOperation(value = "Search a board with an ID",response = Optional.class)
    @RequestMapping(method=RequestMethod.GET, value="/boards/{id}")
    public ResponseEntity<?> getBoard(@PathVariable String id, Principal principal) {
        String username = principal.getName();
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isPresent()) {
            Board board = optionalBoard.get();
            if (board.getOwnerUsernames().contains(username)) {
                return new ResponseEntity<>(board, HttpStatus.OK);
            }
            else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ApiOperation(value = "Search a board with an ID, a get's list from there",response = Iterable.class)
    @RequestMapping(method=RequestMethod.GET, value="/boards/{id}/lists")
    public ResponseEntity<?> getListsFromBoard(@PathVariable String id, Principal principal) {
        String username = principal.getName();
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isPresent()) {
            Board board = optionalBoard.get();
            if (board.getOwnerUsernames().contains(username)) {
                return new ResponseEntity<>(board.getCardLists(), HttpStatus.OK);
            }
            else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ApiOperation(value = "Add a board",response = Board.class)
    @RequestMapping(method=RequestMethod.POST, value="/boards")
    public ResponseEntity<?> postBoard(@RequestBody Board board, Principal principal) {

        String username = principal.getName();

        if (board.getOwnerUsernames() == null) {
            board.setOwnerUsernames(Arrays.asList(username));
        }
        else if (!board.getOwnerUsernames().contains(username)) {
            board.getOwnerUsernames().add(username);
        }
        if (board.getActivities() == null) {
            board.setActivities(new ArrayList<>());
        }
        return new ResponseEntity<>(boardRepository.save(board), HttpStatus.CREATED);
    }

    @ApiOperation(value = "Add a List to Board",response = Board.class)
    @RequestMapping(method=RequestMethod.POST, value="/boards/{id}/lists")
    public ResponseEntity<?> postListToBoard(@RequestBody CardList cardList, @PathVariable String id, Principal principal) {
        String username = principal.getName();
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if(optionalBoard.isPresent()) {
            Board board = optionalBoard.get();
            if (cardList.getParentBoardId() != null && !cardList.getParentBoardId().equals(id)) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            if (!board.getOwnerUsernames().contains(username)) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            if (board.getCardLists() == null) {
                board.setCardLists(new ArrayList<CardList>());
            }
            cardList.setParentBoardId(board.getId());
            //Add list creation info to board----------------------------------------
            addActivityToBoard(board, prepareListCreationActivity(cardList, username));
            //-----------------------------------------------------------------------
            board.getCardLists().add(cardList);
            return new ResponseEntity<>(boardRepository.save(board), HttpStatus.CREATED);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Update a board")
    @RequestMapping(method=RequestMethod.PUT, value="/boards/{id}")
    public ResponseEntity<?> putBoard(@PathVariable String id, @RequestBody Board board, Principal principal) {

        String username = principal.getName();

        if (!board.getOwnerUsernames().contains(username)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<Board> optionalBoard;

        if (!(board.getId() == null || board.getId().isEmpty())) {
            if (!board.getId().equals(id)) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            optionalBoard = boardRepository.findById(board.getId());
        }
        else {
            optionalBoard = boardRepository.findById(id);
        }

        if (optionalBoard.isPresent()) {
            Board foundBoard = optionalBoard.get();
            if(foundBoard.equals(board)) {
                return new ResponseEntity<>(null, HttpStatus.FOUND);
            }else {
                return new ResponseEntity<>(boardRepository.save(board), HttpStatus.OK);
            }
        }
        else {
            return new ResponseEntity<>(boardRepository.save(board), HttpStatus.CREATED);
        }

    }

    @RequestMapping(method=RequestMethod.PATCH, value="/boards/{id}")
    public ResponseEntity<?> patchBoard(@PathVariable String id, @RequestBody Board patchData, Principal principal) {

        String username = principal.getName();

        Optional<Board> optionalBoard;

        if (!(patchData.getId() == null || patchData.getId().isEmpty())) {
            if (!patchData.getId().equals(id)) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            optionalBoard = boardRepository.findById(patchData.getId());
        }
        else {
            optionalBoard = boardRepository.findById(id);
        }

        if (optionalBoard.isPresent()) {
            Board foundBoard = optionalBoard.get();
            if (!foundBoard.getOwnerUsernames().contains(username)) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            //Add board rename info to board----------------------------------------
            if (patchData.getName() != null) {
                addActivityToBoard(foundBoard, prepareBoardRenameActivity(patchData, foundBoard, username));
            }
            //---------------------------------------------------------------------
            return new ResponseEntity<>(boardRepository.save(ControllerUtils.patchObject(foundBoard, patchData)), HttpStatus.OK);
        }
        return null;
    }

    @ApiOperation(value = "Delete a board")
    @RequestMapping(method=RequestMethod.DELETE, value="/boards/{id}")
    public ResponseEntity<?> deleteBoard(@PathVariable String id, Principal principal) {
        String username = principal.getName();
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isPresent()) {
            Board board = optionalBoard.get();
            if (board.getOwnerUsernames().contains(username)) {
                boardRepository.delete(board);
                return new ResponseEntity<>(HttpStatus.OK);
            }
            else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    //Methods to generate activities shown solely on board's history. They can base on existing activities like comment activity of a card.

    public static ActivityData prepareCommentCreationActivity(ActivityData activityData, String cardName) {
        ActivityData boardActivityData = new ActivityData();
        boardActivityData.setOwnerUsername(activityData.getOwnerUsername());
        boardActivityData.setDate(activityData.getDate());
        boardActivityData.setEditable(false);
        boardActivityData.setData(" added comment on " + cardName + " \"" + activityData.getData() + "\"");
        return boardActivityData;
    }

    public static ActivityData prepareListCreationActivity(CardList cardList, String username) {
        ActivityData boardActivityData = new ActivityData();
        boardActivityData.setOwnerUsername(username);
        boardActivityData.setDate(ControllerUtils.getCurrentDate());
        boardActivityData.setEditable(false);
        boardActivityData.setData(" added list " + cardList.getName() + " to this board");
        return boardActivityData;
    }

    public static ActivityData prepareListRenameActivity(CardList newCardList, CardList oldCardList, String username) {
        ActivityData boardActivityData = new ActivityData();
        boardActivityData.setOwnerUsername(username);
        boardActivityData.setDate(ControllerUtils.getCurrentDate());
        boardActivityData.setEditable(false);
        boardActivityData.setData(" renamed list " + oldCardList.getName() + " to " + newCardList.getName());
        return boardActivityData;
    }

    public static ActivityData prepareBoardRenameActivity(Board newBoard, Board oldBoard, String username) {
        ActivityData boardActivityData = new ActivityData();
        boardActivityData.setOwnerUsername(username);
        boardActivityData.setDate(ControllerUtils.getCurrentDate());
        boardActivityData.setEditable(false);
        boardActivityData.setData(" renamed this board " + oldBoard.getName() + " to " + newBoard.getName());
        return boardActivityData;
    }

    public void addActivityToBoard(Board board, ActivityData activityData) {
        //TODO: Add enum type to activity describing what kind of activity it is.
        board.getActivities().add(activityData);
        boardRepository.save(board);
    }
}