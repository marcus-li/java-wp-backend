package com.trading.backend.mapper;

import com.trading.backend.entity.QuoteLog;
import com.trading.backend.dto.QuoteLogResponseDto;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class QuoteLogMapper {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public static QuoteLogResponseDto toResponse(QuoteLog entity) {
        return QuoteLogResponseDto.builder()
                .id(entity.getId())
                .symbol(entity.getSymbol())
                .mark(entity.getMark())
                .askSize(entity.getAskSize())
                .askPrice(entity.getAskPrice())
                .bidSize(entity.getBidSize())
                .bidPrice(entity.getBidPrice())
                .totalVolume(entity.getTotalVolume())
                // Conversion Logic
                .requestedTime(format(entity.getRequestedTime()))
                .askTime(format(entity.getAskTime()))
                .bidTime(format(entity.getBidTime()))
                .lastTradeTime(format(entity.getLastTradeTime()))
                .lastQuoteTime(format(entity.getLastQuoteTime()))
                .build();
    }

    private static String format(Long millis) {
        return (millis == null) ? null : FORMATTER.format(Instant.ofEpochMilli(millis));
    }
}