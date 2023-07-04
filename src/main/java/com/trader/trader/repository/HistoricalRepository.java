package com.trader.trader.repository;

import com.trader.trader.models.Historical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoricalRepository extends JpaRepository<Historical, Long> {
    @Query("SELECT COUNT(h) > 0 FROM Historical h WHERE h.date = :date")
    boolean existsByDate(@Param("date") LocalDateTime localDateTime);

    @Query("SELECT h.price FROM Historical h WHERE h.date < :date ORDER BY h.date DESC")
    List<Double> findAllOrderByDate(@Param("date") LocalDateTime localDateTime);
}
