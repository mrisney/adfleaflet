  var map;

  function initMap() {
      map = new L.Map("adf-map-div");
      map.locate({
          setView: true,
          maxZoom: 16
      });
      map.attributionControl.setPrefix('');
      osmTile = "http://tile.openstreetmap.org/{z}/{x}/{y}.png";
      osmCopyright = "&copy; oracle.com geonames.org openstreetmap.org https://github.com/mrisney/adfleaflet";
      osmLayer = new L.TileLayer(osmTile, {
          maxZoom: 16,
          attribution: osmCopyright
      });
      map.addLayer(osmLayer);
      map.on('locationfound', onLocationFound);
      map.on('locationerror', onLocationError);
      map.on('click', onMapClick);
      map.doubleClickZoom.disable();
  }

  function onLocationFound(e) {
      var radius = e.accuracy / 2;

      L.marker(e.latlng).addTo(map)
          .bindPopup("You are within " + radius + " meters from this point").openPopup();

      L.circle(e.latlng, radius).addTo(map);
  }

  function onLocationError(e) {
      alert(e.message);
  }

  function onMapClick(e) {
      map.fitBounds(e.target.getBounds());
      var param = {
          LatLong: e.latlng
      };
      
      // This is important, this references in the jsff, where the clientComponent 
      // is  set to true, when your embedd a .jsff into a region, it is the region name: panelGroupLayout id
      // so the leaflet-geonames.jsff is in the index.jspx "r1"
      //  <af:region value="#{bindings.adfleaflettaskflow1.regionModel}" id="r1"/>
      // <af:panelGroupLayout clientComponent="true" id="map-panel">
      
      var eventSource = AdfPage.PAGE.findComponentByAbsoluteId("r1:map-panel");
      AdfCustomEvent.queue(eventSource, "jsServerListener", param, true);
  }

  function zoomToLocation(e) {
      var location = JSON.parse(e);
      center = new L.LatLng(location.latitude, location.longitude);
      zoom = 12;
      map.setView(center, zoom);
      var contentStr;
      if (location.weather != null && location.weather.weatherIcon != null) {
          contentStr = "<img src='img/" + location.weather.weatherIcon + "'>";
          contentStr += "<br>" + location.weather.weatherCondition;
          contentStr += "<br>" + location.weather.temperature;
          contentStr += "<br> Windspeed : " + location.weather.windSpeed;
      } else {
          contentStr = "Weather conditions unavailable for"
      }
      contentStr += "<br>" + location.placeName;
      
      var popup = L.popup()
          .setLatLng([location.latitude, location.longitude])
          .setContent(contentStr)
          .openOn(map);
  }

  $(document).ready(function () {
      initMap();
  });