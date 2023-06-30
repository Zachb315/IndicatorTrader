package com.trader.trader.repository;

import com.trader.trader.models.Historical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoricalRepository extends JpaRepository<Historical, Long> {
}
