package com.example.mapguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.optimization.v1.MapboxOptimization;
import com.mapbox.api.optimization.v1.models.OptimizationResponse;
import com.mapbox.geojson.LineString;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class StartCreateGuide_AddStationOverview extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener{

   private MapView mapView;

   //Für Berechtigungen für Zugriff zum Standort
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;

    //Liste der Stationen | RecyclerView Variablen
    List<Station> stationList;
    RecyclerView recyclerView;
    StationAdapter stationAdapter;
    Station tempStation;
    SymbolManager symbolManager;

    Button saveButton;


    private DirectionsRoute optimizedRoute;
    private MapboxOptimization optimizedClient;
    private Point origin;

    private static final String ICON_GEOJSON_SOURCE_ID = "icon-source-id";
    private static final String FIRST = "first";
    private static final String ANY = "any";
    private static final String TEAL_COLOR = "#FF0000";
    private static final float POLYLINE_WIDTH = 4;
    private static final String ROUTE_SOURCE_ID = "route-source-id";

    private DirectionsRoute currentRoute;
    MapboxDirections client;
    private List<DirectionsRoute> directionsRouteList;
    private final List<Feature> featureList = new ArrayList<>();

    boolean responseReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize MapBox View

        Log.d("----MY MAPBOX TOKEN IS------", getString(R.string.mapbox_access_token));

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        //Muss gelöscht werden, wenn das andere auskommentiert wird
        setContentView(R.layout.activity_start_create_guide__add_station_overview);


        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //StationListe initialisieren
        stationList = new ArrayList<Station>();

        recyclerView = (RecyclerView) findViewById(R.id.stationRecyclerView);
        stationAdapter =new StationAdapter(stationList,this);
        RecyclerView.LayoutManager sLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(sLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(stationAdapter);

        List<Station> stationListTemp = (List<Station>) getIntent().getSerializableExtra("stationList");
        if (stationListTemp != null){
            if(stationListTemp.size() > 0) {
                for (Station s : stationListTemp){
                    stationList.add(s);
                }
               stationAdapter.notifyDataSetChanged();
            }

        }

        //Speichern und Stations Werte setzen, dann zurück zur anderen Activity springen
        saveButton = (Button) findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent resultIntent = new Intent();
                resultIntent.putExtra("stationList",(Serializable) stationList);
                Log.d("--MAPGUIDE--AddstationOverview",stationList.toString());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });


    } //end OnCreate()


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                // Add origin and destination to the mapboxMap
                initMarkerIconSymbolLayer(style);
                initRouteLineLayer(style);
                initMapIfStationsExistent(style);
                mapboxMap.addOnMapClickListener(StartCreateGuide_AddStationOverview.this);
                enableLocationComponent(style);
            }
        });
    }

        private void initMarkerIconSymbolLayer(@NonNull Style loadedMapStyle) {
            // Add the marker image to map
            loadedMapStyle.addImage("icon-image", BitmapFactory.decodeResource(
                    this.getResources(), R.drawable.marker));

            // Add the source to the map
            /**
            loadedMapStyle.addSource(new GeoJsonSource(ICON_GEOJSON_SOURCE_ID,
                    Feature.fromGeometry(Point.fromLngLat(origin.longitude(), origin.latitude()))));
            **/

            loadedMapStyle.addSource(new GeoJsonSource(ICON_GEOJSON_SOURCE_ID));

            loadedMapStyle.addLayer(new SymbolLayer("icon-layer-id", ICON_GEOJSON_SOURCE_ID).withProperties(
                    iconImage("icon-image"),
                    iconSize(0.3f),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true),
                    iconOffset(new Float[] {0f, -7f})
            ));
        }

    private void initRouteLineLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource("route-source-id"));
        loadedMapStyle.addLayerBelow(new LineLayer("line-layer-id", "route-source-id")
                .withProperties(
                        lineColor(Color.parseColor(TEAL_COLOR)),
                        lineWidth(POLYLINE_WIDTH)
                ), "icon-layer-id");
    }




    public void initMapIfStationsExistent(@NonNull Style style){

        if (style != null) {

            Point origin;
            Point destination;

            if (stationList != null) {
                if (stationList.size() == 1) {
                    addDestinationMarker(style, new LatLng(stationList.get(0).getLongitude(), stationList.get(0).getLatitude()));
                }


                if (stationList.size() >= 2) {

                    Log.d("MAPBOXDEBUG", "StationListe ist größer als 2");

                    for (int i = stationList.size() - 1; i > 0; i--) {

                        if(i >=1) {
                            addDestinationMarker(style, new LatLng(stationList.get(i).getLongitude(), stationList.get(i).getLatitude()));
                        }

                        Log.d("MAPBOXDEBUG", "StationList-Size:" + stationList.size());
                        destination = Point.fromLngLat((stationList.get(i).getLongitude()), (stationList.get(i).getLatitude()));
                        Log.d("MAPBOXDEBUG", destination.toString());

                        origin = Point.fromLngLat((stationList.get(i - 1).getLongitude()), (stationList.get(i - 1).getLatitude()));
                        Log.d("MAPBOXDEBUG", origin.toString());

                        getRoute(mapboxMap, origin, destination);

                        Log.d("MAPBOXDEBUG", "For INT I=" + i + "---" + currentRoute);

                    }
                    FeatureCollection tempFeatureCollection = FeatureCollection.fromFeatures(featureList);
                    drawLines(tempFeatureCollection);
                    Log.d("MAPBOXDEBUG", "FeatureCollection:" + FeatureCollection.fromFeatures(featureList).features().size());

                }
            }

        }

    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {

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


                /**
                    FeatureCollection tempFeatureCollection = FeatureCollection.fromFeatures(featureList);
                    drawLines(tempFeatureCollection);
                    Log.d("MAPBOXDEBUG","FeatureCollection:"+FeatureCollection.fromFeatures(featureList).features().size());
**/
                }

                /**
                //Ich brauche für getOptimizedRoute eine Liste mit Punkten von der Stationsliste
                List<Point> stationPointList = new ArrayList<>();
                for(Station s: stationList) {
                    stationPointList.add(Point.fromLngLat(s.getLongitude(), s.getLatitude()));
                }
                if(stationList.size()>=2) {
                    getOptimizedRoute(style, stationPointList);
                } **/
            }

        return true;
}


    private void clearMap() {

        Log.d("Mapbox","Clear Map (should show when Map was clicked");
        if (mapboxMap != null) {
            Style style = mapboxMap.getStyle();
            if (style != null) {
                resetDestinationMarkers(style);
                removeRoute(style);
            }
        }
    }

    private void resetDestinationMarkers(@NonNull Style style) {
        GeoJsonSource lineSource = style.getSourceAs(ICON_GEOJSON_SOURCE_ID);
        /**
        if (optimizedLineSource != null) {
            optimizedLineSource.setGeoJson(Point.fromLngLat(origin.longitude(), origin.latitude()));
        }**/
    }



    private void removeRoute(@NonNull Style style) {
        GeoJsonSource lineSource = style.getSourceAs("route-source-id");
        /**
         if (optimizedLineSource != null) {
         optimizedLineSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[] {}));
         }**/
    }



    private void addDestinationMarker(@NonNull Style style, LatLng point) {
        List<Feature> destinationMarkerList = new ArrayList<>();
        for (Station s : stationList) {
            destinationMarkerList.add(Feature.fromGeometry(
                    Point.fromLngLat(s.getLongitude(), s.getLatitude())));
            Log.d("MAPBOX", "AddDestinationMarker: Iteration durch stationList, Hinzufügen aller Punkte");
        }
        //destinationMarkerList.add(Feature.fromGeometry(Point.fromLngLat(point.getLongitude(), point.getLatitude())));
        GeoJsonSource iconSource = style.getSourceAs(ICON_GEOJSON_SOURCE_ID);
        if (iconSource != null) {
            iconSource.setGeoJson(FeatureCollection.fromFeatures(destinationMarkerList));
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

                    Log.d("MAPBOXDEBUG", "GET ROUTE " + currentRoute);
                    Log.d("MAPBOXDEBUG", "FeatureList-size is" + featureList.size());
                    Log.d("MAPBOXDEBUG", "FeatureList FEATURE COLLECTION-size is" + FeatureCollection.fromFeatures(featureList).features().size());
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
                    if (featureCollection.features().size() > 0) {


                        GeoJsonSource source = style.getSourceAs("route-source-id");

                        if(source!=null) {
                            source.setGeoJson(featureCollection);
                        }

                        /**
                        style.addLayer(new LineLayer("linelayer", "line-source")
                                .withProperties(PropertyFactory.lineCap(Property.LINE_CAP_SQUARE),
                                        PropertyFactory.lineJoin(Property.LINE_JOIN_MITER),
                                        PropertyFactory.lineOpacity(.7f),
                                        PropertyFactory.lineWidth(7f),
                                        PropertyFactory.lineColor(Color.parseColor("#3bb2d0"))));
**/
                        Log.d("MAPBOXDEBUG","DRAW LINES");
                    }
                }
            });
        }
        if (featureList != null) {
            if(featureList.size() > 0) {
                featureList.clear();
            }
        }
    }

/**
        StartCreateGuide_AddStationOverview.this.mapboxMap = mapboxMap;

        Log.d("--MAPREADY--","OnMapReady was loaded");
        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjerxnqt3cgvp2rmyuxbeqme7"),
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        //Liste für Marker-Symbole
                        List<Symbol> symbolList = new ArrayList<>();

                        //Manager für Icons auf der Karte
                        style.addImage("markerIcon",getResources().getDrawable(R.drawable.marker));
                        symbolManager = new SymbolManager(mapView,mapboxMap,style);
                        // set non-data-driven properties, such as:
                        symbolManager.setIconAllowOverlap(true);
                        symbolManager.setIconTranslate(new Float[]{-4f,5f});
                        symbolManager.setIconRotationAlignment(ICON_ROTATION_ALIGNMENT_VIEWPORT);

                        enableLocationComponent(style);
                        Log.d("--MAPREADY,ONSTYLELOADED--","OnMapReady - OnSTyleloaded");

                        //Interaktion bei Klick auf die Karte
                        mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                            @Override
                            public boolean onMapClick(@NonNull LatLng point) {

                                //Wenn auf die Karte geklickt wird, werden alle vorherigen Symbole/Marker gelöscht! Danach durch die Liste iteriert und neu generiert
                                symbolManager.deleteAll();
                                LatLng tempPoint;
                                tempStation = new Station(stationList.size()+1, point.getLongitude(), point.getLatitude(),"Titel der Station","null","null","Beschreibung der Station");
                                stationList.add(tempStation);
                                //Nachdem die Stationen der Liste hinzugefügt werden, soll jede Station auch einen Marker auf der Karte erhalten
                                // Add symbol to symbolList at specified lat/lon

                                for (Station s : stationList) {
                                    tempPoint = new LatLng(s.getLatitude(),s.getLongitude());
                                    Symbol symbol = symbolManager.create(new SymbolOptions()
                                            .withLatLng(tempPoint)
                                            .withIconImage("markerIcon")
                                            .withIconSize(0.3f));
                                }
                                stationAdapter.notifyDataSetChanged();

                                //Wenn in der StationListe ein Eintrag vorher herrsscht, dann nimm diesen und zeichne eine Route von diesem Punkt zum aktuell geklickten Punkt

                                return true;
                            }
                        });
                    }
                });
    } //End onMapReady()**/

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
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1){
            if (resultCode == RESULT_OK){
                Station stationEdited = data.getExtras().getParcelable("station");
                stationList.set((stationEdited.getNumber()-1),stationEdited);
                stationAdapter.notifyDataSetChanged();
            }
        }
    }


}