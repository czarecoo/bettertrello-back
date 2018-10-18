package com.paw.bettertrello.repositories;

import com.paw.bettertrello.models.CardList;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CardListRepository extends MongoRepository<CardList, String> {
    @Override
    void delete(CardList deleted);
}
