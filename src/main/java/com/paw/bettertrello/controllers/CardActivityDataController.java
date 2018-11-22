package com.paw.bettertrello.controllers;

import com.paw.bettertrello.models.Board;
import com.paw.bettertrello.models.Card;
import com.paw.bettertrello.models.CardList;
import com.paw.bettertrello.repositories.BoardRepository;
import com.paw.bettertrello.repositories.CardActivityDataRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Optional;

@RestController
@Api(description="Operations pertaining to cards in application")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class CardActivityDataController {

    @Autowired
    CardActivityDataRepository cardRepository;
    @Autowired
    BoardRepository boardRepository;


}

