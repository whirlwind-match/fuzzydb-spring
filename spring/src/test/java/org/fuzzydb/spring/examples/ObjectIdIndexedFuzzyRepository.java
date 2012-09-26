package org.fuzzydb.spring.examples;

import org.bson.types.ObjectId;
import org.fuzzydb.spring.entities.ObjectIdItem;
import org.fuzzydb.spring.repository.FuzzyRepository;

public interface ObjectIdIndexedFuzzyRepository extends FuzzyRepository<ObjectIdItem, ObjectId> {

}
