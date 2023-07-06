package com.trader.trader.repository;

import com.trader.trader.models.Historical;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface HistoricalRepository extends MongoRepository<Historical, Long> {
    @Query("{'date':?0}")
    boolean existsByDate(Instant localDateTime);

    @Query("{'date': {$lt: ?0}}")
    List<Double> findAllOrderByDate(Instant localDateTime);
}
