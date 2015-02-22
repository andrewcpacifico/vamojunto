/*
 * Copyright (c) 2015. Vamo Junto Ltda. Todos os direitos reservados.
 *
 * Este software contém informações confidenciais e de propriedade da Vamo Junto Ltda,
 * ("Informações Confidenciais"). Você não deve divulgar tais informações, e deve usá-las somente em
 * conformidade com os termos do contrato de licença estabelecido entre você e a Vamo Junto Ltda.
 *
 * See LICENSE.txt
 */

package co.vamojunto.helpers;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;

/**
 * Contém as funções que utilizam a Places API do Google.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class GooglePlacesHelper {
    private static final String TAG = "GooglePlacesHelper";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private final String API_KEY;

    private static boolean requesting = false;

    public GooglePlacesHelper(Context context) {
        API_KEY = context.getString(R.string.google_maps_key_web);
    }

    /**
     * Faz uma requisição asíncrona à API do Google Places para autocompletar uma String.
     *
     * @param input String fornecida como entrada, para ser buscada pela Places API
     * @return {@link bolts.Task} correspondente à tarefa realizada.
     */
    public Task<List<String>> autocompleteAsync(final String input) {
        final Task<List<String>>.TaskCompletionSource tcs = Task.create();

        if ( !requesting ) {
            requesting = true;

            Task.callInBackground(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    HttpURLConnection conn = null;
                    StringBuilder jsonResults = new StringBuilder();

                    try {
                        StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
                        sb.append("?key=" + API_KEY);
                        sb.append("&input=" + URLEncoder.encode(input, "utf8"));

                        URL url = new URL(sb.toString());
                        conn = (HttpURLConnection) url.openConnection();

                        InputStreamReader in = new InputStreamReader(conn.getInputStream());

                        // Load the results into a StringBuilder
                        int read;
                        char[] buff = new char[1024];
                        while ((read = in.read(buff)) != -1) {
                            jsonResults.append(buff, 0, read);
                        }
                    } catch (MalformedURLException e) {
                        Log.e(TAG, "Error processing Places API URL", e);
                    } catch (IOException e) {
                        Log.e(TAG, "Error connecting to Places API", e);
                    } finally {
                        if (conn != null) {
                            conn.disconnect();
                        }
                    }

                    return jsonResults.toString();
                }
            }).continueWith(new Continuation<String, Void>() {
                @Override
                public Void then(Task<String> task) throws Exception {
                    List<String> resultList = null;
                    String jsonString = task.getResult();

                    try {
                        // Create a JSON object hierarchy from the results
                        JSONObject jsonObj = new JSONObject(jsonString);
                        JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

                        // Extract the Place descriptions from the results
                        resultList = new ArrayList<String>(predsJsonArray.length());
                        for (int i = 0; i < predsJsonArray.length(); i++) {
                            resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
                        }

                        tcs.setResult(resultList);
                    } catch (JSONException e) {
                        Log.e(TAG, "Cannot process JSON results", e);
                    } finally {
                        requesting = false;
                    }

                    return null;
                }
            });
        }

        return tcs.getTask();
    }
}
