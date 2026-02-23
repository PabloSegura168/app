package com.example.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.app.R;
import com.example.app.database.GestorBaseDatos;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etNombre = findViewById(R.id.etNombreReg);
        EditText etEmail = findViewById(R.id.etEmailReg);
        EditText etPass = findViewById(R.id.etPassReg);
        Button btnRegistrar = findViewById(R.id.btnRegistrar);
        TextView txtVolver = findViewById(R.id.txtVolverLogin);

        GestorBaseDatos db = new GestorBaseDatos(this);

        btnRegistrar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPass.getText().toString().trim();

            if(nombre.isEmpty() || email.isEmpty() || pass.isEmpty()){
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                boolean exito = db.registrarUsuario(nombre, email, pass);
                if(exito){
                    Toast.makeText(this, "¡Cuenta creada! Inicia sesión", Toast.LENGTH_LONG).show();
                    finish(); // Cierra registro y vuelve al login
                } else {
                    Toast.makeText(this, "El email ya está registrado", Toast.LENGTH_SHORT).show();
                }
            }
        });

        txtVolver.setOnClickListener(v -> finish());
    }
}