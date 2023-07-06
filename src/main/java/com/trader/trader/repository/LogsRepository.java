package com.trader.trader.repository;

import com.trader.trader.models.Logs;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogsRepository extends MongoRepository<Logs, Long> {
}
