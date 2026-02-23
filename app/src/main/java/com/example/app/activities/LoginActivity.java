package com.example.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth; // Instancia de Firebase Auth
    private static final int RC_SIGN_IN = 9001;

    // Email del administrador para control de roles
    private static final String ADMIN_1 = "pablohsg168@gmail.com";

    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 1. CONFIGURACIÓN LOGIN MANUAL
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnEntrarManual = findViewById(R.id.btnEntrar);

        if (btnEntrarManual != null) {
            btnEntrarManual.setOnClickListener(v -> {
                String correo = etEmail.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                if (!correo.isEmpty() && !pass.isEmpty()) {
                    // Aquí podrías añadir mAuth.signInWithEmailAndPassword si usas Firebase Manual
                    Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();
                    comprobarRolYEntrar(correo);
                } else {
                    Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 2. CONFIGURAR GOOGLE SIGN-IN (USANDO TU WEB CLIENT ID)
        // Hemos puesto el ID directamente para evitar el error de R.string
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("881181221096-9ova9plt0a271tqqm8368ulj1tbebr4t.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Botón de Google
        Button btnGoogle = findViewById(R.id.btnGoogleLogin);
        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
        }

        // 3. BOTÓN PARA IR A REGISTRARSE
        Button btnIrRegistro = findViewById(R.id.btnIrRegistro);
        if (btnIrRegistro != null) {
            btnIrRegistro.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }
    }

    // Manejo del resultado del Intent de Google
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google dio permiso, obtenemos la cuenta
                GoogleSignInAccount account = task.getResult(ApiException.class);

                // Paso crítico: Autenticar este usuario en Firebase
                if (account != null) {
                    autenticarEnFirebaseConGoogle(account.getIdToken());
                }

            } catch (ApiException e) {
                // Si el error es code=10, revisa el SHA-1 en la consola de Firebase
                Log.w("GOOGLE_LOGIN", "signInResult:failed code=" + e.getStatusCode());
                Toast.makeText(this, "Error en Google (Code: " + e.getStatusCode() + ")", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Vincula la cuenta de Google con el sistema de usuarios de Firebase
    private void autenticarEnFirebaseConGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(this, "Bienvenido: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                            comprobarRolYEntrar(user.getEmail());
                        }
                    } else {
                        Log.e("FIREBASE_AUTH", "Error al vincular con Firebase", task.getException());
                        Toast.makeText(this, "Fallo en la autenticación de Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Lógica para decidir si el usuario entra como ADMIN o USER normal
    private void comprobarRolYEntrar(String email) {
        boolean esAdmin = false;

        // Si el email coincide con el definido arriba, le damos permisos de Admin
        if (email != null && email.equalsIgnoreCase(ADMIN_1)) {
            esAdmin = true;
        }

        Intent intent = new Intent(LoginActivity.this, EventosActivity.class);
        intent.putExtra("CORREO_USUARIO", email);
        intent.putExtra("ES_ADMIN", esAdmin);
        startActivity(intent);

        // Cerramos el login para que no puedan volver atrás
        finish();
    }
}