package com.example.loquat.network;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import android.content.Context;

public class ApiClient {
    private final OkHttpClient client = new OkHttpClient();
    private static final String TAG = "ApiClient";

    public interface ApiResponseListener {
        void onSuccess(String response);
        void onError(String error);
    }
    /**
     * Sends a command and the client's IP address to a local server.
     * This is the primary method for communicating with your C server.
     *
     * @param context    The application context, used for getting WifiManager and showing Toasts.
     * @param serverIp   The IP address of your C server (e.g., "192.168.4.1").
     * @param serverPort The port your C server is listening on (e.g., 8080).
     * @param pathSegment    The command to send (e.g., "get_status").
     */
    public void sendInfoToLocalServer(Context context, String serverIp, int serverPort, String pathSegment, ApiResponseListener listener) {
        // --- Step 1: Get the client's (this device's) IP address ---
        String clientIpAddress = getWifiIpAddress(context);
        if (clientIpAddress == null || clientIpAddress.equals("0.0.0.0")) {
            Log.e(TAG, "Could not get a valid client Wi-Fi IP address.");
            if (context instanceof AppCompatActivity) {
                ((AppCompatActivity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Error: Could not get phone's IP. Check Wi-Fi.", Toast.LENGTH_LONG).show());
            }
            return;
        }

        // --- Step 2: Build the URL with multiple query parameters ---
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme("http")
                .host(serverIp)
                .port(serverPort)
                .addPathSegment(pathSegment);

        String url = urlBuilder.build().toString();
        Log.d(TAG, "Requesting Local Server URL: " + url);

        // --- Step 3: Create and send the request ---
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Local Server Request Failed: " + e.getMessage(), e);
                if (context instanceof AppCompatActivity) { // Ensure UI updates are on the main thread
                    ((AppCompatActivity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Server Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        if (listener != null) {
                            listener.onError("Request Failed: " + e.getMessage());
                        }
                    });
                } else { // Handle if context is not an Activity, e.g., from a Service
                    if (listener != null) {
                        listener.onError("Request Failed: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    // It's important to get the response string regardless of success, for logging.
                    final String responseString = (responseBody != null) ? responseBody.string() : "Response body was null";

                    if (response.isSuccessful()) {
                        Log.d(TAG, "Local Server Success Code: " + response.code());
                        Log.d(TAG, "Local Server Response Body: " + responseString);

                        if (context instanceof AppCompatActivity) {
                            ((AppCompatActivity) context).runOnUiThread(() -> {
                                //Toast.makeText(context, "Server Response Received!", Toast.LENGTH_SHORT).show();
                                if (listener != null) {
                                    listener.onSuccess(responseString);
                                }
                            });
                        } else {
                            if (listener != null) {
                                listener.onSuccess(responseString);
                            }
                        }
                    } else {
                        Log.e(TAG, "Local Server Error Code: " + response.code() + ", Body: " + responseString);
                        if (context instanceof AppCompatActivity) {
                            ((AppCompatActivity) context).runOnUiThread(() -> {
                                //Toast.makeText(context, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                                if (listener != null) {
                                    listener.onError("Server Error: " + response.code() + " - " + responseString);
                                }
                            });
                        } else {
                            if (listener != null) {
                                listener.onError("Server Error: " + response.code() + " - " + responseString);
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Sends a generic GET request to any provided URL.
     * This can be used for testing or for simple API calls that don't need special parameters.
     *
     * @param context The application context.
     * @param url     The full URL to send the GET request to.
     */
    public void sendGetRequest(Context context, String url) {
        Log.d(TAG, "Sending generic GET request to: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Generic GET Request Failed: " + e.getMessage(), e);
                if (context instanceof AppCompatActivity) {
                    ((AppCompatActivity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Request Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    final String body = (responseBody != null) ? responseBody.string() : "Empty response body";

                    if (response.isSuccessful()) {
                        Log.d(TAG, "Generic GET Success. Response (first 100 chars): " + body.substring(0, Math.min(body.length(), 100)));
                        if (context instanceof AppCompatActivity) {
                            ((AppCompatActivity) context).runOnUiThread(() ->
                                    Toast.makeText(context, "Generic Request Success!", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Log.e(TAG, "Generic GET Error. Code: " + response.code() + ", Body: " + body);
                        if (context instanceof AppCompatActivity) {
                            ((AppCompatActivity) context).runOnUiThread(() ->
                                    Toast.makeText(context, "Generic Request Error: " + response.code(), Toast.LENGTH_SHORT).show());
                        }
                    }
                }
            }
        });
    }

    /**
     * Helper method to get the device's current Wi-Fi IP address.
     * Returns null if not connected to Wi-Fi or if an error occurs.
     * Requires the ACCESS_WIFI_STATE permission in AndroidManifest.xml.
     */
    private String getWifiIpAddress(Context context) {
        try {
            // Get the WifiManager service
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                // The IP address is an integer that needs to be formatted
                int ip = wifiManager.getConnectionInfo().getIpAddress();
                // The integer is stored in little-endian format, Formatter.formatIpAddress handles this conversion.
                return Formatter.formatIpAddress(ip);
            } else {
                Log.e(TAG, "WifiManager is null.");
                return null;
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Permission error getting IP Address. Did you add ACCESS_WIFI_STATE to AndroidManifest.xml?", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Exception getting IP Address", e);
            return null;
        }
    }

    public void postDataToLocalServer(
            String serverIp,
            int serverPort,
            String commandPath,
            String jsonPayload,
            ApiResponseListener listener) {

        new Thread(() -> {
            try {
                HttpUrl url = new HttpUrl.Builder()
                        .scheme("http")
                        .host(serverIp)
                        .port(serverPort)
                        .addPathSegment(commandPath)
                        .build();

                MediaType JSON = MediaType.get("application/json; charset=utf-g");
                RequestBody body = RequestBody.create(jsonPayload, JSON);

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                Log.d("ApiClient", "Requesting Local Server URL (POST): " + request.url());
                Log.d("ApiClient", "POST Payload: " + jsonPayload);

                try (Response response = client.newCall(request).execute()) {
                    String responseBody = response.body().string();
                    if (response.isSuccessful()) {
                        if (listener != null) {
                            listener.onSuccess(responseBody);
                        }
                    } else {
                        String errorMsg = "Error Code: " + response.code() + ", Body: " + responseBody;
                        if (listener != null) {
                            listener.onError(errorMsg);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("ApiClient", "Exception in postDataToLocalServer", e);
                if (listener != null) {
                    listener.onError("Network call failed: " + e.getMessage());
                }
            }
        }).start();
    }
}

