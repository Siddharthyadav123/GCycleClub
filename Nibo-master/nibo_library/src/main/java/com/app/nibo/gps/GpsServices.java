package com.app.nibo.gps;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.app.nibo.R;
import com.app.nibo.origindestinationpicker.NiboOriginDestinationPickerActivity;

public class GpsServices extends Service implements LocationListener, GpsStatus.Listener {
    private LocationManager mLocationManager;

    Location lastlocation = new Location("last");
    Data data;

    double currentLon = 0;
    double currentLat = 0;
    double lastLon = 0;
    double lastLat = 0;

    PendingIntent contentIntent;


    @Override
    public void onCreate() {
        Intent notificationIntent = new Intent(this, NiboOriginDestinationPickerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        contentIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 0);
        updateNotification(false);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.addGpsStatusListener(this);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);

        Toast.makeText(this,"GpsService created ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        data = NiboOriginDestinationPickerActivity.getData();
        if (data.isRunning()) {
            currentLat = location.getLatitude();
            currentLon = location.getLongitude();

            lastlocation.setLatitude(lastLat);
            lastlocation.setLongitude(lastLon);

            if (data.isFirstTime()) {
                lastLat = currentLat;
                lastLon = currentLon;
                data.setFirstTime(false);
                data.firstLocationFound(currentLat, currentLon);
            }

            double distance = lastlocation.distanceTo(location);

            if (location.getAccuracy() < distance) {
                data.addDistance(distance);

                lastLat = currentLat;
                lastLon = currentLon;
            }

            if (location.hasSpeed()) {
                data.setCurSpeed(location.getSpeed() * 3.6);
                if (location.getSpeed() == 0) {
                    new isStillStopped().execute();
                }
            }
            data.update();
            updateNotification(true);
        }
    }

    public void updateNotification(boolean asData) {
        Notification.Builder builder = new Notification.Builder(getBaseContext())
                .setContentTitle(getString(R.string.running))
                .setSmallIcon(R.drawable.ic_map_marker_circle_def)
                .setContentIntent(contentIntent);

        if (asData) {
            builder.setContentText(String.format(getString(R.string.notification), data.getMaxSpeed(), data.getDistance()));
        } else {
            builder.setContentText(String.format(getString(R.string.notification), '-', '-'));
        }
        Notification notification = builder.build();
        startForeground(R.string.noti_id, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    /* Remove the locationlistener updates when Services is stopped */
    @Override
    public void onDestroy() {
        Toast.makeText(this,"GpsService onDestroy ", Toast.LENGTH_SHORT).show();
        mLocationManager.removeUpdates(this);
        mLocationManager.removeGpsStatusListener(this);
        stopForeground(true);
    }

    @Override
    public void onGpsStatusChanged(int event) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    class isStillStopped extends AsyncTask<Void, Integer, String> {
        int timer = 0;

        @Override
        protected String doInBackground(Void... unused) {
            try {
                while (data.getCurSpeed() == 0) {
                    Thread.sleep(1000);
                    timer++;
                }
            } catch (InterruptedException t) {
                return ("The sleep operation failed");
            }
            return ("return object when task is finished");
        }

        @Override
        protected void onPostExecute(String message) {
            data.setTimeStopped(timer);
        }
    }
}
