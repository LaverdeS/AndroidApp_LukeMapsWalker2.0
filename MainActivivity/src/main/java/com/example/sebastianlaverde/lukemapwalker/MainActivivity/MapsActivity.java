package com.example.sebastianlaverde.lukemapwalker.MainActivivity;

/**
 *
 * https://developers.google.com/android/reference/com/google/android/gms/maps/StreetViewPanoramaView
 * https://developers.google.com/maps/documentation/android-sdk/streetview
 * https://developer.android.com/training/wearables/data-layer/
 * https://github.com/googlesamples/android-DataLayer/blob/master/Application/src/main/java/com/example/android/wearable/datalayer/MainActivity.java
 */
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaOptions;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Wearable;

public class MapsActivity extends AppCompatActivity implements  OnStreetViewPanoramaReadyCallback,
                                                                OnMapReadyCallback,
                                                                GoogleMap.OnMapLongClickListener,
                                                                DataClient.OnDataChangedListener {

    private MapView mapView;
    private GoogleMap gmap;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";


    private static final LatLng SYDNEY = new LatLng(-33.87365, 151.20689);

    private StreetViewPanoramaView streetViewPanoramaView;
    private static final String STREETVIEW_BUNDLE = "StreetViewBundle";
    private StreetViewPanorama streetView;

    private static final String STREETVIEW_BUNDLE_KEY = "StreetViewBundleKey";

    private Wearable.WearableOptions wearableOptions = new Wearable.WearableOptions.Builder().setLooper(Looper.myLooper()).build();
    //private DataClient dataClient = Wearable.getDataClient(this, wearableOptions);

    private Marker marker;
    private boolean smartwatchMode = false;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        LinearLayout linearLayout = findViewById(R.id.streetView_layout);

        final StreetViewPanoramaOptions options = new StreetViewPanoramaOptions();
        if (savedInstanceState == null) {
            options.position(SYDNEY);
        }

        streetViewPanoramaView = new StreetViewPanoramaView(this, options);
        streetViewPanoramaView.getStreetViewPanoramaAsync(this);

        linearLayout.addView(streetViewPanoramaView,
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        Bundle mStreetViewBundle = null;
        if (savedInstanceState != null) {
            mStreetViewBundle = savedInstanceState.getBundle(STREETVIEW_BUNDLE);
        }
        streetViewPanoramaView.onCreate(mStreetViewBundle);
        streetViewPanoramaView.setVisibility(View.INVISIBLE);

        final Button button = findViewById(R.id.toSV);
        final Button playPauseButton = findViewById(R.id.playPause);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mapView.getVisibility() == View.VISIBLE){
                    streetViewPanoramaView.setVisibility(View.VISIBLE);
                    mapView.setVisibility(View.INVISIBLE);
                }
                else{
                    //TODO: check main acceleration axis to confirm choices
                    streetViewPanoramaView.setVisibility(View.INVISIBLE);
                    mapView.setVisibility(View.VISIBLE);
                }
                playPauseButton.setVisibility(streetViewPanoramaView.getVisibility());
            }
        });

        playPauseButton.setVisibility(streetViewPanoramaView.getVisibility());
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playPauseButton.getVisibility() == View.VISIBLE){
                    smartwatchMode = !smartwatchMode;
                    streetView.setPanningGesturesEnabled(smartwatchMode);
                    streetView.setZoomGesturesEnabled(smartwatchMode);
                    streetView.setUserNavigationEnabled(smartwatchMode);
                }
            }
        });

        Wearable.getDataClient(this).addListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getDataClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getDataClient(this).removeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Wearable.getDataClient(this).removeListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        LatLng ny = SYDNEY;
        marker = gmap.addMarker(new MarkerOptions().position(ny));
        gmap.moveCamera(CameraUpdateFactory.newLatLng(ny));
    }

    @Override
    public void onMapLongClick(LatLng point){
        this.gmap.clear();
        this.marker = this.gmap.addMarker(new MarkerOptions().position(point));
    }

    @Override
    public void onStreetViewPanoramaReady(final StreetViewPanorama streetViewPanorama) {
        streetView = streetViewPanorama;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);

        Bundle mStreetViewBundle = outState.getBundle(STREETVIEW_BUNDLE);
        if (mStreetViewBundle == null) {
            mStreetViewBundle = new Bundle();
            outState.putBundle(STREETVIEW_BUNDLE, mStreetViewBundle);
        }

    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        if(streetViewPanoramaView.getVisibility() == View.VISIBLE && smartwatchMode) {
            for (DataEvent event : dataEventBuffer) {
                Log.v("check", event.toString());
            }
        }
    }
}