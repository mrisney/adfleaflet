package org.risney.adf.model;


import java.text.DecimalFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WeatherObservation;
import org.geonames.WebService;

import org.risney.adf.model.Locations.Location;
import org.risney.adf.utils.WeatherIconUtils;


public class GeoNamesDataControl {
    private static final Logger logger = Logger.getLogger(GeoNamesDataControl.class.getName());
    private static final int MAX_ROWS = 20;
    private static final String USER_NAME = "mrisney";
    private static final String ICON_FILE_EXT = ".png";
    private Map<String, Location> suggestedLocations;
    private WeatherIconUtils weatherIconUtils;

    public GeoNamesDataControl() {
        super();
        weatherIconUtils = new WeatherIconUtils();
        logger.setLevel(Level.ALL);
    }


    /**
     *
     * @param searchString
     * @return org.risney.adf.model.Locations
     * fuzzySearch constructs a org.geonames.ToponymSearchCriteria object and uses the Geonames method
     * WebService.search(searchCriteria) returing a list of Toponyms.
     * For the purpose of this example application, the last - or only Toponym
     * is used, more sophisticated applications could fine tune the results.
     *
     */

    public Locations fuzzySearch(String searchString) {
        Locations locations = new Locations();
        WebService.setUserName(USER_NAME);
        suggestedLocations = new HashMap<String, Location>();
        try {
            ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
            searchCriteria.setStyle(Style.FULL);
            searchCriteria.setMaxRows(MAX_ROWS);
            searchCriteria.setQ(searchString);

            ToponymSearchResult searchResult = WebService.search(searchCriteria);
            for (Toponym toponym : searchResult.getToponyms()) {
                Location location = getLocation(toponym, false);
                suggestedLocations.put(location.getPlaceName(), location);
                locations.addLocation(location);
            }
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
        }
        return locations;
    }

    /**
     *
     * @param latitude
     * @param longitude
     * @return org.risney.adf.model.Locations.Location
     *
     * getLocationAndWeatherWithCoordinates is primarily used by the onMapClick method
     * in adf-leaflet.js, then executed  from the proxy method serverEventHandler
     * in the ManagedBean
     * a org.geonames.ToponymSearchCriteria object and uses the Geonames method
     * WebService.search(searchCriteria) returing a list of Toponyms.
     * For the purpose of this example application, the last - or only Toponym
     * is used, more sophisticated applications could fine tune the results.
     */

    public Location getLocationAndWeatherWithCoordinates(Double latitude, Double longitude) {
        WebService.setUserName(USER_NAME);
        Location location = new Locations().new Location();
        try {

            List<Toponym> toponyms = WebService.findNearbyPlaceName(latitude, longitude);
            for (Toponym toponym : toponyms) {
                location = getLocation(toponym, true);
                Weather weather = getWeather(location.getLatitude(), location.getLongitude());
                location.setWeather(weather);
            }

        } catch (Exception ex) {
            logger.severe(ex.getMessage());
        }
        return location;
    }

    /**
     *
     * @param placeName
     * @return org.risney.adf.model.Locations.Location
     *
     * Used by the fuzzySearch autocomplete method 'onLocationSearchValueChange'
     * in the managed bean LeafletManagedBean, this returns previously found candidates from geonames
     * that were stored in a HashMap (avoid a second query). Then get the weather for that location.
     *
     */

    public Location getLocationAndWeatherWithPlaceName(String placeName) {
        try {
            Location location = suggestedLocations.get(placeName);
            Weather weather = getWeather(location.getLatitude(), location.getLongitude());
            location.setWeather(weather);
            return location;
        } catch (Exception ex) {
            logger.warning(ex.getMessage());
            return null;
        }
    }

    /**
     *
     * @param toponym
     * @param reQueryWebServie
     * @return org.risney.adf.model.Locations.Location
     *
     * Creates a comma seperated string, by concatenating the Place name, followed the successive Admin codes
     * that the adminName1, adminName2 and adminName3 provide - usually the city, followed by the state or province
     * followed by the district or in the US - the county, followed by the country name. returns a Location object
     *
     * There seems to be a bug in WebServices.findNearbyPlaceName - that returns a Toponym without a full feature,
     * so using  WebService.get(Toponym.getGeoNameId()..) seems to solve that problem.
     * Passing a Boolean lets this method know wether to call it or not.
     *
     */
    private Location getLocation(Toponym toponym, Boolean reQueryWebServie) {
        Location location = new Locations().new Location();
        StringBuffer sb = new StringBuffer(toponym.getName());

        try {
            // Workaround as the Toponym from WebServices.findNearbyPlaceName does not contain
            // AdminName1, however the resulting Toponym from WebService.get does.
            // so if this Boolean is true, re-query the toponym, using the geoname identifier.
            if (reQueryWebServie) {
                WebService.setUserName(USER_NAME);
                toponym = WebService.get(toponym.getGeoNameId(), null, null);
            }
            if (toponym.getAdminName1().length() > 0) {
                sb.append(", " + toponym.getAdminName1());
            }

        } catch (Exception ex) {
            logger.warning(ex.getMessage());
        }
        sb.append(", " + toponym.getCountryName());
        location.setPlaceName(sb.toString());
        location.setLatitude(toponym.getLatitude());
        location.setLongitude(toponym.getLongitude());
        return location;
    }

    /**
     *
     * @param latitude
     * @param longitude
     * @return org.risney.adf.model.Weather
     *
     *  Using the geonames webservice, find the nearest weather report (usually the nearest airport - ICAO)
     *
     */
    private Weather getWeather(Double latitude, Double longitude) {
        WebService.setUserName(USER_NAME);
        Weather weather = new Weather();
        try {
            WeatherObservation weatherObservation = WebService.findNearByWeather(latitude, longitude);
            weather.setStationName(weatherObservation.getStationName());
            weather.setICAOCode(weatherObservation.getIcaoCode());
            weather.setElevation(weatherObservation.getElevation().toString());
            weather.setObservedTime(weatherObservation.getObservationTime());
            // concatenate clouds and condition
            boolean hasCondition = false;
            StringBuffer generalWeatherSB = new StringBuffer();
            if (null != weatherObservation.getWeatherCondition()) {
                if (!weatherObservation.getWeatherCondition().equalsIgnoreCase("n/a")) {
                    generalWeatherSB.append(weatherObservation.getWeatherCondition());
                    hasCondition = true;
                }
            }
            if (null != weatherObservation.getClouds()) {
                if (!weatherObservation.getClouds().equalsIgnoreCase("n/a")) {
                    if (hasCondition) generalWeatherSB.append(", ");
                    generalWeatherSB.append(weatherObservation.getClouds());
                }
            }
            String generalWeather = "";
            if (null != generalWeatherSB) {
                generalWeather = generalWeatherSB.toString();
                generalWeather = generalWeather.substring(0, 1).toUpperCase() + generalWeather.substring(1);
            }

            weather.setWeatherCondition(generalWeather);

            try {
                double cel = weatherObservation.getTemperature();
                double far = cel * 9 / 5 + 32;
                DecimalFormat twoDForm = new DecimalFormat("#.#");
                far = Double.valueOf(twoDForm.format(far));
                String temperature = cel + "� Celsius, " + far + "� Fahrenheit";
                weather.setTemperature(temperature);
            } catch (Exception ex) {
                logger.warning("unable to convert temperature to fahrenheit, error :" + ex.toString());
            }

            weather.setHumidity(weatherObservation.getHumidity() + "");
            weather.setClouds(weatherObservation.getClouds());
            weather.setWindSpeed(weatherObservation.getWindSpeed());
            String imagePath = weatherIconUtils.getImagePath(weatherObservation.getClouds(), weatherObservation.getWeatherCondition());
            if (null != imagePath) {
                weather.setWeatherIcon(imagePath+ICON_FILE_EXT);
            }

        } catch (Exception ex) {
            logger.warning("unable to get weather, errors : " + ex);
        }
        return weather;
    }

    public static void main(String[] args) {

        GeoNamesDataControl dataControl = new GeoNamesDataControl();

        Double latitude = new Double(37.77493);
        Double longitude = new Double(-122.41942);

        Locations locations = dataControl.fuzzySearch("San Francisco, California, United States");
        logger.log(Level.INFO, locations.toJSON());
        for (Locations.Location location : locations.getLocations()) {
            logger.log(Level.INFO, location.toJSON());
        }

        Location location01 = dataControl.getLocationAndWeatherWithCoordinates(latitude, longitude);
        logger.log(Level.INFO, "location and weather from 'getLocationAndWeatherWithCoordinates' : " + location01.toJSON());

        Location location02 = dataControl.getLocationAndWeatherWithPlaceName("San Francisco, California, United States");
        logger.log(Level.INFO, "location and weather from 'getLocationAndWeatherWithPlaceName' : " + location02.toJSON());
    }
}
