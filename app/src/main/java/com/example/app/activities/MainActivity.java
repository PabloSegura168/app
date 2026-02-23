package com.example.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Añadido
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.controlador.ControladorIncidencias;
import com.example.app.database.GestorBaseDatos;
import com.example.app.modelos.Incidencia; // Verifica que sea tu ruta correcta
import com.google.firebase.firestore.FirebaseFirestore; // Añadido

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ControladorIncidencias adapter;
    private GestorBaseDatos dbHelper;
    private int idEventoActual;
    private String nombreEvento, categoriaEvento;
    private double latitud, longitud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Recuperar datos del evento
        idEventoActual = getIntent().getIntExtra("ID_EVENTO", 1);
        nombreEvento = getIntent().getStringExtra("NOMBRE_EVENTO");
        categoriaEvento = getIntent().getStringExtra("CATEGORIA");
        latitud = getIntent().getDoubleExtra("LATITUD_REAL", 40.0);
        longitud = getIntent().getDoubleExtra("LONGITUD_REAL", -3.0);

        TextView txtTitulo = findViewById(R.id.txtNombreEvento);
        txtTitulo.setText("SPOTTER\n" + nombreEvento);

        // 2. BOTÓN NUEVA INCIDENCIA
        Button btnNueva = findViewById(R.id.btnIrFormulario);
        btnNueva.setVisibility(View.VISIBLE);
        btnNueva.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FormActivity.class);
            intent.putExtra("ID_EVENTO_SELECCIONADO", idEventoActual);
            startActivity(intent);
        });

        // 3. BOTÓN MAPA
        Button btnMapa = findViewById(R.id.btnVerMapa);
        btnMapa.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapaActivity.class);
            intent.putExtra("NOMBRE", nombreEvento);
            intent.putExtra("LATITUD", latitud);
            intent.putExtra("LONGITUD", longitud);
            intent.putExtra("ID_EVENTO", idEventoActual);
            intent.putExtra("CATEGORIA", categoriaEvento);
            startActivity(intent);
        });

        // 4. BOTÓN CHAT IA
        Button btnChat = findViewById(R.id.btnAbrirChat);
        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("NOMBRE_EVENTO", nombreEvento);
            intent.putExtra("CATEGORIA", categoriaEvento);
            intent.putExtra("ID_EVENTO", idEventoActual);
            startActivity(intent);
        });

        // 5. LISTA DE INCIDENCIAS
        recyclerView = findViewById(R.id.recyclerIncidencias);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dbHelper = new GestorBaseDatos(this);
        adapter = new ControladorIncidencias(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Sincronizar al abrir la actividad
        sincronizarConFirestore();
    }

    // EL MÉTODO DEBE IR AQUÍ, FUERA DE ONCREATE
    private void sincronizarConFirestore() {
        FirebaseFirestore cloudDb = FirebaseFirestore.getInstance();
        List<Incidencia> listaLocal = dbHelper.getIncidenciasPorEvento(idEventoActual);

        for (Incidencia inc : listaLocal) {
            Map<String, Object> data = new HashMap<>();
            data.put("titulo", inc.getTitulo());
            data.put("descripcion", inc.getDescripcion());
            data.put("urgencia", inc.getUrgencia());
            data.put("id_evento", inc.getIdEvento());
            data.put("latitud", inc.getLatitud());
            data.put("longitud", inc.getLongitud());

            cloudDb.collection("incidencias")
                    .document(String.valueOf(inc.getId()))
                    .set(data)
                    .addOnSuccessListener(aVoid -> Log.d("FIREBASE", "Sincronizado: " + inc.getTitulo()))
                    .addOnFailureListener(e -> Log.e("FIREBASE", "Error al subir", e));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar lista local
        adapter.setListaIncidencias(dbHelper.getIncidenciasPorEvento(idEventoActual));
        // Intentar sincronizar de nuevo al volver a la pantalla
        sincronizarConFirestore();
    }
}