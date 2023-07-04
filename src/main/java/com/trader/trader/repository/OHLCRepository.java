package com.trader.trader.repository;

import com.trader.trader.models.OHLC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OHLCRepository extends JpaRepository<OHLC, Long> {
    @Query("SELECT COUNT(o) > 0 FROM OHLC o WHERE o.date = :date")
    boolean existsByDate(@Param("date") LocalDateTime date);

    @Query("SELECT o.close FROM OHLC o ORDER BY o.id LIMIT :period")
    List<Double> findAllOrderByDate(@Param("period") int longPeriod);

    @Query("SELECT o.date FROM OHLC o ORDER BY o.date ASC LIMIT 1")
    LocalDateTime findOldestDate();

    @Query("SELECT o.close FROM OHLC o ORDER BY o.id DESC")
    List<Double> findAllOrderById();


}
