package com.paw.bettertrello.repositories;

import com.paw.bettertrello.models.Board;
import org.springframework.data.repository.CrudRepository;

public interface BoardRepository extends CrudRepository<Board, String> {
    @Override
    void delete(Board deleted);
}