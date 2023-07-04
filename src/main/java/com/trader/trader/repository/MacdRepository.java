package com.trader.trader.repository;

import com.trader.trader.models.MacdData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MacdRepository extends JpaRepository<MacdData, Long> {
}
