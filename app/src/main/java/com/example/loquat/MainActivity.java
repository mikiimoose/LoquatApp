package com.example.loquat;

import com.example.loquat.network.ApiClient;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.view.View;
import android.net.wifi.WifiManager; // For getting Wi-Fi info
import android.text.format.Formatter; // For IP address formatting
import android.widget.Toast;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private ApiClient apiClient;  // ✅ declare the ApiClient here
    private static final String TAG_MAIN = "MainActivity";
    private String SERVER_IP_ADDRESS = "192.168.50.1"; // !!! REPLACE WITH YOUR ACTUAL TARGET IP !!!
    private final int SERVER_PORT = 8080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Button to open ChatGPT activity
        Button button_chatgpt = findViewById(R.id.button_chatgpt);
        if (button_chatgpt != null) {
            button_chatgpt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ChatgptActivity.class);
                    startActivity(intent);
                }
            });
        }

        Button button_wifi_setup = findViewById(R.id.button_wifisetup);
        if (button_wifi_setup != null) {
            button_wifi_setup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create an Intent to start WifiSetupActivity
                    Intent intent = new Intent(MainActivity.this, WifiSetupActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}
