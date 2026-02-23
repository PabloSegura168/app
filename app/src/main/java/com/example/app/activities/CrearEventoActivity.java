package com.example.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.app.R;
import com.example.app.database.GestorBaseDatos;

public class CrearEventoActivity extends AppCompatActivity {
    // Inicializamos en 0.0 o un valor que indique "no seleccionado"
    private double lat = 0.0, lon = 0.0;
    private TextView txtCoords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_evento);

        // Referencias de la UI
        EditText etNombre = findViewById(R.id.etNombreEvento);
        EditText etUbicacion = findViewById(R.id.etUbicacionEvento);
        Button btnMapa = findViewById(R.id.btnAbrirMapaSelector);
        Button btnGuardar = findViewById(R.id.btnGuardarEvento);
        RadioGroup rgCat = findViewById(R.id.rgCategoriaEvento);
        txtCoords = findViewById(R.id.txtCoordenadas);

        GestorBaseDatos db = new GestorBaseDatos(this);

        // Launcher para recibir las coordenadas del mapa selector
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), res -> {
                    if (res.getResultCode() == RESULT_OK && res.getData() != null) {
                        lat = res.getData().getDoubleExtra("LATITUD", 0.0);
                        lon = res.getData().getDoubleExtra("LONGITUD", 0.0);
                        txtCoords.setText("📍 Ubicación seleccionada correctamente");
                        txtCoords.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                    }
                });

        // Botón para abrir el mapa
        btnMapa.setOnClickListener(v -> {
            Intent intent = new Intent(this, SeleccionarUbicacionActivity.class);
            launcher.launch(intent);
        });

        // Lógica de Guardado
        btnGuardar.setOnClickListener(v -> {
            String nom = etNombre.getText().toString().trim();
            String ubi = etUbicacion.getText().toString().trim();

            // Determinamos la categoría seleccionada
            String cat = "Videojuegos"; // Valor por defecto
            int selectedId = rgCat.getCheckedRadioButtonId();

            if (selectedId == R.id.rbAnime) {
                cat = "Anime";
            } else if (selectedId == R.id.rbMusica) {
                cat = "Musica";
            }

            // Validaciones básicas antes de guardar
            if (nom.isEmpty()) {
                etNombre.setError("El nombre es obligatorio");
                return;
            }

            if (lat == 0.0 && lon == 0.0) {
                Toast.makeText(this, "Por favor, selecciona una ubicación en el mapa", Toast.LENGTH_LONG).show();
                return;
            }

            // Guardar en la Base de Datos
            try {
                // Pasamos los parámetros: nombre, ubicación, lat, lon y la CATEGORÍA
                db.crearNuevoEvento(nom, ubi, lat, lon, cat);

                Toast.makeText(this, "Evento '" + nom + "' creado con éxito", Toast.LENGTH_SHORT).show();

                // Volver a la lista de eventos
                finish();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al guardar el evento", Toast.LENGTH_SHORT).show();
            }
        });
    }
}