package com.example.loquat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.loquat.R; // Adjust if your R file is elsewhere
import java.util.List;

public class WifiNetworkAdapter extends RecyclerView.Adapter<WifiNetworkAdapter.ViewHolder> {

    private List<WifiNetworkItem> networkList;
    private LayoutInflater inflater;
    private OnNetworkClickListener clickListener;

    public interface OnNetworkClickListener {
        void onNetworkClick(WifiNetworkItem network);
    }

    public WifiNetworkAdapter(Context context, List<WifiNetworkItem> networkList, OnNetworkClickListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.networkList = networkList;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_wifi_network, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WifiNetworkItem network = networkList.get(position);
        holder.textViewNetworkName.setText(network.getSsid());

        // --- Logic for setting signal strength icon based on 'bars' ---
        // This is an example, you'll need appropriate drawables for signal levels
        int bars = network.getBars();
        if (bars <= 0) {
            holder.imageViewWifiSignal.setImageResource(R.drawable.ic_signal_wifi_3_bar); // Example
        } else if (bars == 1) {
            holder.imageViewWifiSignal.setImageResource(R.drawable.ic_signal_wifi_3_bar); // Example
        } else if (bars == 2) {
            holder.imageViewWifiSignal.setImageResource(R.drawable.ic_signal_wifi_3_bar); // Example
        } else if (bars == 3) {
            holder.imageViewWifiSignal.setImageResource(R.drawable.ic_signal_wifi_4_bar); // Example
        } else {
            holder.imageViewWifiSignal.setImageResource(R.drawable.ic_signal_wifi_4_bar); // Example (full signal)
        }
        // Ensure you have these drawables (e.g., ic_signal_wifi_0_bar, _1_bar, etc.)

        // --- Logic for setting security icon ---
        String security = network.getSecurity().toUpperCase();
        if (security.contains("WEP") || security.contains("WPA")) { // WPA, WPA2, WPA3
            holder.imageViewSecurityType.setImageResource(R.drawable.ic_lock); // Your lock icon
            holder.imageViewSecurityType.setVisibility(View.VISIBLE);
        } else { // OPEN network
            holder.imageViewSecurityType.setVisibility(View.INVISIBLE); // Or set a different "open" icon
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onNetworkClick(network);
            }
        });
    }

    @Override
    public int getItemCount() {
        return networkList == null ? 0 : networkList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNetworkName;
        ImageView imageViewWifiSignal;
        ImageView imageViewSecurityType;

        ViewHolder(View itemView) {
            super(itemView);
            textViewNetworkName = itemView.findViewById(R.id.textViewNetworkName);
            imageViewWifiSignal = itemView.findViewById(R.id.imageViewWifiSignal);
            imageViewSecurityType = itemView.findViewById(R.id.imageViewSecurityType);
        }
    }

    // Optional: Method to update data (e.g., if using DiffUtil later)
    public void updateData(List<WifiNetworkItem> newNetworkList) {
        this.networkList.clear();
        this.networkList.addAll(newNetworkList);
        notifyDataSetChanged(); // For simplicity, use DiffUtil for better performance on large lists
    }
}

