package com.example.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.app.R;
import com.example.app.controlador.AdaptadorChat;
import com.example.app.modelos.Mensaje;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private AdaptadorChat adaptador;
    private List<Mensaje> historial;
    private EditText etMensaje;
    private Button btnEnviar;

    // VARIABLES DECLARADAS AQUÍ ARRIBA PARA QUE TODA LA CLASE LAS PUEDA USAR
    private String nombreEvento;
    private String categoria;

    // TODO: PON TU CLAVE DE GOOGLE AI STUDIO AQUÍ (⚠️ Cuidado con compartirla públicamente)
    private static final String API_KEY = "API_KEY_AQUI";

    // Usamos el modelo más rápido de Gemini
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // ASIGNAMOS LOS VALORES AQUÍ
        nombreEvento = getIntent().getStringExtra("NOMBRE_EVENTO");
        categoria = getIntent().getStringExtra("CATEGORIA");

        // Protección extra por si los datos llegan vacíos
        if (nombreEvento == null) nombreEvento = "este evento";
        if (categoria == null) categoria = "general";

        recycler = findViewById(R.id.recyclerChat);
        etMensaje = findViewById(R.id.etMensaje);
        btnEnviar = findViewById(R.id.btnEnviarMsg);

        historial = new ArrayList<>();
        adaptador = new AdaptadorChat(historial);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // Para que los mensajes salgan desde abajo
        layoutManager.setStackFromEnd(true);
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(adaptador);

        // Mensaje de bienvenida
        agregarMensaje("¡Hola! Soy la IA del evento " + nombreEvento + ". ¿Tienes alguna duda sobre este evento de " + categoria + "?", false);

        btnEnviar.setOnClickListener(v -> {
            String textoUsuario = etMensaje.getText().toString().trim();
            if (!textoUsuario.isEmpty()) {
                agregarMensaje(textoUsuario, true);
                etMensaje.setText(""); // Limpiar caja
                llamarGeminiAPI(textoUsuario);
            }
        });
    }

    private void agregarMensaje(String texto, boolean esMio) {
        historial.add(new Mensaje(texto, esMio));
        adaptador.notifyItemInserted(historial.size() - 1);
        recycler.smoothScrollToPosition(historial.size() - 1);
    }

    private void llamarGeminiAPI(String pregunta) {
        btnEnviar.setEnabled(false);

        // Hacemos todo el trabajo pesado en segundo plano
        executor.execute(() -> {
            try {
                // 1. ABRIMOS LA BASE DE DATOS Y BUSCAMOS LAS INCIDENCIAS
                int idEvento = getIntent().getIntExtra("ID_EVENTO", -1);
                com.example.app.database.GestorBaseDatos db = new com.example.app.database.GestorBaseDatos(ChatActivity.this);
                List<com.example.app.modelos.Incidencia> listaIncidencias = db.getIncidenciasPorEvento(idEvento);

                // 2. CREAMOS UN TEXTO RESUMIENDO LAS INCIDENCIAS PARA LA IA
                String resumenIncidencias = "";
                if (listaIncidencias.isEmpty()) {
                    resumenIncidencias = "Actualmente NO hay ninguna incidencia reportada. Todo funciona perfectamente en el evento.";
                } else {
                    resumenIncidencias = "Atención, actualmente hay " + listaIncidencias.size() + " incidencias reportadas en el evento:\n";
                    for (com.example.app.modelos.Incidencia inc : listaIncidencias) {
                        resumenIncidencias += "- " + inc.getTitulo() + " (Urgencia: " + inc.getUrgencia() + "). Descripción: " + inc.getDescripcion() + "\n";
                    }
                }

                // 3. SE LO INYECTAMOS AL PROMPT SECRETO
                String promptSecreto = "Eres el asistente oficial de un evento llamado '" + nombreEvento +
                        "' (Categoría: '" + categoria + "').\n\n" +
                        "INFORMACIÓN INTERNA DEL EVENTO:\n" + resumenIncidencias + "\n\n" +
                        "Responde a la siguiente pregunta del usuario de forma útil, amigable y breve (máximo 2 párrafos). Si te preguntan por problemas o incidencias, usa la información interna que te he dado. Pregunta: " + pregunta;

                // --- A PARTIR DE AQUÍ ES LA CONEXIÓN A GOOGLE (Igual que antes) ---
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject textPart = new JSONObject();
                textPart.put("text", promptSecreto);

                JSONArray partsArray = new JSONArray();
                partsArray.put(textPart);

                JSONObject contentObj = new JSONObject();
                contentObj.put("parts", partsArray);

                JSONArray contentsArray = new JSONArray();
                contentsArray.put(contentObj);

                JSONObject requestBody = new JSONObject();
                requestBody.put("contents", contentsArray);

                OutputStream os = conn.getOutputStream();
                os.write(requestBody.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int codigoRespuesta = conn.getResponseCode();

                if (codigoRespuesta == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String respuestaIA = jsonResponse.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    runOnUiThread(() -> {
                        agregarMensaje(respuestaIA, false);
                        btnEnviar.setEnabled(true);
                    });

                } else {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    br.close();

                    runOnUiThread(() -> {
                        agregarMensaje("Error de Google (" + codigoRespuesta + ").", false);
                        System.out.println("ERROR GEMINI: " + errorResponse.toString());
                        btnEnviar.setEnabled(true);
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    agregarMensaje("Error de conexión.", false);
                    btnEnviar.setEnabled(true);
                });
            }
        });
    }
}