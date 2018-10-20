package com.paw.bettertrello.controllers;

import com.paw.bettertrello.models.Board;
import com.paw.bettertrello.models.Card;
import com.paw.bettertrello.models.CardList;
import com.paw.bettertrello.repositories.BoardRepository;
import com.paw.bettertrello.repositories.CardListRepository;
import com.paw.bettertrello.repositories.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RestController
public class BoardController {

    @Autowired
    BoardRepository boardRepository;
    @Autowired
    CardListRepository cardListRepository;
    @Autowired
    CardRepository cardRepository;

    @RequestMapping(method=RequestMethod.GET, value="/boards")
    public Iterable<Board> getBoards() {
        return boardRepository.findAll();
    }

    @RequestMapping(method=RequestMethod.GET, value="/boards/{id}")
    public Optional<Board> getBoard(@PathVariable String id) {
        return boardRepository.findById(id);
    }

    @RequestMapping(method=RequestMethod.GET, value="/boards/{id}/lists")
    public Iterable<CardList> getListsFromBoard(@PathVariable String id) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        return optionalBoard.get().getCardLists();
    }

    @RequestMapping(method=RequestMethod.GET, value="/lists/{id}")
    public Optional<CardList> getList(@PathVariable String id) {
        Optional<CardList> optionalCardList = cardListRepository.findById(id);
        return optionalCardList;
    }

    @RequestMapping(method=RequestMethod.GET, value="/lists/{id}/cards")
    public Iterable<Card> getCardsFromList(@PathVariable String id) {
        Optional<CardList> optionalCardList = cardListRepository.findById(id);
        return optionalCardList.get().getCards();
    }

    @RequestMapping(method=RequestMethod.POST, value="/boards")
    public Board postBoard(@RequestBody Board board) {
        boardRepository.save(board);

        return board;
    }

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

    @RequestMapping(method=RequestMethod.PUT, value="/boards")
    public Board updateBoard(@RequestBody Board board) {
        boardRepository.save(board);

        return board;
    }

    @RequestMapping(method=RequestMethod.PUT, value="/lists")
    public CardList updateList(@RequestBody CardList cardList) {
        cardListRepository.save(cardList);

        return cardList;
    }

    @RequestMapping(method=RequestMethod.DELETE, value="/boards/{id}")
    public String deleteBoard(@PathVariable String id) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        Board board = optionalBoard.get();
        boardRepository.delete(board);

        return "";
    }
}