package com.trading.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.trading.backend.config.MillisToDateSerializer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "quote_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;

    @Column(name = "requested_time")
    @Getter(onMethod_ = {@JsonSerialize(using = MillisToDateSerializer.class)})
    private Long requestedTime; // OK

    private BigDecimal mark;

    @Column(name = "ask_size")
    private Integer askSize;

    @Column(name = "ask_time")
    @JsonSerialize(using = MillisToDateSerializer.class)
    private Long askTime;

    @Column(name = "ask_price")
    private BigDecimal askPrice;

    @Column(name = "bid_size")
    private Integer bidSize;

    @Column(name = "bid_time")
    @JsonSerialize(using = MillisToDateSerializer.class)
    private Long bidTime;

    @Column(name = "bid_price")
    private BigDecimal bidPrice;

    @Column(name = "last_trade_time")
    @JsonSerialize(using = MillisToDateSerializer.class)
    private Long lastTradeTime;

    @Column(name = "last_quote_time")
    @JsonSerialize(using = MillisToDateSerializer.class)
    private Long lastQuoteTime;

    @Column(name = "total_volume")
    private Long totalVolume;
}