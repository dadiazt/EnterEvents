package com.example.entereventsproject.Activities;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.example.entereventsproject.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtiene el mapa de google
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Cuando el mapa está listo le añadimos un marcador y hacemos zoom
        mMap = googleMap;
        mMap.setMinZoomPreference(12.0f);
        mMap.setMaxZoomPreference(28.0f);
        LatLng pista = new LatLng(41.44639969722975,2.244880199432373);
        mMap.addMarker(new MarkerOptions().position(pista).title("Pista de Hielo"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pista));
    }
}

