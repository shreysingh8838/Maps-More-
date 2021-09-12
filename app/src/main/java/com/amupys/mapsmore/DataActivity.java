package com.amupys.mapsmore;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.Date;
import java.sql.Timestamp;

import static com.amupys.mapsmore.MainActivity.places;
import static com.amupys.mapsmore.MainActivity.userArray;

public class DataActivity extends AppCompatActivity {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        listView = findViewById(R.id.data_list);
        if(places != null){
            listView.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return places.size();
                }

                @Override
                public Object getItem(int position) {
                    return null;
                }

                @Override
                public long getItemId(int position) {
                    return 0;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = getLayoutInflater().inflate(R.layout.item_main, null);
                    Location location = places.get(position);

                    TextView textView = view.findViewById(R.id.item_loc);
                    TextView textView1 = view.findViewById(R.id.item_des);

                    textView.setText(location.getName()+" "+String.valueOf(location.getLongitude())+" "+String.valueOf(location.getLatitude()));

//                    Timestamp ts=new Timestamp(location.getTimeOfAcc());
//                    Date date=new Date(ts.getTime());
//                    System.out.println(date);
                    textView1.setText(location.getDescription());
                    return view;
                }
            });
        }
    }
}