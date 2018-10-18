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

        List<CardList> referencedCardLists = new ArrayList<>();
        List<Card> referencedCards = new ArrayList<>();

        if (board.getCardLists() != null ) {
            for (CardList cardList : board.getCardLists()) {
                if (cardList.getCards() != null) {
                    for (Card card : cardList.getCards()) {
                        card = cardRepository.save(card);
                        referencedCards.add(card);
                    }
                    cardList.setCards(referencedCards);
                }
                cardList = cardListRepository.save(cardList);
                referencedCardLists.add(cardList);
            }
            board.setCardLists(referencedCardLists);
        }

        boardRepository.save(board);

        return board;
    }

    @RequestMapping(method=RequestMethod.DELETE, value="/boards/{id}")
    public String deleteBoard(@PathVariable String id) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        Board board = optionalBoard.get();
        boardRepository.delete(board);

        return "";
    }
}