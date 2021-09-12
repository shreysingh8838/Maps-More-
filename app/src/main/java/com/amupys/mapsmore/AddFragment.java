package com.amupys.mapsmore;

import android.content.Context;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.amupys.mapsmore.MainActivity.userArray;


public class AddFragment extends BottomSheetDialogFragment {
    public static final int TIME_GAP = 1200000;
    public static final int MIN_CONSIDER_DISTANCE = 23;
    private Context context;
    private EditText location, description, speed, time;
    private Button btn_submit;
    private LatLng latLng;

    public AddFragment(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add, container, false);

        location = v.findViewById(R.id.user_name);
        description = v.findViewById(R.id.user_feedback);
        speed = v.findViewById(R.id.user_speed);
        time = v.findViewById(R.id.user_time);
        btn_submit = v.findViewById(R.id.btn_feed_send);

        time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 2){
                    time.setText(s+":");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_submit.setOnClickListener(v1 -> {
            String loc = location.getText().toString();
            String des = description.getText().toString();
            String tim = time.getText().toString();
            String num_speed = speed.getText().toString();

            if(loc.isEmpty() || des.isEmpty() || tim.isEmpty()){
                location.setError("Required");
                description.setError("Required");
                time.setError("Required");
            }else {
                addData(loc, des, tim, num_speed);
            }
        });
        return v;
    }

    private void addData(String loc, String des, String tim, String num_speed) {
        Toast.makeText(context, "Report submitted successfully", Toast.LENGTH_SHORT).show();
        dismiss();
        getLocationL();

        SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm");

        if(userArray != null && userArray.size() != 0){
            Location newLoc = null;
            boolean flag = false;
            try {
                Date date = inputFormat.parse(tim);
                Log.e("time", String.valueOf(date.getTime()));
                for (Location m: userArray){
                    newLoc = m;
                    try {
                        if ( LocationService.distance(new LatLng(m.getLatitude(), m.getLongitude()), latLng) > MIN_CONSIDER_DISTANCE) {
//                            new report creates a different location for addition in db
                            flag = true;
                        } else {
                            flag = false;
                            break;
                        }
                    }catch (Exception t){
                        t.printStackTrace();
                    }
                }
                if(flag){
                    addToServer(loc, des, tim, num_speed, latLng);
                }else {
//                    new report matches a previous record
                    if(date.getTime() - newLoc.getTimeOfAcc() >= TIME_GAP){
//                        if time gap between two report is greater than 20 min then it is considered a different report
                        newLoc.setNumAcc(newLoc.getNumAcc()+1);
                        // TODO: 11-09-2021 UPDATE IT TO SERVER
                    }
                }
            } catch (ParseException e) { e.printStackTrace(); }
        }else {
            addToServer(loc, des, tim, num_speed, latLng);
        }

    }

    public LatLng getLocationL() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        try {
                Task<android.location.Location> location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
                    @Override
                    public void onComplete(@NonNull Task<android.location.Location> task) {
                        if (task.isSuccessful()) {
                            android.location.Location location1 = (android.location.Location) task.getResult();
                            if (location1 != null) {
                                latLng = new  LatLng(location1.getLatitude(), location1.getLongitude());
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
        } catch (SecurityException e) {
        }
        return latLng;
    }

    private void addToServer(String loc, String des, String tim, String num_speed, LatLng latLng) {
        Location newLoc = new Location(loc, loc, des, latLng.latitude, latLng.longitude, 1, Integer.parseInt(num_speed));
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm");
            Date date = inputFormat.parse(tim);
            newLoc.setTimeOfAcc(date.getTime());
            Log.d("TIME TEST", String.valueOf(date.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        userArray.add(newLoc);
    }
}