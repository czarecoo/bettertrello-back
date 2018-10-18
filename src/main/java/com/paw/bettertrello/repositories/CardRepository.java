package com.paw.bettertrello.repositories;

import com.paw.bettertrello.models.Card;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CardRepository extends MongoRepository<Card, String> {
    @Override
    void delete(Card deleted);
}
