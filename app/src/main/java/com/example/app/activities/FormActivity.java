package com.example.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.example.app.R;
import com.example.app.database.GestorBaseDatos;
import com.example.app.modelos.Incidencia;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FormActivity extends AppCompatActivity {
    private EditText etTitulo, etDescripcion;
    private RadioGroup rgUrgencia;
    private Button btnGuardar, btnMapa, btnFoto;
    private TextView txtCoords;
    private ImageView imgPreview;
    private GestorBaseDatos dbHelper;
    private int idEventoRecibido;
    private double lat = 0.0, lon = 0.0;
    private Uri photoURI;
    private String currentPhotoPath = null;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        rgUrgencia = findViewById(R.id.rgUrgencia);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnMapa = findViewById(R.id.btnUbicacionIncidencia);
        btnFoto = findViewById(R.id.btnTomarFoto);
        txtCoords = findViewById(R.id.txtCoordsIncidencia);
        imgPreview = findViewById(R.id.imgPreview);

        dbHelper = new GestorBaseDatos(this);
        idEventoRecibido = getIntent().getIntExtra("ID_EVENTO_SELECCIONADO", 1);

        ActivityResultLauncher<Intent> mapaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), res -> {
                    if (res.getResultCode() == RESULT_OK && res.getData() != null) {
                        lat = res.getData().getDoubleExtra("LATITUD", 0.0);
                        lon = res.getData().getDoubleExtra("LONGITUD", 0.0);
                        txtCoords.setText("✅ Guardado");
                    }
                });

        ActivityResultLauncher<Intent> camaraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), res -> {
                    if (res.getResultCode() == RESULT_OK) {
                        imgPreview.setVisibility(ImageView.VISIBLE);
                        imgPreview.setImageURI(photoURI);
                    }
                });

        btnMapa.setOnClickListener(v -> mapaLauncher.launch(new Intent(this, SeleccionarUbicacionActivity.class)));

        btnFoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
            } else abrirCamara(camaraLauncher);
        });

        btnGuardar.setOnClickListener(v -> guardarEnBackground());
    }

    private void abrirCamara(ActivityResultLauncher<Intent> launcher) {
        Intent take = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (take.resolveActivity(getPackageManager()) != null) {
            try {
                File f = crearArchivo();
                photoURI = FileProvider.getUriForFile(this, "com.example.app.fileprovider", f);
                take.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                launcher.launch(take);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private File crearArchivo() throws IOException {
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File f = File.createTempFile("JPEG_" + time + "_", ".jpg", dir);
        currentPhotoPath = f.getAbsolutePath();
        return f;
    }

    private void guardarEnBackground() {
        String titulo = etTitulo.getText().toString();
        String desc = etDescripcion.getText().toString();
        if (titulo.isEmpty()) return;

        btnGuardar.setEnabled(false);
        btnGuardar.setText("GUARDANDO...");

        executor.execute(() -> {
            try { Thread.sleep(500); } catch (Exception e) {}
            int urg = 2;
            if (rgUrgencia.getCheckedRadioButtonId() == R.id.rbBaja) urg = 1;
            if (rgUrgencia.getCheckedRadioButtonId() == R.id.rbAlta) urg = 3;
            String fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

            Incidencia i = new Incidencia(titulo, desc, urg, fecha, idEventoRecibido, lat, lon, currentPhotoPath);
            long res = dbHelper.addIncidencia(i);

            runOnUiThread(() -> {
                if (res != -1) { Toast.makeText(this, "Guardado", Toast.LENGTH_SHORT).show(); finish(); }
                else { btnGuardar.setEnabled(true); }
            });
        });
    }
}