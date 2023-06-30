package com.trader.trader.repository;

import com.trader.trader.models.OHLC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OHLCRepository extends JpaRepository<OHLC, Long> {
}
