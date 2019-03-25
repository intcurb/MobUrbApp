package br.com.infotransctd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RadioGroup rgVehicles;
    private Location globalLocation;
    private Button btnStart;
    private Button btnStop;
    private Button btnExit;
    private RadioButton car;
    private RadioButton motorcicle;
    private RadioButton bike;
    private RadioButton bus;
    private RadioButton truck;
    private RadioButton notInform;
    private Context context;
    private BroadcastReceiver broadcastReceiver;
    private static int marked = -1;
    private boolean flag = true;
    private String city = "Catanduva";

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableButtons();
            } else {
                runtime_permissions();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        FirebaseApp.initializeApp(this);
        getSupportActionBar().setElevation(0);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnExit = (Button) findViewById(R.id.btnExit);
        rgVehicles = (RadioGroup) findViewById(R.id.rgVehicles);
        car = (RadioButton) findViewById(R.id.car);
        motorcicle = (RadioButton) findViewById(R.id.motorcicle);
        bike = (RadioButton) findViewById(R.id.bike);
        bus = (RadioButton) findViewById(R.id.bus);
        truck = (RadioButton) findViewById(R.id.truck);
        notInform = (RadioButton) findViewById(R.id.not_inform);

        if (!runtime_permissions()) {
            enableButtons();
        }
    }

    private boolean runtime_permissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            return true;
        }
        return false;
    }

    private void enableButtons() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (rgVehicles.getCheckedRadioButtonId() != -1) //Checks if any radio is marked
                {
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                        return;
                    }

                    Location location = getLastBestLocation();
                    String cityName = "";
                    if (location != null) {
                        cityName = hereLocation(location.getLatitude(), location.getLongitude());
                    }

                    if (cityName.equals(city) || location == null) {
                        Intent i = new Intent(getApplicationContext(), GPS_Service.class);
                        i.putExtra("meansOfTransport", ((RadioButton) findViewById(rgVehicles.getCheckedRadioButtonId())).getText().toString());
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                            startService(i);
                        } else {
                            startForegroundService(i);
                        }

                        //Minimizes the app
                        MainActivity.this.moveTaskToBack(true);

                        //Get checked radio button
                        marked = rgVehicles.getCheckedRadioButtonId();

                        //Disable the button after the service started
                        btnStart.setEnabled(false);
                        btnStart.setBackgroundResource(R.drawable.background_button_start_disabled);

                        Toast.makeText(MainActivity.this, "O serviço foi iniciado!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Ops, você não está em Catanduva no momento. Você se encontra em: " + cityName, Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this, "Selecione um tipo de veículo!", Toast.LENGTH_SHORT).show();
                }


            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Stop the service
                Intent i = new Intent(getApplicationContext(), GPS_Service.class);
                stopService(i);
                Toast.makeText(MainActivity.this, "O serviço foi finalizado!", Toast.LENGTH_SHORT).show();

                btnStop.setEnabled(false);
                btnStop.setBackgroundResource(R.drawable.background_button_stop_disabled);
                btnStart.setEnabled(true);
                btnStart.setBackgroundResource(R.drawable.background_button_start);
                car.setEnabled(true);
                motorcicle.setEnabled(true);
                bike.setEnabled(true);
                bus.setEnabled(true);
                truck.setEnabled(true);
                notInform.setEnabled(true);
            }
        });

        if(isServiceRunning(GPS_Service.class)){
            btnStop.setEnabled(true);
            btnStop.setBackgroundResource(R.drawable.background_button_stop);
            car.setEnabled(false);
            motorcicle.setEnabled(false);
            bike.setEnabled(false);
            bus.setEnabled(false);
            truck.setEnabled(false);
            notInform.setEnabled(false);
        }
        else {
            btnStop.setEnabled(false);
            btnStop.setBackgroundResource(R.drawable.background_button_stop_disabled);
            car.setEnabled(true);
            motorcicle.setEnabled(true);
            bike.setEnabled(true);
            bus.setEnabled(true);
            truck.setEnabled(true);
            notInform.setEnabled(true);
        }

        //Close the app
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(isServiceRunning(GPS_Service.class)){
                Intent i = new Intent(getApplicationContext(), GPS_Service.class);
                i.putExtra("meansOfTransport", ((RadioButton) findViewById(rgVehicles.getCheckedRadioButtonId())).getText().toString());
                stopService(i);
            }
            finish();
            moveTaskToBack(true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Checks the radio select by user when the app resumes
        rgVehicles.check(marked);

        //Checks if the service is running and enable or disable the stop button
        btnStop.setEnabled(isServiceRunning(GPS_Service.class));
        if(isServiceRunning(GPS_Service.class)){
            btnStart.setEnabled(false);
            btnStart.setBackgroundResource(R.drawable.background_button_start_disabled);
            btnStop.setBackgroundResource(R.drawable.background_button_stop);
        }
        else{
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
            btnStart.setBackgroundResource(R.drawable.background_button_start);
            btnStop.setBackgroundResource(R.drawable.background_button_stop_disabled);
        }

        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
    }

    //Checks if a service is running
    private boolean isServiceRunning(Class<GPS_Service> serviceClass){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if(serviceClass.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    private String hereLocation(double lat, double lon){
        String cityName = "";

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try{
            addresses = geocoder.getFromLocation(lat, lon, 10);
            if(addresses.size() > 0) {
                for(Address addr: addresses) {
                    if(addr.getLocality() != null && addr.getLocality().length() > 0) {
                        cityName = addr.getLocality();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cityName;
    }

    private Location getLastBestLocation() {
        LocationManager mLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        @SuppressLint("MissingPermission") Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        @SuppressLint("MissingPermission") Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) {
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if (0 < GPSLocationTime - NetLocationTime) {
            return locationGPS;
        } else {
            return locationNet;
        }
    }
}
