package com.trading.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class QuoteLogResponseDto {
    private Long id;
    private String symbol;
    private String requestedTime;
    private BigDecimal mark;
    private Integer askSize;
    private String askTime;
    private BigDecimal askPrice;
    private Integer bidSize;
    private String bidTime;
    private BigDecimal bidPrice;
    private String lastTradeTime;
    private String lastQuoteTime;
    private Long totalVolume;
}