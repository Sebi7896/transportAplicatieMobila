package com.sebastian.licentafrontendtransport.Map;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sebastian.licentafrontendtransport.Map.model.Route;
import com.sebastian.licentafrontendtransport.Map.model.DirectionResponse;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class DirectionsFetchTask {
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public DirectionsFetchTask(Context context) {
        this.context = context;
    }

    public void fetchRoutes(String urlStr, Consumer<List<Route>> callback) {
        executor.execute(() -> {
            List<Route> routes;
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                in.close();

                // Parse the entire response using Gson
                Gson gson = new Gson();
                DirectionResponse directions = gson.fromJson(sb.toString(), DirectionResponse.class);
                routes = (directions != null && directions.routes != null) ? directions.routes : Collections.emptyList();
            } catch (Exception e) {
                routes = Collections.emptyList(); //nothing found
            }
            List<Route> result = routes;
            mainHandler.post(() -> {
                if (result.isEmpty()) {
                    Toast.makeText(context, "No routes found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, result.size() + " routes found", Toast.LENGTH_SHORT).show();
                }
                callback.accept(result);
            });
        });
    }
}