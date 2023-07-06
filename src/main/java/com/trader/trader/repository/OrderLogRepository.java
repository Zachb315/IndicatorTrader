package com.trader.trader.repository;

import com.trader.trader.models.OrderLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderLogRepository extends MongoRepository<OrderLog, Long> {

    @Query("{'open_or_closed': ?0}")
    public Integer countByOpenOrClosed(boolean openOrClosed);

    public OrderLog findTopByOrderByIdDesc();
}
