package com.amupys.mapsmore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ServiceConnection, View.OnClickListener {

    public static final float ZOOM = 15f;
    public static final int NUM_ACC = 20;
    public static final int MEDIUM_ACCIDENT = 15;
    private boolean mLocationPermission = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    LocationManager locationManager;
    Context mContext;
    private LocationService locationService;
    private Button btnStart, btnAdd;
    ImageView imageView;
    private LatLng latLng;
    static MainActivity instance;
    private RelativeLayout relativeLayout;
    public static ArrayList<com.amupys.mapsmore.Location> places, userArray;
    private final String[] url = {"https://us-central1-roadsafety-d90af.cloudfunctions.net/app/api/read",
    "https://us-central1-roadsafety-d90af.cloudfunctions.net/app/useraddtoapi/read"};

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_start:
                if(mLocationPermission){
                    if(!locationService.isRunning()){
                        btnStart.setText("Stop service");
                        Log.e("button", "starting");
                        try {
                            Bundle bundle = new Bundle();
                            Intent intent = new Intent(this, LocationService.class);
                            bundle.putDouble("Lat", latLng.latitude);
                            bundle.putDouble("Long", latLng.longitude);
                            intent.putExtras(bundle);
                            startService(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error setting location", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Log.e("button", "stopped");
                        btnStart.setText("Start");
                        locationService.stopService();
                    }
                }
                break;
            case R.id.btn_main:
                startActivity(new Intent(this, DataActivity.class));
                break;
            case R.id.btn_add_data:
                addUserData();
                break;
        }
    }

    private void addUserData() {
        AddFragment addFragment = new AddFragment(MainActivity.this);
        addFragment.show(this.getSupportFragmentManager(), "bottomMenu");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStart = findViewById(R.id.btn_start);
        btnAdd = findViewById(R.id.btn_add_data);

        relativeLayout = findViewById(R.id.linear_lay);

        btnStart.setOnClickListener(this);
        btnAdd.setOnClickListener(this);

        mContext = this;

        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        instance = this;

        if(hasInternetConnection()){
            initList();
        }else
            Toast.makeText(this, "Oops! No Internet Connection", Toast.LENGTH_SHORT).show();

        imageView = findViewById(R.id.btn_main);
        imageView.setOnClickListener(this);
    }

    private void initList() {
        if(places != null && places.size() != 0){
        }else {
            places = new ArrayList<>();

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Please wait..");
            progressDialog.setCancelable(true);
            progressDialog.show();

            StringRequest request = new StringRequest(Request.Method.GET ,url[0], new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if(response != null){
                        progressDialog.dismiss();
                        try{
                            JSONArray jsonArray = new JSONArray(response);
                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject object = jsonArray.getJSONObject(i);
                                com.amupys.mapsmore.Location location = new com.amupys.mapsmore.Location(null, object.getString("name"), object.getString("description")
                                        , object.getDouble("lat"), object.getDouble("long"), object.getInt("Acc_per_year")
                                        , object.getInt("Speed_limit"));

                                places.add(location);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                }
            });

            RequestQueue queue = Volley.newRequestQueue(this);

            queue.add(request);
        }

        if(userArray != null && userArray.size() != 0){}else {
            userArray = new ArrayList<>();

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Please wait..");
            progressDialog.setCancelable(true);
            progressDialog.show();

            StringRequest request = new StringRequest(Request.Method.GET ,url[1], new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if(response != null){
                        progressDialog.dismiss();
                        try{
                            JSONArray jsonArray = new JSONArray(response);
                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject object = jsonArray.getJSONObject(i);
                                com.amupys.mapsmore.Location location = new com.amupys.mapsmore.Location(null, object.getString("name"), object.getString("description")
                                        , object.getDouble("lat"), object.getDouble("long"), object.getInt("Acc_per_year")
                                        , object.getInt("Speed_limit"));

                                location.setTimeOfAcc(object.getLong("time"));
                                userArray.add(location);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                }
            });

            RequestQueue queue = Volley.newRequestQueue(this);

            queue.add(request);
        }
    }

    public static MainActivity getInstance(){
        return instance;
    }

    protected void onResume(){
        super.onResume();
        Intent intent = new Intent(this, LocationService.class);
        bindService(intent, this, BIND_AUTO_CREATE);

        getLocationPermission();
    }

    private boolean hasInternetConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (NetworkInfo ni : netInfo) {
                if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                    if (ni.isConnected())
                        haveConnectedWifi = true;
                if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (ni.isConnected())
                        haveConnectedMobile = true;
            }
        } catch (Exception e) {
        }

        return haveConnectedWifi || haveConnectedMobile;
    }

    private void isLocationEnabled() {
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder alertDialog=new AlertDialog.Builder(mContext);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            AlertDialog alert=alertDialog.create();
            alert.show();
        }else {
            getLocation(MainActivity.this);
        }
    }

    public LatLng getLocation(Context context) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        try {
            if (mLocationPermission) {
                Task<Location> location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Location location1 = (Location) task.getResult();
                            if (location1 != null) {
                                latLng = new  LatLng(location1.getLatitude(), location1.getLongitude());
                                moveCamera(latLng, ZOOM, "My Location");
                                String string = String.valueOf(latLng.latitude+" "+latLng.longitude);
                                Log.e("Location", string);
                            } else {
                                Toast.makeText(context, "Device Location is disabled", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
        }
        return latLng;
    }

    public void moveCamera(LatLng latlng, float zoom, String title) {
        Log.e("move camera", "done");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));

        MarkerOptions options = new MarkerOptions()
                .position(latlng)
                .title(title);
        mMap.addMarker(options);
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.e("Map", "map is ready");
                mMap = googleMap;

                if (mLocationPermission) {
                    isLocationEnabled();

                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                }
            }
        });
    }

    public void createToast(com.amupys.mapsmore.Location location){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_lay,
                (ViewGroup) findViewById(R.id.toast_lay_root));

        if(location.getNumAcc() >= NUM_ACC){
            layout.setBackgroundColor(getResources().getColor(R.color.red));
        }else if(location.getNumAcc() < NUM_ACC && location.getNumAcc() > MEDIUM_ACCIDENT)
            layout.setBackgroundColor(getResources().getColor(R.color.yellow));
        TextView text1 = (TextView) layout.findViewById(R.id.toast_loc);
        text1.setText(location.getName());
        TextView text = (TextView) layout.findViewById(R.id.toast_des);
        text.setText(location.getDescription());
        TextView speed = layout.findViewById(R.id.txt_speed), severe = layout.findViewById(R.id.txt_severity);
        speed.setText("Speed: "+location.getSpeedLim()+" Km/hr");
        int num = location.getNumAcc();
        if(num>50)
            severe.setText("High");
        else
            severe.setText("Moderate");


//        Toast toast = new Toast(getApplicationContext());
//        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//        toast.setDuration(Toast.LENGTH_LONG);
//        toast.setView(layout);
//        toast.show();

        relativeLayout.removeAllViews();
        relativeLayout.addView(layout);
        relativeLayout.setVisibility(View.VISIBLE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                relativeLayout.setVisibility(View.INVISIBLE);
            }
        }, 30000);
    }

    private void getLocationPermission(){
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION };

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermission = true;
                initMap();
            }else {
                ActivityCompat.requestPermissions(this, permissions, 1234);
            }
        }else {
            ActivityCompat.requestPermissions(this, permissions, 1234);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermission = false;

        switch (requestCode) {
            case 1234: {
                if (grantResults.length > 0) {
                    for (int i=0;i<grantResults.length;i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermission = false;
                            return;
                        }
                    }
                    mLocationPermission = true;
//                    initialise Map
                    initMap();
                }
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        LocationService.MyBinder myBinder = (LocationService.MyBinder) service;
        locationService = myBinder.getService();

        if(locationService != null && locationService.isRunning()){
            btnStart.setText("Stop service");
        }else btnStart.setText("Start");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        locationService = null;
    }
}