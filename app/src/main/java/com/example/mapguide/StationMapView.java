package com.example.mapguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
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

import java.util.List;

public class StationMapView extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener {

    Station selectedStation;
    TextView title, number;
    ImageView next, back;

    List<Station> stationList;

    private MapView mapView;

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
        number = (TextView) findViewById(R.id.textViewNumber);
        title.setText(selectedStation.getTitle());
        number.setText(Integer.toString(selectedStation.getNumber()));

        next = (ImageView) findViewById(R.id.nextButton);
        back = (ImageView) findViewById(R.id.backButton);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int currentPosition = selectedStation.getNumber()-1;
                selectedStation = stationList.get(currentPosition+1);

                title.setText(selectedStation.getTitle());
                number.setText(Integer.toString(selectedStation.getNumber()));

                Log.d("--CLICK--","NextButton Clicked");

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(selectedStation.getLatitude(),selectedStation.getLongitude()))
                        .zoom(15)
                        .tilt(20)
                        .build();

                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 10);

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


                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(selectedStation.getLatitude(),selectedStation.getLongitude()))
                        .zoom(15)
                        .tilt(20)
                        .build();

                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 10);

            }
        });
    }//ennd onMapReady

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
/**
        FeatureCollection featureCollection;

        clearMap();

        //ADD NEW STATION
        tempStation = new Station(stationList.size()+1, point.getLongitude(), point.getLatitude(),"Titel der Station","null","null","Beschreibung der Station");
        stationList.add(tempStation);
        stationAdapter.notifyDataSetChanged();

        Style style = mapboxMap.getStyle();
        if (style != null) {
            addDestinationMarker(style, point);


            Point origin;
            Point destination;

            if(stationList.size() >= 2) {

                Log.d("MAPBOXDEBUG","StationListe ist größer als 2");

                for (int i = stationList.size()-1; i > 0; i--) {

                    Log.d("MAPBOXDEBUG","StationList-Size:"+stationList.size());
                    destination = Point.fromLngLat((stationList.get(i).getLongitude()), (stationList.get(i).getLatitude()));
                    Log.d("MAPBOXDEBUG",destination.toString());

                    origin = Point.fromLngLat((stationList.get(i-1).getLongitude()), (stationList.get(i-1).getLatitude()));
                    Log.d("MAPBOXDEBUG",origin.toString());

                    getRoute(mapboxMap, origin, destination);

                    Log.d("MAPBOXDEBUG", "For INT I="+i+"---"+currentRoute);

                }
                FeatureCollection tempFeatureCollection = FeatureCollection.fromFeatures(featureList);
                drawLines(tempFeatureCollection);
                Log.d("MAPBOXDEBUG","FeatureCollection:"+FeatureCollection.fromFeatures(featureList).features().size());
            }

        }**/

        return true;
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

}