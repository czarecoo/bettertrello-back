package com.paw.bettertrello.repositories;

import com.paw.bettertrello.models.ActivityData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CardActivityDataRepository extends MongoRepository<ActivityData, String> {

}
