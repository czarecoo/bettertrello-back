package com.paw.bettertrello.repositories;

import com.paw.bettertrello.models.Board;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface BoardRepository extends MongoRepository<Board, String> {
    @Override
    void delete(Board deleted);
    @Query(value = "{}", fields = "{ 'cardLists' : 0 }")
    @Override
    List<Board> findAll();
}