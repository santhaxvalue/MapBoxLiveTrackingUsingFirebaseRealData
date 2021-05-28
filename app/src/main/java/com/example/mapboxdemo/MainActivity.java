package com.example.mapboxdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ReceiverCallNotAllowedException;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.StaticMapCriteria;
import com.mapbox.api.staticmap.v1.models.StaticMarkerAnnotation;
import com.mapbox.geojson.Point;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerOptions;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements  LocationEngineListener, PermissionsListener, MapboxMap.OnMapClickListener {

    private MapView mapView;
    private MapboxMap map;
    private TextView startButton;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;
    private Point originPosition;
    private Point destinationPosition;
    private Marker destinationMarker;

    private NavigationMapRoute navigationMapRoute;
    private static final String TAG = "MainActivity";

    // creating a variable for
    // our Firebase Database.
    FirebaseDatabase firebaseDatabase;

    // creating a variable for our
    // Database Reference for Firebase.
    DatabaseReference databaseReference1;

    // variable for Text view.
    private TextView titletext1,titletext2;

    ImageView imageView, centerimage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);

        // below line is used to get the instance
        // of our Firebase database.
        firebaseDatabase = FirebaseDatabase.getInstance();
        // below line is used to get
        // reference for our database.
        databaseReference1 = firebaseDatabase.getReference("data");

        
        mapView = (MapView) findViewById(R.id.mapView);
        startButton = (TextView) findViewById(R.id.startButton);
        imageView = (ImageView)findViewById(R.id.imageview);
        centerimage = (ImageView)findViewById(R.id.centerimage);

        // initializing our object class variable.
        titletext1 = findViewById(R.id.title1);
        titletext2 = findViewById(R.id.title2);

        // calling method
        // for getting data.
        getdataone();

        Log.d("super111:","super111:");

//        Double latdou = Double.valueOf(titletext1.getText().toString());
//        Double londou = Double.valueOf(titletext2.getText().toString());
//
////        Double latdou = 9.969641;
////        Double londou = 78.570410;
//
//        String latstr = String. valueOf(latdou);
//        String lonstr = String.valueOf(londou);
//
//        Log.d("newnew:","newnew:"+latstr);
//        Log.d("newnew1:","newnew1:"+lonstr);
//
//        LatLng locationlatlng = new LatLng(latdou,londou);
//
//
//        destinationMarker = map.addMarker(new MarkerOptions().position(locationlatlng));
//
//        MapboxStaticMap staticImage = MapboxStaticMap.builder()
//                .accessToken(getString(R.string.access_token))
//                .styleId(StaticMapCriteria.LIGHT_STYLE)
//                .cameraPoint(Point.fromLngLat(londou, latdou)) // Image's centerpoint on map
//                .cameraZoom(20)
//                .width(320) // Image width
//                .height(320) // Image height
//                .retina(true) // Retina 2x image will be returned
//                .build();
//
//        Picasso.get()
//                .load(staticImage.url().toString())
//                .noFade()
//                .placeholder(R.drawable.mapbox_marker_icon_default)
//                .into(imageView);

        mapView.onCreate(savedInstanceState);
//        mapView.getMapAsync(this);
        permissionsManager = new PermissionsManager(this);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .origin(originPosition)
                        .destination(destinationPosition)
                        .shouldSimulateRoute(true)
                        .build();
                NavigationLauncher.startNavigation(MainActivity.this, options);

            }
        });



    }


    private void getdataone() {

        Log.d("welcome1:","welcome1:");

        // calling add value event listener method
        // for getting the values from database.
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("welcome11:","welcome11:");
                // this method is call to get the realtime
                // updates in the data.
                // this method is called when the data is
                // changed in our Firebase console.
                // below line is for getting the data from
                // snapshot of our database.

                //old code
//                String value = snapshot.getValue(String.class);

                String lat = snapshot.child("lat").getValue(String.class);
                String lon = snapshot.child("lng").getValue(String.class);

                Log.d("welcome111:","welcome111:"+lat);
                Log.d("welcome1111:","welcome1111:"+lon);

                titletext1.setText("Latitude : "+lat+ " || "+"Longitude : "+lon);

                Double latdou = Double.valueOf(lat);
                Double londou = Double.valueOf(lon);

                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(latdou, londou, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName();

                    titletext2.setText("City : "+city+ " || "+"Country : "+country);

                } catch (IOException e) {
                    e.printStackTrace();
                }





                // after getting the value we are setting
                // our value to our text view in below line.
//                titletext.setText(value);


//        Double latdou = 9.969641;
//        Double londou = 78.570410;

                String latstr = String. valueOf(latdou);
                String lonstr = String.valueOf(londou);

                Log.d("newnew:","newnew:"+latstr);
                Log.d("newnew1:","newnew1:"+lonstr);

                MapboxStaticMap staticImage = MapboxStaticMap.builder()
                        .accessToken(getString(R.string.access_token))
                        .styleId(StaticMapCriteria.LIGHT_STYLE)
                        .cameraPoint(Point.fromLngLat(londou, latdou)) // Image's centerpoint on map
                        .cameraZoom(10)
                        .retina(true) // Retina 2x image will be returned
                        .build();

                Picasso.get()
                        .load(staticImage.url().toString())
                        .noFade()
                        .into(imageView);

                centerimage.setVisibility(View.VISIBLE);


                //add location using firebase live data in Mapbox Map
                if(destinationMarker != null){
                    map.removeMarker(destinationMarker);
                }
                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(MapboxMap mapboxMap) {
                        map = mapboxMap;

                        LatLng sydney = new LatLng(latdou,londou);

//                        destinationMarker = map.addMarker(new MarkerOptions().position(sydney));
                        Log.d("onesuper:","onesuper:"+sydney.toString());
                        destinationMarker =  map.addMarker(new MarkerOptions().position(sydney).title("Marker in My City"));
                        map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//                      map.animateCamera(CameraUpdateFactory.zoomTo(40.5f),2000,null);
//                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latdou,londou), 15.5f), 4000, null);
//                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15.5f));

                    }
                });



//                LatLng sydney = new LatLng(latdou,londou);
//
//                Log.d("onesuper:","onesuper:"+sydney.toString());
//
//                map.addMarker(new MarkerOptions().position(sydney).title("Marker in My City"));
//                map.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // calling on cancelled method when we receive
                // any error or we are not able to get the data.
                Toast.makeText(MainActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressWarnings("MissingPermission")
    @Override
    protected void onStart() {
        super.onStart();
        if(locationEngine != null){
            locationEngine.requestLocationUpdates();
        }
        if(locationLayerPlugin != null){
            locationLayerPlugin.onStart();
        }
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
        if(locationEngine != null){
            locationEngine.removeLocationUpdates();
        }
        if(locationLayerPlugin != null){
            locationLayerPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
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
        if(locationEngine != null){
            locationEngine.deactivate();
        }
        mapView.onDestroy();
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        locationEngine.requestLocationUpdates();

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            Log.d("super111666:","super111666:");
            originLocation = location;
            setCameraPosition(location);
        }

    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }
   @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine(){
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if(lastLocation != null){
            Log.d("super1116:","super1116:");
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            Log.d("super11166:","super11166:");
            locationEngine.addLocationEngineListener(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer(){
        locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
        locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
    }

    private void setCameraPosition(Location location){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),13.0));
    }

//    @Override
//    public void onMapReady(MapboxMap mapboxMap) {
//
//
//
//        map = mapboxMap;
//
//        LatLng sydney = new LatLng(31,74);
//
//        Log.d("onesuper:","onesuper:"+sydney.toString());
//
//        map.addMarker(new MarkerOptions().position(sydney).title("Marker in My City"));
//        map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//
//
////        map.addOnMapClickListener(this);
////        enableLocation();
//
//
//
//
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void enableLocation() {
        if(PermissionsManager.areLocationPermissionsGranted(this)){
            Log.d("super1111:","super1111:");
                initializeLocationEngine();
                initializeLocationLayer();
        }else{
            Log.d("super11111:","super11111:");
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onMapClick(@NonNull LatLng point) {

        if(destinationMarker != null){
            map.removeMarker(destinationMarker);
        }

        destinationMarker = map.addMarker(new MarkerOptions().position(point));

//        Log.d("super1:","super1:"+point.getLongitude()+" : "+point.getLatitude());
//        Log.d("super11:","super11:"+originLocation.getLongitude()+" : "+originLocation.getLongitude());

        destinationPosition = Point.fromLngLat(point.getLongitude(), point.getLatitude());
//        originPosition = Point.fromLngLat(originLocation.getLongitude(),originLocation.getLongitude());
        originPosition = Point.fromLngLat( 78.547726,9.989036);
        getRoute(originPosition,destinationPosition);

        startButton.setEnabled(true);
        startButton.setBackgroundResource(R.color.mapboxBlue);

    }

    private void getRoute(Point origin,Point destination){
        NavigationRoute.builder()
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if(response.body() == null){
                            Log.d(TAG,"No routes found, check right user and access token");
                            return;
                        } else if (response.body().routes().size() == 0) {
                            Log.d(TAG, "No routes found");
                            return;
                        }

                        DirectionsRoute currentRoute = response.body().routes().get(0);
                        if(navigationMapRoute != null){
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, map);
                        }
                        navigationMapRoute.addRoute(currentRoute);

                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Log.e(TAG,"Error: "+t.getMessage());
                    }
                });

    }

}