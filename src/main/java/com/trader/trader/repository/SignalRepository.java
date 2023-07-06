package com.trader.trader.repository;

import com.trader.trader.models.SignalData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignalRepository extends MongoRepository<SignalData, Long> {
}
