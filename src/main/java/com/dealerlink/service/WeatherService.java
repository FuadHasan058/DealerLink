package com.dealerlink.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Fetches location (geocoding) and current weather information for a city name, using the
 * free Open-Meteo REST APIs (no API key required). Responses are JSON and parsed with Jackson.
 *
 * This is used to help shops/dealers make smarter delivery decisions (e.g. flagging bad
 * weather at the dealer's or delivery's location). All calls are blocking/synchronous by
 * design - callers MUST invoke this from a background thread (see SessionManager.executor())
 * so the JavaFX UI thread never freezes while waiting on the network.
 */
public class WeatherService {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static class WeatherInfo {
        public String resolvedLocation;
        public double temperatureC;
        public String condition;
        public boolean success;
        public String errorMessage;

        @Override
        public String toString() {
            if (!success) return "Weather unavailable (" + errorMessage + ")";
            return resolvedLocation + ": " + condition + ", " + temperatureC + "\u00B0C";
        }
    }

    /** Looks up current weather for a free-text city/location name. Blocking call - run on a background thread. */
    public static WeatherInfo getWeatherForCity(String city) {
        WeatherInfo info = new WeatherInfo();
        if (city == null || city.isBlank()) {
            info.success = false;
            info.errorMessage = "No city provided";
            return info;
        }
        try {
            String encoded = URLEncoder.encode(city.trim(), StandardCharsets.UTF_8);
            String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?count=1&name=" + encoded;

            HttpRequest geoRequest = HttpRequest.newBuilder(URI.create(geoUrl))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();
            HttpResponse<String> geoResponse = CLIENT.send(geoRequest, HttpResponse.BodyHandlers.ofString());

            JsonNode geoRoot = MAPPER.readTree(geoResponse.body());
            JsonNode results = geoRoot.path("results");

            if (!results.isArray() || results.isEmpty()) {
                info.success = false;
                info.errorMessage = "Location \"" + city + "\" not found";
                return info;
            }

            JsonNode first = results.get(0);
            double lat = first.path("latitude").asDouble();
            double lon = first.path("longitude").asDouble();
            String resolvedName = first.path("name").asText(city);
            String country = first.path("country").asText("");
            info.resolvedLocation = country.isEmpty() ? resolvedName : resolvedName + ", " + country;

            String weatherUrl = String.format(
                    "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current_weather=true",
                    lat, lon);
            HttpRequest weatherRequest = HttpRequest.newBuilder(URI.create(weatherUrl))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();
            HttpResponse<String> weatherResponse = CLIENT.send(weatherRequest, HttpResponse.BodyHandlers.ofString());

            JsonNode weatherRoot = MAPPER.readTree(weatherResponse.body());
            JsonNode current = weatherRoot.path("current_weather");

            info.temperatureC = current.path("temperature").asDouble();
            int code = current.path("weathercode").asInt(-1);
            info.condition = describeWeatherCode(code);
            info.success = true;
            return info;

        } catch (Exception e) {
            info.success = false;
            info.errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();
            return info;
        }
    }

    /** Maps Open-Meteo's numeric WMO weather codes to a short human-readable description. */
    private static String describeWeatherCode(int code) {
        if (code == 0) return "Clear sky";
        if (code <= 3) return "Partly cloudy";
        if (code <= 48) return "Fog";
        if (code <= 57) return "Drizzle";
        if (code <= 67) return "Rain";
        if (code <= 77) return "Snow";
        if (code <= 82) return "Rain showers";
        if (code <= 86) return "Snow showers";
        if (code >= 95) return "Thunderstorm";
        return "Unknown conditions";
    }
}