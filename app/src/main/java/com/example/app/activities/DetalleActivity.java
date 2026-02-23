package com.example.app.activities;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.app.R;
import java.io.File;

public class DetalleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        TextView txtTitulo = findViewById(R.id.txtDetalleTitulo);
        TextView txtDesc = findViewById(R.id.txtDetalleDescripcion);
        TextView txtUrgencia = findViewById(R.id.txtDetalleUrgencia);
        TextView txtFecha = findViewById(R.id.txtDetalleFecha);
        ImageView imgFoto = findViewById(R.id.imgDetalleFoto);
        Button btnVolver = findViewById(R.id.btnVolver);

        // 1. RECUPERAR DATOS DEL INTENT
        String titulo = getIntent().getStringExtra("TITULO");
        String desc = getIntent().getStringExtra("DESCRIPCION");
        int urgencia = getIntent().getIntExtra("URGENCIA", 2);
        String fecha = getIntent().getStringExtra("FECHA");
        String pathFoto = getIntent().getStringExtra("FOTO");

        // 2. PINTAR DATOS
        txtTitulo.setText(titulo);
        txtDesc.setText(desc);
        txtFecha.setText(fecha);

        // Colores urgencia
        if (urgencia == 3) {
            txtUrgencia.setText("ALTA 🔴");
            txtUrgencia.setTextColor(getColor(R.color.urgency_high));
        } else if (urgencia == 2) {
            txtUrgencia.setText("MEDIA 🟠");
            txtUrgencia.setTextColor(getColor(R.color.urgency_mid));
        } else {
            txtUrgencia.setText("BAJA 🟢");
            txtUrgencia.setTextColor(getColor(R.color.urgency_low));
        }

        // 3. CARGAR FOTO (SI EXISTE)
        if (pathFoto != null && !pathFoto.isEmpty()) {
            File imgFile = new File(pathFoto);
            if (imgFile.exists()) {
                imgFoto.setImageURI(Uri.fromFile(imgFile));
            } else {
                imgFoto.setImageResource(android.R.drawable.ic_menu_camera); // Icono por defecto si falla
            }
        } else {
            // Si no hay foto, ocultar o poner placeholder
            imgFoto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        btnVolver.setOnClickListener(v -> finish());
    }
}