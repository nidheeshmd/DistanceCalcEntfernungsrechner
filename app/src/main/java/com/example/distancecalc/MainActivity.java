package com.example.distancecalc;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.text.Html;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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
    public static final int DEFAULT_ZOOM = 15;
    private ImageButton mBtnLocate;
    private EditText mSearchAddress;
    private TextView mTxtDistance, mTxtCurLoc, mTxtSerLoc;
    private FusedLocationProviderClient mLocationClient;
    private LocationCallback mLocationCallback;
    public double dblCurLan, dblCurLang, dblSerLan, dblSerLang;
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
        initGoogleMap();
        mLocationClient = new FusedLocationProviderClient(this);
        mTxtCurLoc = findViewById(R.id.txtCurLoc);
        mTxtCurLoc.setText(Html.fromHtml("<strong>Your Location Details</strong>" ));
        mTxtCurLoc.setTextColor(Color.parseColor("#3867d6"));
        mTxtSerLoc = findViewById(R.id.txtSerLoc);
        mTxtSerLoc.setText(Html.fromHtml("<strong>Searched Location Details</strong>" ));
        mTxtSerLoc.setTextColor(Color.parseColor("#3867d6"));
        mTxtDistance = findViewById(R.id.txtDistance);
        mTxtDistance.setText(Html.fromHtml("<strong> 00.00 Kilometers </strong>"));
        mTxtDistance.setTextColor(Color.parseColor("#ffffff"));

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location location = locationResult.getLastLocation();

                setCurLoc(location.getLatitude(),location.getLongitude());
                if(intFlag == 1) {
                    //Toast.makeText(MainActivity.this, location.getLatitude() + " \n" +
                     //       location.getLongitude(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onLocationResult: " + location.getLatitude() + " \n" +
                            location.getLongitude());
                    gotoLocation(dblCurLan, dblCurLang);
                    showMarker(dblCurLan, dblCurLang);
                    getAddress(dblCurLan, dblCurLang);
                }
                intFlag++;
            }
        };
        getLocationUpdates();
    }

    private void setCurLoc( double lat, double lng)
    {
        dblCurLan =lat;
        dblCurLang = lng;
    }

    private void setSerLoc(double lat, double lng)
    {
        dblSerLan =lat;
        dblSerLang = lng;
    }

    private void getAddress(double lat, double lng)
    {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
        addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
        }
        String curAddress = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String curCity = addresses.get(0).getLocality();
        String curState = addresses.get(0).getAdminArea();
        String curCountry = addresses.get(0).getCountryName();
        String curPostalCode = addresses.get(0).getPostalCode();
        mTxtCurLoc = findViewById(R.id.txtCurLoc);
        mTxtCurLoc.setText(Html.fromHtml("<strong>Your Location Details</strong><br>Address : <strong>" + curAddress +"</strong><br>"+
                "Postal Code: <strong>" + curPostalCode + "</strong> <br>"+
                "City: <strong>" + curCity + "</strong> <br>"+
                "State: <strong>" + curState + "</strong> <br>"+
                "Country: <strong>" + curCountry + "</strong>" ));
    }

    private void getDistanceBetweenTwoPoints(double lat1,double lon1,double lat2,double lon2) {
        mTxtDistance = findViewById(R.id.txtDistance);
        double pk = (double) (180.f/Math.PI);
        double a1 = lat1 / pk;
        double a2 = lon1 / pk;
        double b1 = lat2 / pk;
        double b2 = lon2 / pk;
        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);
        mTxtDistance.setText(Html.fromHtml("<strong>" + String.format("%.2f",(6366000 * tt)/1000 )+" Kilometers </strong>"));
    }

    private void geoLocate(View view) {
        hideSoftKeyboard(view);
        String locationName = mSearchAddress.getText().toString();

        Geocoder geocoder = new Geocoder(this, Locale.GERMANY);
        try {
            if(locationName.length() >= 3){
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList.size() > 0) {
                Address address = addressList.get(0);
                gotoLocation(address.getLatitude(), address.getLongitude());
                showMarker(address.getLatitude(), address.getLongitude());
                setSerLoc(address.getLatitude(), address.getLongitude());
                Toast.makeText(this, address.getLocality(), Toast.LENGTH_LONG).show();
                String SerAddress = address.getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String SerCity = address.getLocality();
                String SerState = address.getAdminArea();
                String SerCountry = address.getCountryName();
                String SerPostalCode = address.getPostalCode();
                mTxtSerLoc = findViewById(R.id.txtSerLoc);
                mTxtSerLoc.setText(Html.fromHtml("<strong>Searched Location Details</strong><br>Address : <strong>" + SerAddress + "</strong><br>" +
                        "Postal Code: <strong>" + SerPostalCode + "</strong> <br>" +
                        "City: <strong>" + SerCity + "</strong> <br>" +
                        "State: <strong>" + SerState + "</strong> <br>" +
                        "Country: <strong>" + SerCountry + "</strong>"));
                getDistanceBetweenTwoPoints(dblCurLan, dblCurLang, dblSerLan, dblSerLang);
                Log.d(TAG, "geoLocate: Locality: " + address.getAddressLine(0) + "," + address.getLocality() + "," + address.getSubLocality() + "," + address.getCountryName());
            }

            }
            else{
                Toast.makeText(MainActivity.this,
                        "Please enter valid location address.", Toast.LENGTH_LONG).show();
               // AlertDialog alertDialog = new AlertDialog.Builder(this)
                //        .setTitle("Valid Location")
                //        .setMessage("Please enter valid location address.")

                     //   .show();
            }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.current_location: {
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
}

//AIzaSyCRKQXgM_IBOjLkarOzziGCeCr0Xu48Fgk