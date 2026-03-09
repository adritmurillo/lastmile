package com.lastmile.infrastructure.adapter.out.maps;

import com.lastmile.domain.port.out.GeocodingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleMapsGeocodingAdapter implements GeocodingPort {

    @Value("${lastmile.google-maps.api-key}")
    private String apiKey;

    private final RestClient restClient;

    private static final String GEOCODING_URL =
            "https://maps.googleapis.com/maps/api/geocode/json";

    @Override
    public Optional<Coordinates> geocode(String address) {
        try {
            GoogleGeocodingResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("maps.googleapis.com")
                            .path("/maps/api/geocode/json")
                            .queryParam("address", address)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(GoogleGeocodingResponse.class);

            if (response == null
                    || response.results() == null
                    || response.results().isEmpty()
                    || !"OK".equals(response.status())) {
                log.warn("Geocoding returned no results for address: {}", address);
                return Optional.empty();
            }

            GoogleGeocodingResponse.Location location = response.results()
                    .get(0)
                    .geometry()
                    .location();

            return Optional.of(new Coordinates(location.lat(), location.lng()));

        } catch (Exception e) {
            log.error("Geocoding failed for address: {}. Error: {}", address, e.getMessage());
            return Optional.empty();
        }
    }

    record GoogleGeocodingResponse(
            java.util.List<Result> results,
            String status
    ) {
        record Result(Geometry geometry) {}

        record Geometry(Location location) {}

        record Location(Double lat, Double lng) {}
    }
}