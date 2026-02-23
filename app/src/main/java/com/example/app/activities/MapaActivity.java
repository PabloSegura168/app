package com.example.app.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.example.app.R;
import com.example.app.database.GestorBaseDatos;
import com.example.app.modelos.Incidencia;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.List;

public class MapaActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private String nombre, categoria;
    private double lat, lon;
    private int idEvento;
    private GestorBaseDatos db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        nombre = getIntent().getStringExtra("NOMBRE");
        categoria = getIntent().getStringExtra("CATEGORIA");
        lat = getIntent().getDoubleExtra("LATITUD", 0.0);
        lon = getIntent().getDoubleExtra("LONGITUD", 0.0);
        idEvento = getIntent().getIntExtra("ID_EVENTO", -1);
        db = new GestorBaseDatos(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Color según categoría
        float colorCat = BitmapDescriptorFactory.HUE_AZURE;
        if ("Anime".equals(categoria)) colorCat = BitmapDescriptorFactory.HUE_ROSE;
        else if ("Musica".equals(categoria)) colorCat = BitmapDescriptorFactory.HUE_YELLOW;
        else if ("Videojuegos".equals(categoria)) colorCat = BitmapDescriptorFactory.HUE_VIOLET;

        LatLng loc = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(loc).title(nombre).icon(BitmapDescriptorFactory.defaultMarker(colorCat)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16f));

        if (idEvento != -1) cargarIncidencias();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            mMap.setMyLocationEnabled(true);
        else ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    private void cargarIncidencias() {
        List<Incidencia> lista = db.getIncidenciasPorEvento(idEvento);
        for (Incidencia i : lista) {
            if (i.getLatitud() != 0.0) {
                float col = BitmapDescriptorFactory.HUE_GREEN;
                if (i.getUrgencia() == 2) col = BitmapDescriptorFactory.HUE_ORANGE;
                if (i.getUrgencia() == 3) col = BitmapDescriptorFactory.HUE_RED;
                mMap.addMarker(new MarkerOptions().position(new LatLng(i.getLatitud(), i.getLongitud())).title(i.getTitulo()).icon(BitmapDescriptorFactory.defaultMarker(col)));
            }
        }
    }
}