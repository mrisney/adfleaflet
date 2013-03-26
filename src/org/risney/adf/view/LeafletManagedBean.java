package org.risney.adf.view;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import oracle.adf.model.BindingContext;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.render.ClientEvent;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;

import org.risney.adf.model.Locations;
import org.risney.adf.model.Locations.Location;


public class LeafletManagedBean {
    private RichInputText locationInputText;

    public LeafletManagedBean() {
    }

    /**
     *
     * @param searchString
     * @return
     */
    public List getSuggestedLocations(String searchString) {

        // An array is created from candiates, derived  from the
        // the dataControl method 'fuzzySearch' passing, in whatever searchString is keyed
        // into the RichInputText locationInputText;

        List<SelectItem> suggestedLocations = new ArrayList<SelectItem>();
        BindingContainer bindings = BindingContext.getCurrent().getCurrentBindingsEntry();
        OperationBinding operationBinding = bindings.getOperationBinding("fuzzySearch");
        operationBinding.getParamsMap().put("searchString", searchString);
        operationBinding.execute();

        Locations locations = (Locations)operationBinding.getResult();
        if ((null != locations) && (null != locations.getLocations())) {
            for (Locations.Location location : locations.getLocations()) {
                if (null != location) {
                    suggestedLocations.add(new SelectItem(location.getPlaceName()));
                }
            }
        }
        return suggestedLocations;
    }


    public void onLocationSearchValueChange(ValueChangeEvent valueChangeEvent) {

        // Capture the auto-completed value,
        // executing the DataControl method 'getLocationAndWeatherWithPlaceName'
        // getting back a Location object, executing the

        if (valueChangeEvent.getNewValue() != null) {
            String placeName = valueChangeEvent.getNewValue().toString();
            BindingContainer bindings = BindingContext.getCurrent().getCurrentBindingsEntry();
            OperationBinding operationBinding = bindings.getOperationBinding("getLocationAndWeatherWithPlaceName");
            operationBinding.getParamsMap().put("placeName", placeName);
            operationBinding.execute();
            Location location = (Location)operationBinding.getResult();
            executeJavaScript("zoomToLocation('" + location.toJSON() + "')");
        }
    }

    /**
     *
     * @param clientEvent
     */

    public void serverEventHandler(ClientEvent clientEvent) {

        // Retrieve the parameters passed in from the adf-leaflet.js method onMapClick
        Map LatLong = (Map)clientEvent.getParameters().get("LatLong");
        Double latitude = (Double)LatLong.get("lat");
        Double longitude = (Double)LatLong.get("lng");
        System.out.println("latitude : " + latitude);
        System.out.println("longitude : " + longitude);

        // Execute the DataControl method 'getLocationAndWeatherWithCoordinates', getting back a Location object
        BindingContainer bindings = BindingContext.getCurrent().getCurrentBindingsEntry();
        OperationBinding operationBinding = bindings.getOperationBinding("getLocationAndWeatherWithCoordinates");
        operationBinding.getParamsMap().put("latitude", latitude);
        operationBinding.getParamsMap().put("longitude", longitude);
        operationBinding.execute();
        Location location = (Location)operationBinding.getResult();

        // Refresh the adf input text
        AdfFacesContext adfFacesContext = AdfFacesContext.getCurrentInstance();
        locationInputText.setValue(location.getPlaceName());
        adfFacesContext.addPartialTarget(locationInputText);

        // Execute the javascript
        String javascriptCommand = "zoomToLocation('" + location.toJSON() + "')";
        executeJavaScript(javascriptCommand);
    }

    /**
     *
     * @param script
     *
     * example: executeJavaScript("alert('hello world')");
     */
    public void executeJavaScript(final String script) {
        ExtendedRenderKitService service = (ExtendedRenderKitService)Service.getRenderKitService(FacesContext.getCurrentInstance(), ExtendedRenderKitService.class);
        service.addScript(FacesContext.getCurrentInstance(), script);
    }


    public void setLocationInputText(RichInputText locationInputText) {
        this.locationInputText = locationInputText;
    }

    public RichInputText getLocationInputText() {
        return locationInputText;
    }
}