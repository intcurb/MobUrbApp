package br.com.infotransctd;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static br.com.infotransctd.App.CHANNEL_ID;

public class GPS_Service extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private String meansOfTransport;
    Date dataInicial, dataFinal;
    ArrayList<LocationData> listOfLocation;
    private LocationData tempLocation;
    double listOfSpeed = 0.0;
    int numberOfGetSpeed = 0;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRefLat;
    CountDownTimer count;
    Date dateNow;
    int interval = 120000, countDown = 1;   //interval = 2 minutes in millis and countDown is the steps to downgrade the millis
    String userId = "";
    String data = "";
    String initDate = "", finishDate = "";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public GPS_Service () {
        listOfLocation = new ArrayList<>();
        tempLocation = new LocationData();
        dataInicial = Calendar.getInstance().getTime();
        initDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(dataInicial);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        meansOfTransport = (String) intent.getExtras().get("meansOfTransport");
        Intent notificationIntent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MobUrb")
                .setContentText("O App está rodando!")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return START_STICKY;
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public void onDestroy() {
        super.onDestroy();

       try{
           dataFinal = Calendar.getInstance().getTime();
           finishDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(dataFinal);

           double averageOfSpeed = 0;
           if(locationManager != null){
               locationManager.removeUpdates(locationListener);
           }
           myRefLat = database.getReference("locations");
           if(listOfSpeed != 0 && numberOfGetSpeed != 0) {
               averageOfSpeed = listOfSpeed / numberOfGetSpeed;
           }

           LocationData location = listOfLocation.get(listOfLocation.size()-1);

           String cityName = hereLocation(location.getLatitude(), location.getLongitude());

           RouteDate routeDate = new RouteDate(initDate, finishDate, listOfLocation, averageOfSpeed, meansOfTransport, cityName);
           myRefLat.push().setValue(routeDate);

           myRefLat = database.getReference("locationsWeb"); //troca a referencia do banco

           myRefLat.child(userId).removeValue(); //remove o valor temporário do banco

           count.cancel(); //Para o contador para não chamar o método do push

       }catch(Exception e){
            String erro = e.getMessage();
       }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {

        userId = "user - " + Calendar.getInstance().getTime();  //define um nome para o node child

        count = new CountDownTimer(interval, countDown) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                myRefLat = database.getReference("locationsWeb"); //Seleciona o node do banco

                try {
                    LocationData location = tempLocation;

                    String cityName = hereLocation(location.getLatitude(), location.getLongitude());

                    RouteDate routeDate = new RouteDate(tempLocation, meansOfTransport, cityName);

                    myRefLat.child(userId).setValue(routeDate);

                    count.cancel(); //Cancela o contador
                    count.start();  //Chama o contador de novo
                } catch (Exception e) {
                    String erro = e.getMessage();
                }
            }
        };

        count.start(); //Starta o contador para subir a cada 2 minutos as locations no banco temporario


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Intent i = new Intent("location_update");
                dateNow = Calendar.getInstance().getTime();
                data = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(dateNow);
                LocationData locationDataTemp = new LocationData(location.getLatitude(), location.getLongitude());
                LocationData locationData = new LocationData(location.getLatitude(), location.getLongitude(), data);
                i.putExtra("coordinates", location.getLatitude()+" "+location.getLongitude()+" "+(location.getSpeed()*3.6));
                if (locationData != null && (locationDataTemp.getLatitude() != 0 || locationDataTemp.getLongitude() != 0)) {
                    numberOfGetSpeed++;
                    listOfLocation.add(locationData);
                    tempLocation = locationDataTemp;
                    listOfSpeed = listOfSpeed + (location.getSpeed() * 3.6);
                }
                sendBroadcast(i);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
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
}
