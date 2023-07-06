package com.trader.trader.repository;

import com.trader.trader.models.OHLC;
import org.bson.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OHLCRepository extends MongoRepository<OHLC, Long> {
    boolean existsByDate(Instant date);


    @Query(value = "{}", sort = "{_id: -1}", fields = "{_id: 0, close: 1}")
    List<Document> findRecentOrders(int longPeriod);

//    @Query(value = "{}", sort = "{date: 1}")
//    Instant findTopByOrderByDateAsc();
    @Query(value = "{}", sort = "{id: -1}")
    List<Double> findAllByOrderByIdDesc();


}
