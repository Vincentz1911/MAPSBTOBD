package com.vincentz1911.mapsandbtandobd2;

import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private Activity activity;
    private GoogleMap map;
    private float bearing = 0;

    public MapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
        View view = li.inflate(R.layout.fragment_map, vg, false);
        activity = getActivity();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Tools.msg(activity, "Permission is required for location");
            Tools.checkPermissions(activity);
            return;
        }

        map = googleMap;
        map.setMapStyle(new MapStyleOptions(getString(R.string.json_mapstyle)));
        map.setBuildingsEnabled(true);
        map.setTrafficEnabled(true);
        map.setMyLocationEnabled(true);

        UiSettings mUiSettings = map.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);

        LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) return;
        Location last = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        LatLng pos = last != null ? new LatLng(last.getLatitude(), last.getLongitude()) : null;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 10));

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                2000, 3, GPSlistener);

    }

    private LocationListener GPSlistener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location == null) return;

            LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
            bearing = location.getBearing() != 0.0 ? location.getBearing() : bearing;

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(pos)
                    .zoom(20 - location.getSpeed() / 6)
                    .bearing(bearing)
                    .tilt(70)
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }
        @Override
        public void onProviderEnabled(String provider) { }
        @Override
        public void onProviderDisabled(String provider) { }
    };
}
