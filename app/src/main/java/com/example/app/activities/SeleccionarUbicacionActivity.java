package com.example.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.app.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class SeleccionarUbicacionActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitudSeleccionada = 0.0;
    private double longitudSeleccionada = 0.0;
    private Button btnConfirmar;

    // Cliente de ubicación (necesita la librería que acabamos de añadir)
    private FusedLocationProviderClient fusedLocationClient;
    private static final int CODIGO_PERMISO_UBICACION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar_ubicacion);

        btnConfirmar = findViewById(R.id.btnConfirmarUbicacion); // Asegúrate de que este ID existe en tu XML

        // Inicializamos el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map); // Asegúrate de que este ID existe en tu XML
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnConfirmar.setOnClickListener(v -> {
            if (latitudSeleccionada != 0.0) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("LATITUD", latitudSeleccionada);
                resultIntent.putExtra("LONGITUD", longitudSeleccionada);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Por favor, marca un punto en el mapa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // 1. Intentamos activar la ubicación del usuario
        habilitarMiUbicacion();

        // 2. Listener para cuando tocas el mapa (poner marcador)
        mMap.setOnMapClickListener(latLng -> {
            mMap.clear(); // Borra el anterior
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Ubicación Seleccionada")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            latitudSeleccionada = latLng.latitude;
            longitudSeleccionada = latLng.longitude;
            btnConfirmar.setEnabled(true); // Habilita el botón
        });
    }

    private void habilitarMiUbicacion() {
        // Comprobamos si tenemos permiso
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Si hay permiso, activamos la capa azul de "Mi Ubicación"
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true); // Botón de la diana

            // OBTENER LA ÚLTIMA POSICIÓN CONOCIDA Y MOVER CÁMARA
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng miPosicion = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miPosicion, 15f));
                            } else {
                                // Si el GPS está encendido pero no ha calculado posición aún
                                Toast.makeText(SeleccionarUbicacionActivity.this, "Esperando señal GPS...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        } else {
            // Si no tenemos permiso, lo pedimos
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_PERMISO_UBICACION);
        }
    }

    // Respuesta del usuario al diálogo de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_PERMISO_UBICACION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si dijo que SÍ, recargamos la lógica
                habilitarMiUbicacion();
            } else {
                Toast.makeText(this, "Permiso necesario para ver tu ubicación", Toast.LENGTH_SHORT).show();
            }
        }
    }
}