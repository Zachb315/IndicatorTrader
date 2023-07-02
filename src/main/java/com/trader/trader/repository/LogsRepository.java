package com.trader.trader.repository;

import com.trader.trader.models.Logs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogsRepository extends JpaRepository<Logs, Long> {
}
