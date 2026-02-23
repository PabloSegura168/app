package com.example.app.controlador;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;
import com.example.app.activities.DetalleActivity; // Importante: Importamos la nueva actividad
import com.example.app.database.GestorBaseDatos;
import com.example.app.modelos.Incidencia;

import java.util.List;

public class ControladorIncidencias extends RecyclerView.Adapter<ControladorIncidencias.ViewHolder> {

    private List<Incidencia> listaIncidencias;
    private Context context;
    private GestorBaseDatos db;

    public ControladorIncidencias(List<Incidencia> listaIncidencias, Context context) {
        this.listaIncidencias = listaIncidencias;
        this.context = context;
        this.db = new GestorBaseDatos(context);
    }

    public void setListaIncidencias(List<Incidencia> nuevaLista) {
        this.listaIncidencias = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incidencia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Incidencia incidencia = listaIncidencias.get(position);

        // 1. Poner datos básicos
        holder.txtTitulo.setText(incidencia.getTitulo());
        holder.txtFecha.setText(incidencia.getFecha());

        // 2. Lógica de Colores de Urgencia
        if (incidencia.getUrgencia() == 3) { // Alta
            holder.txtUrgencia.setText("ALTA");
            holder.txtUrgencia.setTextColor(context.getColor(R.color.urgency_high));
        } else if (incidencia.getUrgencia() == 2) { // Media
            holder.txtUrgencia.setText("MEDIA");
            holder.txtUrgencia.setTextColor(context.getColor(R.color.urgency_mid));
        } else { // Baja
            holder.txtUrgencia.setText("BAJA");
            holder.txtUrgencia.setTextColor(context.getColor(R.color.urgency_low));
        }

        // 3. --- NUEVO: CLICK EN LA FILA PARA VER DETALLE ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalleActivity.class);

            // Pasamos TODOS los datos a la otra pantalla
            intent.putExtra("TITULO", incidencia.getTitulo());
            intent.putExtra("DESCRIPCION", incidencia.getDescripcion());
            intent.putExtra("URGENCIA", incidencia.getUrgencia());
            intent.putExtra("FECHA", incidencia.getFecha());
            intent.putExtra("FOTO", incidencia.getUrlFoto()); // ¡La ruta de la foto!

            context.startActivity(intent);
        });

        // 4. Botón Eliminar (Papelera)
        holder.btnEliminar.setOnClickListener(v -> {
            db.borrarIncidencia(incidencia.getId());
            // Eliminamos de la lista visual y notificamos
            listaIncidencias.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, listaIncidencias.size());
            Toast.makeText(context, "Incidencia eliminada", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return listaIncidencias.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtUrgencia, txtFecha;
        ImageButton btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloIncidencia);
            txtUrgencia = itemView.findViewById(R.id.txtUrgencia);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            btnEliminar = itemView.findViewById(R.id.btnEliminarIncidencia);
        }
    }
}