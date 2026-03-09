package com.lastmile.domain.port.out;

import java.util.Optional;

public interface GeocodingPort {
    record Coordinates(Double latitude, Double longitude){}
    Optional<Coordinates> geocode(String address);
}
