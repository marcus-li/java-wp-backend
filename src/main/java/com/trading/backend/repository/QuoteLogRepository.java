package com.trading.backend.repository;

import com.trading.backend.entity.QuoteLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuoteLogRepository extends JpaRepository<QuoteLog, Long> {
    // Spring parses "Top100" to add "LIMIT 100" to the SQL query
    List<QuoteLog> findTop100BySymbolOrderByRequestedTimeDesc(String symbol);
}