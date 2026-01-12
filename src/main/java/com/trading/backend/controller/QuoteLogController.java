package com.trading.backend.controller;

import com.trading.backend.dto.QuoteLogResponseDto;
import com.trading.backend.mapper.QuoteLogMapper;
import com.trading.backend.repository.QuoteLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteLogController {
    private final QuoteLogRepository quoteLogRepository;

    @GetMapping("/{symbol}")
    public List<QuoteLogResponseDto> getQuotesBySymbol(@PathVariable String symbol){
        return quoteLogRepository.findTop100BySymbolOrderByRequestedTimeDesc(symbol)
                .stream()
                .map(QuoteLogMapper::toResponse)
                .toList();
    }
}
