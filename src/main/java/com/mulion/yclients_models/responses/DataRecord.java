package com.mulion.yclients_models.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataRecord {
    private long id;
    private String date;
    private int length;
    @JsonProperty("visit_id")
    private long visitId;
}
