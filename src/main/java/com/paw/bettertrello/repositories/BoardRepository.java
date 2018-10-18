package com.paw.bettertrello.repositories;

import com.paw.bettertrello.models.Board;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BoardRepository extends MongoRepository<Board, String> {
    @Override
    void delete(Board deleted);
}