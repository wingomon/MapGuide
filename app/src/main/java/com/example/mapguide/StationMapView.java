package com.example.mapguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
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
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.mapguide.R.drawable.ic_outline_house_24;
import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class StationMapView extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener {

    Station selectedStation;
    TextView title, number;
    ImageView img;
    ImageView next, back, myLocation;
    LinearLayout toStationView;

    List<Station> stationList;


    private MapView mapView;
    private static final String ICON_GEOJSON_SOURCE_ID = "icon-source-id";
    private SymbolManager symbolManager;
    private List<Symbol> symbols = new ArrayList<>();
    HashMap<Symbol,Integer> markerIdMapping = new HashMap<>();

    //ROUTE LINE STYLE
    private static final String TEAL_COLOR = "#B993D6";
    private static final float POLYLINE_WIDTH = 4;

    //Für Berechtigungen für Zugriff zum Standort
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;

    private DirectionsRoute currentRoute;
    MapboxDirections client;
    private final List<Feature> featureList = new ArrayList<>();

    //Bitmap Markers
    Bitmap bm,bm1,bm2,bm3,bm4,bm5,bm6,bm7,bm8,bm9,bm10,bm11,bm12,bm13,bm14,bm15;

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

        toStationView = (LinearLayout) findViewById(R.id.toStationView);
        toStationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StationViewActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("station",(Parcelable) selectedStation);
                intent.putExtra("stationList",(Serializable) stationList);
                startActivity(intent);
            }
        });

    }//end OnCreate()



    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                enableLocationComponent(style);

                if(stationList.size() > 1) {
                    initRouteLineLayer(style);
                }
                initMarkerIconSymbolLayer(style);


                //Camera-Setting: If coming from "Start Tour", then focus to fit all markers into camera View
                String startOfTour = getIntent().getStringExtra("start");
                if(startOfTour != null){
                    if(startOfTour.equals("true")){

                            if(stationList.size()>1) {

                                List<LatLng> stationLatLngList = new ArrayList<>();
                                for (Station s : stationList) {
                                    stationLatLngList.add(new LatLng(s.getLatitude(), s.getLongitude()));
                                }
                                LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(stationLatLngList).build();
                                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 300));
                            }
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(new LatLng(stationList.get(0).getLatitude(), stationList.get(0).getLongitude()))
                                        .zoom(15)
                                        .tilt(20)
                                        .build();

                                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 7000);


                    }
                } else {

                    //If NOT coming from "Start Tour"-Button, then focus on selected Station
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(selectedStation.getLatitude(), selectedStation.getLongitude()))
                            .zoom(15)
                            .tilt(20)
                            .build();

                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 10);

                }

                addClickListenerToMarker();

                myLocation = (ImageView) findViewById(R.id.myLocation);
                myLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enableLocationComponent(style);

                    }
                });

            }
        });



    }//end onMapReady

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return true;
    }


    private void initMarkerIconSymbolLayer(Style style){
        // Add the marker image to map
        if(stationList!=null){
            if(stationList.size()>0){
                addDestinationMarker(style);
            }
        }

    }

    private void initMarkerIcons(@NonNull Style style){

    }

    private void addDestinationMarker(@NonNull Style style) {

        symbolManager = new SymbolManager(mapView, mapboxMap, style);
        symbolManager.setIconAllowOverlap(true);

        //Add Marker Bitmaps
        Bitmap bm = BitmapFactory.decodeResource(getBaseContext().getResources(),R.drawable.marker_blue);
        mapboxMap.getStyle().addImage("my-marker", bm);
        //Marker for selected Station
        Bitmap bm1 = BitmapFactory.decodeResource(getBaseContext().getResources(),R.drawable.marker);
        mapboxMap.getStyle().addImage("my-marker-selected", bm1);

        List<SymbolOptions> options = new ArrayList<>();


        for (Station s : stationList) {

            //Setze einen anderen Marker für selektierte Station
            if(selectedStation != null && s.getNumber() == selectedStation.getNumber()){
                options.add(new SymbolOptions()
                        .withLatLng(new LatLng(s.getLatitude(), s.getLongitude()))
                        .withIconImage("my-marker-selected")
                        //set the below attributes according to your requirements
                        .withIconSize(0.3f)
                        .withIconOffset(new Float[]{0f, -1.5f})
                        .withTextHaloColor("rgba(255, 255, 255, 100)")
                        .withTextHaloWidth(5.0f)
                        .withTextAnchor("top")
                        .withTextOffset(new Float[]{0f, 1.5f}));
            } else {

                options.add(new SymbolOptions()
                        .withLatLng(new LatLng(s.getLatitude(), s.getLongitude()))
                        .withIconImage("my-marker")
                        //set the below attributes according to your requirements
                        .withIconSize(0.15f)
                        .withIconOffset(new Float[]{0f, -1.5f})
                        .withTextHaloColor("rgba(255, 255, 255, 100)")
                        .withTextHaloWidth(5.0f)
                        .withTextAnchor("top")
                        .withTextOffset(new Float[]{0f, 1.5f}));
            }
        }

        symbols = symbolManager.create(options);

    }

    private void updateDestinationMarker(){
        symbolManager.deleteAll();
        addDestinationMarker(mapboxMap.getStyle());
        addClickListenerToMarker();
    }

    private void addClickListenerToMarker(){

        if (symbolManager != null) {
            // Add click listener and change the symbol to a cafe icon on click
            symbolManager.addClickListener(new OnSymbolClickListener() {
                @Override
                public void onAnnotationClick(Symbol symbol) {
                    int positionOfSymbolInList = symbols.indexOf(symbol);
                    selectedStation = stationList.get(positionOfSymbolInList);
                    changeViewToSelectedStation();
                    Log.d("StationMapView", "This Marker was clicked:" + symbol.getLatLng() + "Positon:" + positionOfSymbolInList);

                }
            });
        }

    }

    private void initRouteLineLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource("route-source-id"));
        loadedMapStyle.addLayerBelow(new LineLayer("line-layer-id", "route-source-id")
                .withProperties(
                        lineColor(Color.parseColor(TEAL_COLOR)),
                        lineWidth(POLYLINE_WIDTH)
                ), "icon-layer-id");

        Style style = mapboxMap.getStyle();
        if (style != null) {
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

                }
            }
        }

    }

    private void getRoute(MapboxMap mapboxMap, Point origin, Point destination) {

        Log.d("MAPBOXDEBUG","Get Route");
        client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_WALKING)
                .accessToken(getString(R.string.mapbox_access_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                // You can get the generic HTTP info about the response
                Timber.d("Response code: " + response.code());
                if (response.body() == null) {
                    Timber.e("No routes found, make sure you set the right user and access token.");
                    Log.d("MAPBOXDEBUG","No route found");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Timber.e("No routes found");
                    Log.d("MAPBOXDEBUG","No route found < 1");
                    return;
                }   else {
                    // Get the directions route
                    currentRoute = response.body().routes().get(0);
                    featureList.add(Feature.fromGeometry(
                            LineString.fromPolyline(currentRoute.geometry(), PRECISION_6)));

                    FeatureCollection tempFeatureCollection = FeatureCollection.fromFeatures(featureList);
                    drawLines(tempFeatureCollection);
                }
            }
            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Timber.e("Error: " + throwable.getMessage());
            }
        });


    }


    private void drawLines(@NonNull FeatureCollection featureCollection) {
        Log.d("MAPBOXDEBUG","Feature - draw Lines" + featureCollection.features().size());

        if (mapboxMap != null) {
            mapboxMap.getStyle(style -> {
                if (featureCollection.features() != null) {
                    if (featureCollection.features().size() >= 0) {


                        GeoJsonSource source = style.getSourceAs("route-source-id");

                        if(source!=null) {
                            source.setGeoJson(featureCollection);
                        }
                        Log.d("MAPBOXDEBUG","DRAW LINES");
                    }
                }
            });
        }
    }


    private void changeViewToSelectedStation(){

        updateDestinationMarker();

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

        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 3000);

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