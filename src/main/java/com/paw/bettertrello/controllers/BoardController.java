package com.paw.bettertrello.controllers;

import com.paw.bettertrello.models.Board;
import com.paw.bettertrello.models.CardList;
import com.paw.bettertrello.repositories.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;


@RestController
public class BoardController {

    @Autowired
    BoardRepository boardRepository;

    @RequestMapping(method=RequestMethod.GET, value="/boards")
    public Iterable<Board> getBoards() {
        return boardRepository.findAll();
    }

    @RequestMapping(method=RequestMethod.POST, value="/boards")
    public Board postBoard(@RequestBody Board board) {
        boardRepository.save(board);

        return board;
    }

    @RequestMapping(method=RequestMethod.GET, value="/boards/{id}")
    public Optional<Board> getBoard(@PathVariable String id) {
        return boardRepository.findById(id);
    }

    @RequestMapping(method=RequestMethod.DELETE, value="/boards/{id}")
    public String deleteBoard(@PathVariable String id) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        Board board = optionalBoard.get();
        boardRepository.delete(board);

        return "";
    }

    @RequestMapping(method=RequestMethod.GET, value="/boards/{id}/lists")
    public Iterable<CardList> getListsFromBoard(@PathVariable String id) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        return optionalBoard.get().getCardLists();
    }
}