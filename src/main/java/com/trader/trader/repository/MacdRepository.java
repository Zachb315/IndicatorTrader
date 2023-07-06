package com.trader.trader.repository;

import com.trader.trader.models.MacdData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MacdRepository extends MongoRepository<MacdData, Long> {
}
