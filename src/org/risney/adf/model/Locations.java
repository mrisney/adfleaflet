package org.risney.adf.model;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class Locations {
    private List<Location> locations;

    public Locations() {
        super();
    }

    public void addLocation(Location location) {
        if (null == locations) {
            locations = new ArrayList<Location>();
        }
        this.locations.add(location);
    }

    public void setLocations(List<Locations.Location> locations) {
        this.locations = locations;
    }

    public List<Locations.Location> getLocations() {
        return locations;
    }

    public String toJSON() {
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.toJson(this);
    }


    public class Location {
        private String placeName;
        private Double latitude;
        private Double longitude;
        private Weather weather;

        public Location() {
            super();
        }

        public Location(String placeName) {
            super();
            this.placeName = placeName;
        }

        public void setPlaceName(String placeName) {
            this.placeName = placeName;
        }

        public String getPlaceName() {
            return placeName;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setWeather(Weather weather) {
            this.weather = weather;
        }

        public Weather getWeather() {
            return weather;
        }
        
        public String toJSON() {
            Gson gson = new GsonBuilder().serializeNulls().create();
            return gson.toJson(this);
        }
    }
}
