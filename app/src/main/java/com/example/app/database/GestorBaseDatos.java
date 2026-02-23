package com.example.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.app.modelos.Evento;     // OJO: models o modelos según tu paquete
import com.example.app.modelos.Incidencia; // OJO: models o modelos según tu paquete
import java.util.ArrayList;
import java.util.List;

public class GestorBaseDatos extends SQLiteOpenHelper {

    // CAMBIAMOS NOMBRE PARA REINICIAR DB LIMPIA
    private static final String DATABASE_NAME = "SPOTTER.FINAL";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_INCIDENCIAS = "incidencias";
    private static final String TABLE_EVENTOS = "eventos";
    private static final String TABLE_USUARIOS = "usuarios";

    public GestorBaseDatos(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Tabla Eventos (Con Categoria)
        db.execSQL("CREATE TABLE " + TABLE_EVENTOS + " (" +
                "id_evento INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "ubicacion TEXT, " +
                "latitud REAL, " +
                "longitud REAL, " +
                "categoria TEXT)");

        // 2. Tabla Incidencias (Con Foto y Coordenadas)
        db.execSQL("CREATE TABLE " + TABLE_INCIDENCIAS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "titulo TEXT, " +
                "descripcion TEXT, " +
                "urgencia INTEGER, " +
                "fecha TEXT, " +
                "id_evento INTEGER, " +
                "latitud REAL, " +
                "longitud REAL, " +
                "foto TEXT, " +
                "FOREIGN KEY(id_evento) REFERENCES " + TABLE_EVENTOS + "(id_evento))");

        // 3. Tabla Usuarios
        db.execSQL("CREATE TABLE " + TABLE_USUARIOS + " (" +
                "id_usuario INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "email TEXT UNIQUE, " +
                "password TEXT)");

        insertarDatosPrueba(db);
    }

    private void insertarDatosPrueba(SQLiteDatabase db) {
        // Eventos con categorías
        db.execSQL("INSERT INTO " + TABLE_EVENTOS + " (nombre, ubicacion, latitud, longitud, categoria) VALUES ('Gamergy Madrid', 'IFEMA', 40.463, -3.616, 'Videojuegos')");
        db.execSQL("INSERT INTO " + TABLE_EVENTOS + " (nombre, ubicacion, latitud, longitud, categoria) VALUES ('Mad Cool', 'Villaverde', 40.354, -3.693, 'Musica')");
        db.execSQL("INSERT INTO " + TABLE_EVENTOS + " (nombre, ubicacion, latitud, longitud, categoria) VALUES ('Japan Weekend', 'Barcelona', 41.373, 2.151, 'Anime')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCIDENCIAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        onCreate(db);
    }

    // --- MÉTODOS ---

    public boolean registrarUsuario(String nombre, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("email", email);
        values.put("password", password);
        long result = db.insert(TABLE_USUARIOS, null, values);
        db.close();
        return result != -1;
    }

    public boolean validarLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USUARIOS + " WHERE email=? AND password=?", new String[]{email, password});
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return existe;
    }

    public long addIncidencia(Incidencia incidencia) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("titulo", incidencia.getTitulo());
        values.put("descripcion", incidencia.getDescripcion());
        values.put("urgencia", incidencia.getUrgencia());
        values.put("fecha", incidencia.getFecha());
        values.put("id_evento", incidencia.getIdEvento());
        values.put("latitud", incidencia.getLatitud());
        values.put("longitud", incidencia.getLongitud());
        values.put("foto", incidencia.getUrlFoto()); // Guardar foto
        long id = db.insert(TABLE_INCIDENCIAS, null, values);
        db.close();
        return id;
    }

    public List<Incidencia> getIncidenciasPorEvento(int idEvento) {
        List<Incidencia> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_INCIDENCIAS + " WHERE id_evento = ?", new String[]{String.valueOf(idEvento)});

        if (cursor.moveToFirst()) {
            do {
                Incidencia incidencia = new Incidencia();
                incidencia.setId(cursor.getInt(0));
                incidencia.setTitulo(cursor.getString(1));
                incidencia.setDescripcion(cursor.getString(2));
                incidencia.setUrgencia(cursor.getInt(3));
                incidencia.setFecha(cursor.getString(4));
                incidencia.setIdEvento(cursor.getInt(5));
                if(cursor.getColumnCount() > 6) {
                    incidencia.setLatitud(cursor.getDouble(6));
                    incidencia.setLongitud(cursor.getDouble(7));
                    incidencia.setUrlFoto(cursor.getString(8)); // Recuperar foto
                }
                lista.add(incidencia);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    public void crearNuevoEvento(String nombre, String ubicacion, double lat, double lon, String categoria) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("ubicacion", ubicacion);
        values.put("latitud", lat);
        values.put("longitud", lon);
        values.put("categoria", categoria);
        db.insert(TABLE_EVENTOS, null, values);
        db.close();
    }

    public List<Evento> obtenerTodosLosEventos() {
        List<Evento> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EVENTOS, null);
        if (cursor.moveToFirst()) {
            do {
                // Constructor: id, nombre, ubicacion, lat, lon, categoria
                lista.add(new Evento(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getDouble(3),
                        cursor.getDouble(4),
                        cursor.getString(5)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    // Métodos vacíos auxiliares
    public void eliminarEvento(int id) { SQLiteDatabase db=getWritableDatabase(); db.delete(TABLE_EVENTOS,"id_evento=?",new String[]{String.valueOf(id)}); db.close();}
    public void borrarIncidencia(int id) { SQLiteDatabase db=getWritableDatabase(); db.delete(TABLE_INCIDENCIAS,"id=?",new String[]{String.valueOf(id)}); db.close();}
    public void actualizarIncidencia(Incidencia i) { /*...*/ }
}