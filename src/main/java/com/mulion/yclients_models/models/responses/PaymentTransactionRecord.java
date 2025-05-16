package com.mulion.yclients_models.models.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentTransactionRecord {
    private int amount;

    @JsonProperty("account")
    private Account account;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Account {
        @JsonProperty("is_cash")
        private boolean isCash;
    }
}
