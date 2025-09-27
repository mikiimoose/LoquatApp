package com.example.loquat;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper; // Import Looper if using new Handler(Looper.getMainLooper())
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loquat.network.ApiClient;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray; // For parsing JSON array
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;


public class WifiSetupActivity extends AppCompatActivity {

    private ApiClient apiClient;  // ✅ declare the ApiClient here
    private static final String TAG_MAIN = "WifiSetup";
    private String SERVER_IP_ADDRESS = "192.168.50.1";
    private final int SERVER_PORT = 8080;
    private RecyclerView recyclerViewWifiNetworks;
    private TextInputEditText editTextSsid;
    private TextInputEditText editTextSecurity;
    private TextInputEditText editTextPassword;
    private Button buttonConnectWifi;
    private TextView textViewWifiStatus;

    // You'll need to create an Adapter for the RecyclerView
    // private WifiNetworkAdapter wifiNetworkAdapter;
    private List<ScanResult> wifiList = new ArrayList<>(); // To store scanned Wi-Fi networks

    private WifiNetworkAdapter wifiNetworkAdapter;
    private List<WifiNetworkItem> scannedNetworksList = new ArrayList<>();
    //private WifiManager wifiManager;
    private static final long WIFI_STATUS_UPDATE_INTERVAL = 5000; // 5 seconds in milliseconds

    private Handler statusUpdateHandler;
    private Runnable statusUpdateRunnable;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_setup); // Ensure your layout file is named wifi_setup.xml

        // Initialize UI Elements
        recyclerViewWifiNetworks = findViewById(R.id.recyclerViewWifiNetworks);
        editTextSsid = findViewById(R.id.editTextSsid);
        editTextSecurity = findViewById(R.id.editTextSecurity);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonConnectWifi = findViewById(R.id.buttonConnectWifi);
        buttonConnectWifi = findViewById(R.id.buttonConnectWifi);
        textViewWifiStatus = findViewById(R.id.textViewWifiStatus);

        // --- RecyclerView Setup ---
        recyclerViewWifiNetworks.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter with an empty list initially
        scannedNetworksList = new ArrayList<>(); // Ensure list is also initialized
        wifiNetworkAdapter = new WifiNetworkAdapter(this, scannedNetworksList, new WifiNetworkAdapter.OnNetworkClickListener() {
            @Override
            public void onNetworkClick(WifiNetworkItem network) {
                // This method is called when an item in the RecyclerView is clicked

                if (editTextSsid != null) {
                    editTextSsid.setText(network.getSsid()); // Populate SSID
                }

                if (editTextSecurity != null) {
                    editTextSecurity.setText(network.getSecurity()); // Populate SSID
                }

                if (editTextPassword != null) {
                    editTextPassword.setText(""); // Optionally clear the password field
                    editTextPassword.requestFocus(); // Optionally move focus to password
                }

                // You can also add a Toast or log message for confirmation
                //Toast.makeText(WifiSetupActivity.this, "Selected: " + network.getSsid(), Toast.LENGTH_SHORT).show();
                Log.d(TAG_MAIN, "Clicked network: " + network.getSsid() + ", Security: " + network.getSecurity());

                // Depending on the security type, you might want to automatically focus the password field
                // if (!"OPEN".equalsIgnoreCase(network.getSecurity()) && !"NONE".equalsIgnoreCase(network.getSecurity())) {
                //    if (editTextPassword != null) {
                //        editTextPassword.requestFocus();
                //    }
                // }
            }
        });
        recyclerViewWifiNetworks.setAdapter(wifiNetworkAdapter); // << ADAPTER IS SET HERE

        // Initialize the handler (runs on the main UI thread)
        statusUpdateHandler = new Handler(Looper.getMainLooper()); // Ensures it runs on UI thread

        // Define the runnable task
        statusUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                // Call your method
                updateWifiStatus();

                // Schedule the same runnable to run again after the interval
                statusUpdateHandler.postDelayed(this, WIFI_STATUS_UPDATE_INTERVAL);
            }
        };
        // --- WifiManager Setup ---
        //wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // --- Button Click Listeners ---
        buttonConnectWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ssid = editTextSsid.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String security = editTextSecurity.getText().toString().trim();
                connectToWifi(ssid, security, password);
            }
        });

        textViewWifiStatus.setText("Fetching network info...");
        updateWifiStatus();

        // Get nearby WiFi
        scanWifiNetworks();
    }

    private void updateWifiStatus() {

        // Ensure apiClient is initialized (e.g., in onCreate: apiClient = new ApiClient();)
        if (apiClient == null) {
            apiClient = new ApiClient(); // Or however you manage its instance
        }

        apiClient.sendInfoToLocalServer(
                WifiSetupActivity.this, // The context
                SERVER_IP_ADDRESS,      // Your server's IP
                SERVER_PORT,            // Your server's port
                "get_net_info",         // The command string (now path segment)
                new ApiClient.ApiResponseListener() { // Implement the callback
                    @Override
                    public void onSuccess(String responseBody) {
                        // IMPORTANT: Update UI on the main thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG_MAIN, "Raw response from server: " + responseBody);
                                try {
                                    // Parse the JSON response
                                    JSONObject jsonResponse = new JSONObject(responseBody); // [2]

                                    // Extract values, providing default values if a key is missing
                                    String status = jsonResponse.optString("status", "Unknown Status");
                                    String ipAddress = jsonResponse.optString("ip_address", "N/A");
                                    String ssid = jsonResponse.optString("ssid", "Not Connected");

                                    // Now you can use these parsed values to update your UI
                                    String displayText;
                                    if ("disconnected".equals(status) || status.isEmpty() || "unknown status".equals(status)) {
                                        displayText = "WiFi is not connected";
                                    } else if ("connected".equals(status)) {
                                        displayText = "Connected to " + (ssid.isEmpty() ? "an unnamed network" : ssid);
                                        if (!ipAddress.isEmpty()) {
                                            displayText += ", IP is " + ipAddress;
                                        }
                                    } else {
                                        // Handle other potential status values or treat as unknown
                                        displayText = "Status: " + status; // Fallback for other statuses
                                        if (!ssid.isEmpty()) {
                                            displayText += " (" + ssid + ")";
                                        }
                                        if (!ipAddress.isEmpty()) {
                                            displayText += " [IP: " + ipAddress + "]";
                                        }
                                    }
                                    textViewWifiStatus.setText(displayText);

                                } catch (JSONException e) {
                                    Log.e(TAG_MAIN, "Error parsing JSON response: " + e.getMessage());
                                    textViewWifiStatus.setText("Status: Error parsing server data");
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        // IMPORTANT: Update UI on the main thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //androidx.media3.common.util.Log.e(TAG_MAIN, "Error fetching Wi-Fi status: " + error);
                                textViewWifiStatus.setText("Status: Error - " + error);
                            }
                        });
                    }
                }
        );
    }

    private void scanWifiNetworks() {
        //textViewWifiStatus.setText("Status: Scanning for Wi-Fi networks..."); // Update status
        if (apiClient == null) {
            apiClient = new ApiClient();
        }

        // Assuming your server endpoint for scan results is "/get_scan_results"
        // and it returns a JSON array.
        apiClient.sendInfoToLocalServer(
                WifiSetupActivity.this,
                SERVER_IP_ADDRESS,
                SERVER_PORT,
                "get_scan_result", // Your path segment for scan results
                new ApiClient.ApiResponseListener() {
                    @Override
                    public void onSuccess(String responseBody) {
                        runOnUiThread(() -> {
                            Log.d(TAG_MAIN, "Raw scan result response: " + responseBody);
                            try {
                                JSONArray jsonArray = new JSONArray(responseBody);
                                scannedNetworksList.clear(); // Clear previous results

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject networkJson = jsonArray.getJSONObject(i);

                                    String ssid = networkJson.optString("ssid", "Unknown SSID");
                                    int bars = networkJson.optInt("bars", 0); // Assuming bars is an int (0-4 or similar)
                                    String security = networkJson.optString("security", "Open");

                                    WifiNetworkItem networkItem = new WifiNetworkItem(ssid, bars, security);
                                    scannedNetworksList.add(networkItem);
                                    Log.e(TAG_MAIN, "add one network Item " + ssid + " secu " + security);
                                }

                                // Update the RecyclerView Adapter
                                if (wifiNetworkAdapter != null) {
                                    Log.e(TAG_MAIN, "notify Adapter to update view");
                                    Log.e(TAG_MAIN, "scannedNetworksList size " + scannedNetworksList.size());
                                    wifiNetworkAdapter.notifyDataSetChanged(); // Or use DiffUtil for better performance
                                    //textViewWifiStatus.setText("Status: Scan complete. Found " + scannedNetworksList.size() + " networks.");

                                }

                            } catch (JSONException e) {
                                Log.e(TAG_MAIN, "Error parsing scan results JSON: " + e.getMessage());
                                textViewWifiStatus.setText("Status: Error parsing scan data.");
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Log.e(TAG_MAIN, "Error scanning Wi-Fi networks: " + error);
                            //textViewWifiStatus.setText("Status: Wi-Fi Scan Error - " + error);
                        });
                    }
                }
        );
    }

    private void connectToWifi(String ssid, String security, String password) {
        if (ssid.isEmpty()) {
            Toast.makeText(this, "Please enter Network Name (SSID)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (apiClient == null) {
            apiClient = new ApiClient();
        }

        // 1. Create a JSON format for the three parameters
        JSONObject wifiCredentials = new JSONObject();
        try {
            wifiCredentials.put("ssid", ssid);
            // If the security is empty, then the JSON security item is "Open"
            wifiCredentials.put("security", security.isEmpty() ? "Open" : security);
            // key name is psk for password
            wifiCredentials.put("psk", password);
        } catch (JSONException e) {
            Log.e(TAG_MAIN, "Error creating JSON for Wi-Fi credentials", e);
            Toast.makeText(this, "Internal error creating request.", Toast.LENGTH_SHORT).show();
            return;
        }

        String jsonPayload = wifiCredentials.toString();
        String command = "connect"; // 3. The request name is "connect"

        textViewWifiStatus.setText("Attempting to connect to " + ssid);
        Toast.makeText(this, "Sending connection request for " + ssid, Toast.LENGTH_LONG).show();
        Log.d(TAG_MAIN, "Sending POST request to " + command + " with payload: " + jsonPayload);

        // 2. Use a POST request to send it to the server
        apiClient.postDataToLocalServer(
                SERVER_IP_ADDRESS,
                SERVER_PORT,
                command, // The path segment for the request
                jsonPayload, // The JSON string as the request body
                new ApiClient.ApiResponseListener() {
                    @Override
                    public void onSuccess(String responseBody) {
                        runOnUiThread(() -> {
                            Log.d(TAG_MAIN, "Connection request successful: " + responseBody);
                            Toast.makeText(WifiSetupActivity.this, "Connection request sent successfully!", Toast.LENGTH_SHORT).show();
                            // The periodic status update will eventually show the new status.
                            // You can also trigger an immediate update if you want.
                            updateWifiStatus();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Log.e(TAG_MAIN, "Connection request failed: " + error);
                            Toast.makeText(WifiSetupActivity.this, "Connection request failed: " + error, Toast.LENGTH_LONG).show();
                            textViewWifiStatus.setText("Failed to send connection request.");
                        });
                    }
                }
        );


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start the periodic updates when the Activity is resumed
        startWifiStatusUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopWifiStatusUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ensure to remove any pending callbacks when the Activity is destroyed
        // to prevent memory leaks, though onPause should handle most cases.
        if (statusUpdateHandler != null && statusUpdateRunnable != null) {
            statusUpdateHandler.removeCallbacks(statusUpdateRunnable);
        }
    }

    private void startWifiStatusUpdates() {
        // Remove any existing callbacks to prevent multiple timers if onResume is called multiple times
        if (statusUpdateHandler != null && statusUpdateRunnable != null) {
            statusUpdateHandler.removeCallbacks(statusUpdateRunnable); // Good practice
        }
        // Post the initial runnable with no delay (it will call updateWifiStatus immediately)
        // and then it will schedule itself for 5 seconds later.
        // Or, if you want the first update AFTER 5 seconds, use postDelayed for the first call too.
        statusUpdateHandler.post(statusUpdateRunnable); // First run immediately
        // Alternatively, for first run after 5 seconds:
        // statusUpdateHandler.postDelayed(statusUpdateRunnable, WIFI_STATUS_UPDATE_INTERVAL);
        Log.d(TAG_MAIN, "Wi-Fi status updates started.");
    }

    private void stopWifiStatusUpdates() {
        if (statusUpdateHandler != null && statusUpdateRunnable != null) {
            statusUpdateHandler.removeCallbacks(statusUpdateRunnable);
        }
        Log.d(TAG_MAIN, "Wi-Fi status updates stopped.");
    }

}
