package com.paw.bettertrello.controllers;

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
    public Iterable<Board> getBoards(Principal principal) {
        return boardRepository.findAllByOwnerUsernamesContaining(principal.getName());
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
        return new ResponseEntity<>(boardRepository.save(board), HttpStatus.CREATED);
    }


    @ApiOperation(value = "Update a board")
    @RequestMapping(method=RequestMethod.PUT, value="/boards/{id}")
    public ResponseEntity<?> updateBoard(@PathVariable String id, @RequestBody Board board, Principal principal) {

        String username = principal.getName();

        if (!board.getOwnerUsernames().contains(username)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<Board> optionalBoard;

        if (!(board.getId() == null || board.getId().isEmpty())) {
            if (!board.getId().equals(id)) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }

        optionalBoard = boardRepository.findById(id);

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

}