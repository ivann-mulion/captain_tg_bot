package com.mulion.constants;

import com.mulion.yclients.models.HTTPRequests;
import com.mulion.yclients.models.impl.HTTPRequestsImpl;
import com.mulion.services.ConfigService;
import lombok.experimental.UtilityClass;

import java.net.http.HttpClient;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class Config {
    public static final String PATH_TO_PROPERTIES = "config.properties";
    public static final String BASE_URL = ConfigService.getProperty("yc.base_url");
    public static final String PARTNER_TOKEN = ConfigService.getProperty("yc.partner_token");
    public static final long COMPANY_ID = Long.parseLong(ConfigService.getProperty("yc.company_id"));
    public static final HTTPRequests httpRequests = new HTTPRequestsImpl();
    public static final HttpClient httpClient = HttpClient.newHttpClient();
    public static final DateTimeFormatter yclientDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter reportDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final LocalTime startDayTime = LocalTime.of(3, 0);
}
