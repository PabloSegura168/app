package com.example.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.example.app.R;
import com.example.app.database.GestorBaseDatos;
import com.example.app.modelos.Evento;
import java.util.List;

public class EventosActivity extends AppCompatActivity {
    private LinearLayout contenedorEventos;
    private Button btnAdmin;
    private GestorBaseDatos db;
    private boolean esAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventos);
        contenedorEventos = findViewById(R.id.contenedorEventos);
        btnAdmin = findViewById(R.id.btnAdminCrear);
        db = new GestorBaseDatos(this);
        esAdmin = getIntent().getBooleanExtra("ES_ADMIN", false);
        if (esAdmin) btnAdmin.setVisibility(View.VISIBLE);
        btnAdmin.setOnClickListener(v -> startActivity(new Intent(this, CrearEventoActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarListaEventos();
    }

    private void cargarListaEventos() {
        contenedorEventos.removeAllViews();
        List<Evento> lista = db.obtenerTodosLosEventos();

        for (Evento evento : lista) {
            View view = getLayoutInflater().inflate(R.layout.item_evento, contenedorEventos, false);
            AppCompatButton btnEntrar = view.findViewById(R.id.btnEntrarEvento);
            ImageButton btnBorrar = view.findViewById(R.id.btnBorrarEvento);

            btnEntrar.setText(evento.getNombre() + "\n" + evento.getUbicacion());

            // --- SOLUCIÓN: ASIGNACIÓN POR CATEGORÍA REAL ---
            String cat = evento.getCategoria();
            if (cat == null) cat = "Videojuegos"; // Valor por defecto por seguridad

            switch (cat) {
                case "Videojuegos":
                    btnEntrar.setTextColor(getColor(R.color.neon_blue));
                    btnEntrar.setBackgroundResource(R.drawable.btn_neon_blue_outline);
                    break;
                case "Musica":
                    btnEntrar.setTextColor(getColor(R.color.neon_pink));
                    btnEntrar.setBackgroundResource(R.drawable.btn_neon_pink_outline);
                    break;
                case "Anime":
                    btnEntrar.setTextColor(getColor(R.color.neon_purple));
                    btnEntrar.setBackgroundResource(R.drawable.btn_neon_purple_outline);
                    break;
                default:
                    btnEntrar.setTextColor(getColor(R.color.white));
                    btnEntrar.setBackgroundResource(R.drawable.btn_neon_blue_outline);
                    break;
            }

            btnEntrar.setOnClickListener(v -> irAlDashboard(evento));

            if (esAdmin) {
                btnBorrar.setVisibility(View.VISIBLE);
                btnBorrar.setOnClickListener(v -> mostrarDialogoBorrar(evento));
            }
            contenedorEventos.addView(view);
        }
    }

    private void mostrarDialogoBorrar(Evento evento) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Borrar evento?")
                .setPositiveButton("SÍ", (d, w) -> {
                    db.eliminarEvento(evento.getId());
                    cargarListaEventos();
                }).setNegativeButton("NO", null).show();
    }

    private void irAlDashboard(Evento evento) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("ID_EVENTO", evento.getId());
        intent.putExtra("NOMBRE_EVENTO", evento.getNombre());
        intent.putExtra("LATITUD_REAL", evento.getLatitud());
        intent.putExtra("LONGITUD_REAL", evento.getLongitud());
        intent.putExtra("CATEGORIA", evento.getCategoria());
        intent.putExtra("ES_ADMIN", esAdmin); // Mantenemos el rol
        startActivity(intent);
    }
}