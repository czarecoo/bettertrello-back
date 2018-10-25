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

import java.util.ArrayList;
import java.util.Optional;


@RestController
@Api(description="Operations pertaining to boards in application")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class BoardController {

    @Autowired
    BoardRepository boardRepository;
    @Autowired
    CardListRepository cardListRepository;
    @Autowired
    CardRepository cardRepository;

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved board")
    }
    )

    @ApiOperation(value = "Search a board with an ID",response = Optional.class)
    @RequestMapping(method=RequestMethod.GET, value="/boards")
    public Iterable<Board> getBoards() {
        return boardRepository.findAll();
    }

    @ApiOperation(value = "Search a board with an ID",response = Optional.class)
    @RequestMapping(method=RequestMethod.GET, value="/boards/{id}")
    public Optional<Board> getBoard(@PathVariable String id) {
        return boardRepository.findById(id);
    }

    @ApiOperation(value = "Search a board with an ID, a get's list from there",response = Iterable.class)
    @RequestMapping(method=RequestMethod.GET, value="/boards/{id}/lists")
    public Iterable<CardList> getListsFromBoard(@PathVariable String id) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        return optionalBoard.get().getCardLists();
    }

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

    @ApiOperation(value = "Add a board",response = Board.class)
    @RequestMapping(method=RequestMethod.POST, value="/boards")
    public Board postBoard(@RequestBody Board board) {
        boardRepository.save(board);

        return board;
    }

    @ApiOperation(value = "Add a List to Board",response = Board.class)
    @RequestMapping(method=RequestMethod.POST, value="/boards/{id}/lists")
    public Board postListToBoard(@RequestBody CardList cardList, @PathVariable String id) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if(optionalBoard.isPresent()) {
            Board board = optionalBoard.get();
            if (board.getCardLists() == null) {
                board.setCardLists(new ArrayList<CardList>());
            }
            board.getCardLists().add(cardList);
            return boardRepository.save(board);
        }
        else {
            return null;
        }
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

    @ApiOperation(value = "Update a board")
    @RequestMapping(method=RequestMethod.PUT, value="/boards/{id}")
    public ResponseEntity<?> updateBoard(@PathVariable String id, @RequestBody Board board) {

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

    @ApiOperation(value = "Delete a board")
    @RequestMapping(method=RequestMethod.DELETE, value="/boards/{id}")
    public String deleteBoard(@PathVariable String id) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        Board board = optionalBoard.get();
        boardRepository.delete(board);

        return "";
    }

    @ApiOperation(value = "Delete a list")
    @RequestMapping(method=RequestMethod.DELETE, value="/lists/{id}")
    public String deleteList(@PathVariable String id) {
        Optional<CardList> optionalCardList = cardListRepository.findById(id);
        CardList cardList = optionalCardList.get();
        cardListRepository.delete(cardList);

        return "";
    }

    @ApiOperation(value = "Delete a card")
    @RequestMapping(method=RequestMethod.DELETE, value="/cards/{id}")
    public String deleteCard(@PathVariable String id) {
        Optional<Card> optionalCard = cardRepository.findById(id);
        Card card = optionalCard.get();
        cardRepository.delete(card);

        return "";
    }
}