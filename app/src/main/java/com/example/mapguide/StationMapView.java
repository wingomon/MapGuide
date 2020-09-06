package com.example.mapguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.mapguide.R.drawable.ic_outline_house_24;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

public class StationMapView extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener {

    Station selectedStation;
    TextView title, number;
    ImageView img;
    ImageView next, back;

    List<Station> stationList;

    private MapView mapView;
    private static final String ICON_GEOJSON_SOURCE_ID = "icon-source-id";
    private SymbolManager symbolManager;
    private List<Symbol> symbols = new ArrayList<>();
    HashMap<Symbol,Integer> markerIdMapping = new HashMap<>();


    //Für Berechtigungen für Zugriff zum Standort
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_station_map_view);


        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


        //Bekomme Stationsdaten aus Intent
        selectedStation = getIntent().getExtras().getParcelable("station");
        stationList = (List<Station>) getIntent().getSerializableExtra("stationList");

        title= (TextView) findViewById(R.id.textViewTitle);
        img = (ImageView) findViewById(R.id.textViewNumber);
        title.setText(Integer.toString(selectedStation.getNumber())+". "+selectedStation.getTitle());

        Picasso.get().load(selectedStation.getImgSrcPath()).into(img);

        next = (ImageView) findViewById(R.id.nextButton);

        if(selectedStation.getNumber() == stationList.size()){
            next.setEnabled(false);
            next.setVisibility(View.INVISIBLE);
        }


        back = (ImageView) findViewById(R.id.backButton);

        if(selectedStation.getNumber() == 1){
            back.setEnabled(false);
            back.setVisibility(View.INVISIBLE);
        }

        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int currentPosition = selectedStation.getNumber()-1;
                selectedStation = stationList.get(currentPosition-1);
                changeViewToSelectedStation();


            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int currentPosition = selectedStation.getNumber()-1;
                selectedStation = stationList.get(currentPosition+1);
                changeViewToSelectedStation();
                Log.d("--NEXT-Button--", "Current Position:" + currentPosition + "|" + "StationList-Size"+ stationList.size());




            }
        });


    }//end OnCreate()



    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                /**
                // Add origin and destination to the mapboxMap
                initMarkerIconSymbolLayer(style);
                initRouteLineLayer(style);
                initMapIfStationsExistent(style);
                mapboxMap.addOnMapClickListener(StartCreateGuide_AddStationOverview.this);
                enableLocationComponent(style);**/



                initMarkerIconSymbolLayer(style);


                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(selectedStation.getLatitude(),selectedStation.getLongitude()))
                        .zoom(15)
                        .tilt(20)
                        .build();

                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 10);

                // Add click listener and change the symbol to a cafe icon on click
                symbolManager.addClickListener(new OnSymbolClickListener() {
                    @Override
                    public void onAnnotationClick(Symbol symbol) {
                        int positionOfSymbolInList = symbols.indexOf(symbol);
                        selectedStation = stationList.get(positionOfSymbolInList);
                        changeViewToSelectedStation();
                        Log.d("StationMapView","This Marker was clicked:" + symbol.getLatLng() + "Positon:"+positionOfSymbolInList);

                    }
                });

            }
        });
    }//ennd onMapReady

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return true;
    }


    private void initMarkerIconSymbolLayer(Style style){
        // Add the marker image to map
        /**
        style.addImage("icon-image", BitmapFactory.decodeResource(
                this.getResources(), R.drawable.marker));

        style.addSource(new GeoJsonSource(ICON_GEOJSON_SOURCE_ID));

        style.addLayer(new SymbolLayer("icon-layer-id", ICON_GEOJSON_SOURCE_ID).withProperties(
                iconImage("icon-image"),
                iconSize(0.3f),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                iconOffset(new Float[] {0f, -7f})
        ));
**/
        if(stationList!=null){
            if(stationList.size()>0){
                addDestinationMarker(style);
            }
        }

    }

    private void addDestinationMarker(@NonNull Style style) {

        symbolManager = new SymbolManager(mapView, mapboxMap, style);
        symbolManager.setIconAllowOverlap(true);

        //Add Marker Bitmaps
        Bitmap bm = BitmapFactory.decodeResource(getBaseContext().getResources(),R.drawable.marker);
        mapboxMap.getStyle().addImage("my-marker", bm);

        List<SymbolOptions> options = new ArrayList<>();


        for (Station s : stationList) {

            options.add(new SymbolOptions()
                    .withLatLng(new LatLng(s.getLatitude(),s.getLongitude()))
                    .withIconImage("my-marker")
                    //set the below attributes according to your requirements
                    .withIconSize(0.3f)
                    .withIconOffset(new Float[] {0f,-1.5f})
                    .withTextHaloColor("rgba(255, 255, 255, 100)")
                    .withTextHaloWidth(5.0f)
                    .withTextAnchor("top")
                    .withTextOffset(new Float[] {0f, 1.5f})

            );

        }

        symbols = symbolManager.create(options);

    }

    private void changeViewToSelectedStation(){

        title.setText(Integer.toString(selectedStation.getNumber())+". "+selectedStation.getTitle());
        Picasso.get().load(selectedStation.getImgSrcPath()).into(img);

        //Check Controls
        //Check Visibility of Next-Button
        if(selectedStation.getNumber() == stationList.size()){
            next.setEnabled(false);
            next.setVisibility(View.INVISIBLE);
        } else{
            next.setEnabled(true);
            next.setVisibility(View.VISIBLE);
        }
        //Check Visibility of Back-Button
        if(selectedStation.getNumber() == 1){
            back.setEnabled(false);
            back.setVisibility(View.INVISIBLE);
        } else {
            back.setEnabled(true);
            back.setVisibility(View.VISIBLE);
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(selectedStation.getLatitude(),selectedStation.getLongitude()))
                .zoom(15)
                .tilt(20)
                .build();

        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 10);

    }



    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {

        Log.d("--ENABLELOCATIONCOMPONENT--","enableLocationComponent IN");
        Log.d("--PERMISSIONS ARE GRANTED?--", Boolean.toString(PermissionsManager.areLocationPermissionsGranted(this)));


        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
            Log.d("--Permissions not granted and now request!--","Requested Location Permission");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        symbolManager.onDestroy();
        super.onDestroy();
    }
}