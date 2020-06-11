package com.example.distancecalc;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int PERMISSION_REQUEST_CODE = 9001;
    private final int PLAY_SERVICES_ERROR_CODE = 9002;
    private boolean mLocationPermissionGranted;
    public static final int GPS_REQUEST_CODE = 9003;
    public static final String TAG = "MapDebug";

    private GoogleMap mGoogleMap;

    //GoogleApiClient mLocationClient;

    public static final int DEFAULT_ZOOM = 15;

    private ImageButton mBtnLocate;
    private EditText mSearchAddress;

    private FusedLocationProviderClient mLocationClient;
    private LocationCallback mLocationCallback;

    public double dblCurLan, dblCurLang;
    public Location newLocation;

    private int intFlag = 1 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSearchAddress = findViewById(R.id.et_address);
        mBtnLocate = findViewById(R.id.btn_locate);
        mBtnLocate.setOnClickListener(this::geoLocate);

        //FloatingActionButton fab = findViewById(R.id.fab);


        //fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show());

        //fab.setOnClickListener(view -> {


        //if (mGoogleMap != null) {
        //  double bottomBoundry = ISLAMABAD_LAT - 0.3;
        //double leftBoundry = ISLAMABAD_LNG - 0.3;
        //double topBoundry = ISLAMABAD_LAT + 0.3;
        //double rightBoundry = ISLAMABAD_LNG + 0.3;

        //LatLngBounds ISLAMABAD_BOUNDS = new LatLngBounds(
        //      new LatLng(bottomBoundry, leftBoundry),
        //    new LatLng(topBoundry, rightBoundry)
        //);
        //mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(ISLAMABAD_BOUNDS, 1));
        //showMarker(ISLAMABAD_BOUNDS.getCenter());
        //}

        //});

        initGoogleMap();

        //SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment_container);
        //supportMapFragment.getMapAsync((OnMapReadyCallback) this);

        //SupportMapFragment supportMapFragment =SupportMapFragment.newInstance();
        //getSupportFragmentManager().beginTransaction()
        //      .add(R.id.map_fragment_container,supportMapFragment)
        //    .commit();

        //supportMapFragment.getMapAsync(this);

        //mLocationClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
        //      .addConnectionCallbacks(this)
        //    .addOnConnectionFailedListener(this)
        //  .build();

        //mLocationClient.connect();
        //mLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationClient = new FusedLocationProviderClient(this);
        //getCurrentLocation();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null) {
                    return;
                }
                Location location = locationResult.getLastLocation();
                 Toast.makeText(MainActivity.this, location.getLatitude() + " \n" +
                        location.getLongitude(), Toast.LENGTH_SHORT).show();

                Log.d(TAG, "onLocationResult: " + location.getLatitude() + " \n" +
                        location.getLongitude());

                //dblCurLan = location.getLatitude();
                //dblCurLang= location.getLongitude();
                //dblCurLang= location.getLongitude();

                setCurLoc(location.getLatitude(),location.getLongitude());

                if(intFlag == 1) {
                    gotoLocation(dblCurLan, dblCurLang);
                    showMarker(dblCurLan, dblCurLang);
                }
                intFlag++;
            }
        };
        getLocationUpdates();
        //gotoLocation(dblCurLan,dblCurLang);
        //showMarker(location.getLatitude(),location.getLongitude());
    }

    private void setCurLoc( double lat, double lng)
    {
        dblCurLan =lat;
        dblCurLang = lng;
    }

    private void geoLocate(View view) {
        hideSoftKeyboard(view);

        String locationName = mSearchAddress.getText().toString();

        Geocoder geocoder = new Geocoder(this, Locale.GERMANY);

        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);

            if (addressList.size() > 0) {
                Address address = addressList.get(0);

                gotoLocation(address.getLatitude(), address.getLongitude());

                showMarker(address.getLatitude(), address.getLongitude());

                Toast.makeText(this, address.getLocality(), Toast.LENGTH_LONG).show();

                Log.d(TAG, "geoLocate: Locality: " + address.getAddressLine(0) + "," + address.getLocality() + "," + address.getSubLocality() + "," + address.getCountryName());
            }

            /*for (Address address : addressList) {
                Log.d(TAG, "geoLocate: Address: " + address.getAddressLine(address.getMaxAddressLineIndex()));
            }*/
            //getCurrentLocation();


        } catch (IOException e) {


        }


    }

    private void showMarker(double lat, double lng) {

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(lat, lng));
        mGoogleMap.addMarker(markerOptions);
    }

    private void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void initGoogleMap() {

        if (isServicesOk()) {
            if (isGPSEnabled()) {
                if (checkLocationPermission()) {
                    Toast.makeText(this, "Ready to Map", Toast.LENGTH_SHORT).show();

                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map_fragment_container);

                    supportMapFragment.getMapAsync(this);


                } else {
                    requestLocationPermission();
                }
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is showing on the screen");

        mGoogleMap = googleMap;
        gotoLocation(51.1657, 10.4515);


        //mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        //mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        //mGoogleMap.getUiSettings().setMapToolbarEnabled(false);


    }

    private void gotoLocation(double lat, double lng) {

        if(lat > 0 && lng >0)
        {

        LatLng latLng = new LatLng(lat, lng);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM);

        mGoogleMap.moveCamera(cameraUpdate);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

    }

    private boolean isGPSEnabled() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (providerEnabled) {
            return true;
        } else {

            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("GPS Permissions")
                    .setMessage("GPS is required for this app to work. Please enable GPS.")
                    .setPositiveButton("Yes", ((dialogInterface, i) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, GPS_REQUEST_CODE);
                    }))
                    .setCancelable(false)
                    .show();

        }

        return false;
    }

    private boolean checkLocationPermission() {

        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isServicesOk() {

        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();

        int result = googleApi.isGooglePlayServicesAvailable(this);

        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApi.isUserResolvableError(result)) {
            Dialog dialog = googleApi.getErrorDialog(this, result, PLAY_SERVICES_ERROR_CODE, task ->
                    Toast.makeText(this, "Dialog is cancelled by User", Toast.LENGTH_SHORT).show());
            dialog.show();
        } else {
            Toast.makeText(this, "Play services are required by this application", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void getCurrentLocation() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        mLocationClient.getLastLocation().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                Location location = task.getResult();
                gotoLocation(location.getLatitude(), location.getLongitude());
                showMarker(location.getLatitude(), location.getLongitude());
                dblCurLan = location.getLatitude();
                dblCurLang = location.getLongitude();
            } else {
                Log.d(TAG, "getCurrentLocation: Error: " + task.getException().getMessage());
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.current_location: {
                //getCurrentLocation();
                intFlag = 1;
                getLocationUpdates();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GPS_REQUEST_CODE) {

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (providerEnabled) {
                Toast.makeText(this, "GPS is enabled", Toast.LENGTH_SHORT).show();
                initGoogleMap();
            } else {
                Toast.makeText(this, "GPS not enabled. Unable to show user location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLocationUpdates() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(100);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper());

    }

    //@Override
    //public void onConnected(@Nullable Bundle bundle) {
      //  Toast.makeText(this, "Connected to Location Services", Toast.LENGTH_SHORT).show();
    //}

    //@Override
    //public void onConnectionSuspended(int i) {
    //}

    //@Override
    //public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
     //   Toast.makeText(this, "Connection to Location Services failed", Toast.LENGTH_SHORT).show();
    //}


}


    /*private void geoLocate(View view) {
        hideSoftKeyboard(view);
        String locationName = mSearchAddress.getText().toString();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());



        try {
          List<Address> addressList = geocoder.getFromLocationName(locationName,1);

            if (addressList.size() > 0) {
                Address address = addressList.get(0);

                gotoLocation(address.getLatitude(), address.getLongitude());

                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude())));
                Toast.makeText(this, address.getLocality(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "geoLocate: Locality: " + address.getLocality());
            }
        }catch (IOException e){

        }

    }

    private void hideSoftKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showMarker(double lat, double lng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(lat, lng));
        mGoogleMap.addMarker(markerOptions);
    }

    private void gotoLocation(double lat,double lng){
        LatLng latLng=new LatLng(lat,lng);
        CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM);
        mGoogleMap.moveCamera(cameraUpdate);
    }




    private void initGoogleMap() {
        if(isServicesOk()){
            if(checkLocationPermission()){
                Toast.makeText(this, "Ready to Map", Toast.LENGTH_SHORT).show();
            }else{
                requestLocationPermission();
            }
        }
    }

    private boolean checkLocationPermission() {

        return ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

    }

        private boolean isServicesOk() {

            GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
            int result= googleApi.isGooglePlayServicesAvailable(this);
            if(result == ConnectionResult.SUCCESS){
                return true;
            }else if(googleApi.isUserResolvableError(result)){
                Dialog dialog=googleApi.getErrorDialog(this,result,PLAY_SERVICES_ERROR_CODE, task->
                        Toast.makeText(this, "Dialog is cancelled by User", Toast.LENGTH_SHORT).show());
                dialog.show();
            }else{
                Toast.makeText(this, "Play services are required by this application", Toast.LENGTH_SHORT).show();
            }

            return false;
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            mLocationPermissionGranted=true;
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is showing on the screen");

        mGoogleMap=googleMap;
        gotoLocation(ISLAMABAD_LAT,ISLAMABAD_LNG);

        MarkerOptions markerOptions=new MarkerOptions()
                .title("My Market")
                .position(new LatLng(0,0));

        mGoogleMap.addMarker(markerOptions);
    }*/


//AIzaSyCRKQXgM_IBOjLkarOzziGCeCr0Xu48Fgk