package com.example.app.controlador;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.app.R;
import com.example.app.modelos.Mensaje;
import java.util.List;

public class AdaptadorChat extends RecyclerView.Adapter<AdaptadorChat.ViewHolder> {

    private List<Mensaje> listaMensajes;

    public AdaptadorChat(List<Mensaje> listaMensajes) {
        this.listaMensajes = listaMensajes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensaje, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mensaje msg = listaMensajes.get(position);
        holder.txtBurbuja.setText(msg.getTexto());

        if (msg.isEsMio()) {
            // Mensaje del usuario (Derecha, Azul)
            holder.contenedor.setGravity(Gravity.END);
            holder.txtBurbuja.setBackgroundColor(0xFF007BFF); // Azul
        } else {
            // Mensaje de Gemini (Izquierda, Gris oscuro)
            holder.contenedor.setGravity(Gravity.START);
            holder.txtBurbuja.setBackgroundColor(0xFF333333); // Gris
        }
    }

    @Override
    public int getItemCount() { return listaMensajes.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout contenedor;
        TextView txtBurbuja;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            contenedor = itemView.findViewById(R.id.contenedorMensaje);
            txtBurbuja = itemView.findViewById(R.id.txtBurbuja);
        }
    }
}