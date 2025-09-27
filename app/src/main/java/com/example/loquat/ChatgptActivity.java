package com.example.loquat;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.loquat.network.ApiClient;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatgptActivity extends Activity {

    private static final String TAG = "ChatgptActivity";
    private ApiClient apiClient;
    private String SERVER_IP_ADDRESS = "192.168.50.1"; // Assuming the same server IP
    private final int SERVER_PORT = 8080;

    private TextInputEditText editTextApiKey;
    private TextInputEditText editTextAiServer;
    private Button buttonSaveApiKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatgpt_apikey_layout); // Links to the new layout

        // Initialize the ApiClient
        apiClient = new ApiClient();

        // Initialize UI components
        editTextApiKey = findViewById(R.id.editTextApiKey);
        editTextAiServer = findViewById(R.id.editTextAiServer);
        buttonSaveApiKey = findViewById(R.id.buttonSaveApiKey);

        // Set a click listener for the save button
        buttonSaveApiKey.setOnClickListener(v -> {
            String apiKey = editTextApiKey.getText().toString().trim();
            String aiServer = editTextAiServer.getText().toString().trim();

            if (apiKey.isEmpty()) {
                Toast.makeText(this, "API Key cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Call the new function with the input values
            apikey_input(apiKey, aiServer);
        });
    }

    /**
     * Creates a JSON object with API key and AI server, and sends it to the local server.
     * @param apikey The API key to be sent.
     * @param aiserver The AI server URL to be sent.
     */
    private void apikey_input(String apikey, String aiserver) {
        // 2. Create a JSON format for the two parameters
        JSONObject apiConfigJson = new JSONObject();
        try {
            // a. key name is apikey and aiserver
            apiConfigJson.put("apikey", apikey);
            apiConfigJson.put("aiserver", aiserver);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for API key config", e);
            Toast.makeText(this, "Internal error creating request.", Toast.LENGTH_SHORT).show();
            return;
        }

        String jsonPayload = apiConfigJson.toString();
        // 4. The request name is "apikey"
        String command = "apikey";

        Log.d(TAG, "Sending POST request to '" + command + "' with payload: " + jsonPayload);
        Toast.makeText(this, "Saving API Key...", Toast.LENGTH_SHORT).show();

        // 3. Use a POST request to send it to the server
        apiClient.postDataToLocalServer(
                SERVER_IP_ADDRESS,
                SERVER_PORT,
                command,       // The path segment for the request
                jsonPayload,   // The JSON string as the request body
                new ApiClient.ApiResponseListener() {
                    @Override
                    public void onSuccess(String responseBody) {
                        runOnUiThread(() -> {
                            Log.d(TAG, "API key saved successfully. Server response: " + responseBody);
                            Toast.makeText(ChatgptActivity.this, "API Key saved successfully!", Toast.LENGTH_LONG).show();
                            // Optionally, you could finish the activity or update the UI
                            // finish();
                        });
                    }
                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Failed to save API key: " + error);
                            Toast.makeText(ChatgptActivity.this, "Failed to save API Key: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );

                    // You can add more UI logic for this screen here
    }
}
