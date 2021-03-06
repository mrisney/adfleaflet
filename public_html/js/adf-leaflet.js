  var map;

  // Standard Leaflet.js setup routines, the 'adf-map-div' is a reference to the Div tag
  // in the .jsff page, where  the map is to be rendered.
  // using openstreetmaps for layer, could be swithched to MS Bing or Google maps
  // Openstreetmaps has the least amount of restrictions.
  // to learn more about setting up Leaflet.js - http://leafletjs.com/examples.html
  // Also adding a watermark, or osmCopyright, acknowledging the organizations and corporations that
  // this mashup utilizes
  function initMap() {
      map = new L.Map("adf-map-div");
      map.locate({
          setView: true,
          maxZoom: 16
      });
      map.attributionControl.setPrefix('');
      osmTile = "http://tile.openstreetmap.org/{z}/{x}/{y}.png";
      //osmCopyright = "&copy; oracle.com geonames.org openstreetmap.org https://github.com/mrisney/adfleaflet";
      osmLayer = new L.TileLayer(osmTile, {
          maxZoom: 16
      });
      map.addLayer(osmLayer);
      map.on('locationfound', onLocationFound);
      map.on('locationerror', onLocationError);
      map.on('click', onMapClick);
      map.doubleClickZoom.disable();
  }

  function onLocationFound(e) {
      var param = {
          LatLong: e.latlng
      };
       // JQuery wild card search for 'map-panel' panel group layout
      // http://myadfnotebook.blogspot.com/2011/09/gotcha-when-using-adfpagepage-to-find.html
      
      var componentId = $('[id*=map-panel]').attr('id');
      var eventSource = AdfPage.PAGE.findComponent(componentId);
      AdfCustomEvent.queue(eventSource, "jsServerListener", param, true);
  }

  function onLocationError(e) {
      alert(e.message);
  }

  function onMapClick(e) {
      map.fitBounds(e.target.getBounds());
      var param = {
          LatLong: e.latlng
      };
      // JQuery wild card search for 'map-panel' panel group layout
      // http://myadfnotebook.blogspot.com/2011/09/gotcha-when-using-adfpagepage-to-find.html
      var componentId = $('[id*=map-panel]').attr('id');
      var eventSource = AdfPage.PAGE.findComponent(componentId);
      AdfCustomEvent.queue(eventSource, "jsServerListener", param, true);
  }

  function zoomToLocation(e) {
  
      // Casting the location JSON String sent from the managed bean, to a JSON Object
      // for this example, displaying the weather and location, the "e" or event, could
      // be a JSON String object of anything, or instructions to furthur interact with your 
      // HTML5/js application - location and weather is easy data to get from geonames.org
      // a real world application could pass more meaningful data across.
      
      var location = JSON.parse(e);
      var center = new L.LatLng(location.latitude, location.longitude);
      map.panTo(center);
      var contentStr;
      if (location.weather != null && location.weather.weatherIcon != null) {
          contentStr = "<img src='img/" + location.weather.weatherIcon + "'>";
          contentStr += "<br>" + location.weather.weatherCondition;
          contentStr += "<br>" + location.weather.temperature;
          contentStr += "<br> Windspeed : " + location.weather.windSpeed;
          contentStr += "<br> Updated : " + location.weather.observedTime;
      } else {
          contentStr = "Weather conditions unavailable at this time for"
      }
      contentStr += "<br>" + location.placeName;
      contentStr += "<br> Latitude : " + location.latitude +", Longitude : "+location.longitude;
      
      var popup = L.popup()
          .setLatLng(center)
          .setContent(contentStr)
          .openOn(map);
  }

  // Important ! - ADF generates quite a bit of Javascript for RichFaces.
  // Creating an initialization routine, using JQuery's $(document).ready(function () 
  // function - many hours were spent finding out when to initiate a JS routine outiside the
  // JS that is created from ADF pages - very, very important to have this in any JS that you are
  // incorporating into your ADF application, so that the instantiation of javascript loads on page load - appropriately.
  $(document).ready(function () {
      initMap();
  });