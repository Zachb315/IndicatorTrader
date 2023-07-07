package com.trader.trader.repository;

import com.trader.trader.models.OrderLog;
import org.bson.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderLogRepository extends MongoRepository<OrderLog, Long> {

    @Query(value="{'open_or_closed': true}", count=true)
    public Long countByOpenOrClosed();

    public Document findTopByOrderByIdDesc();
}
