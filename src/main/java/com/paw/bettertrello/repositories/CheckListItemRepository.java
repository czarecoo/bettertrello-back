package com.paw.bettertrello.repositories;

import com.paw.bettertrello.models.CheckListItem;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CheckListItemRepository extends MongoRepository<CheckListItem, String> {
}
