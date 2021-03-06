/*
 * Copyright (c) 2015 Vamo Junto. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Vamo Junto
 * ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Vamo Junto.
 *
 * VAMO JUNTO MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. VAMO JUNTO SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * See LICENSE.txt
 */

package co.vamojunto.helpers;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
import co.vamojunto.model.Place;
import co.vamojunto.util.Globals;

/**
 * Contém as funções que utilizam a Places API do Google.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class GooglePlacesHelper {
    private static final String TAG = "GooglePlacesHelper";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String TYPE_DETAILS = "/details";
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
    public Task<List<Place>> autocompleteAsync(final String input) {
        final Task<List<Place>>.TaskCompletionSource tcs = Task.create();

        Log.d(TAG, "Iniciando consulta por local usando a AutoComplete Places API");
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
                        sb.append("&components=country:br");
                        sb.append("&sensor=true");
                        sb.append("&language=pt_BR");
                        sb.append("&location=" + Globals.MANAUS_LAT + "," + Globals.MANAUS_LNG + "&radius=25000"); // Utilizado para priorizar os resultados em Manaus

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
                public Void then(Task<String> task) {
                    List<Place> resultList = null;
                    String jsonString = task.getResult();

                    try {
                        // Create a JSON object hierarchy from the results
                        JSONObject jsonObj = new JSONObject(jsonString);
                        resultList = parseJSONAutoComplete(jsonObj);

                        tcs.setResult(resultList);
                    } catch (JSONException e) {
                        Log.e(TAG, "Cannot process JSON results", e);

                        tcs.setError(e);
                    } finally {
                        requesting = false;
                    }

                    Log.d(TAG, "Consulta por local usando a AutoComplete Places API finalizada");
                    return null;
                }
            });
        } else {
            Log.d(TAG, "Uma consulta já está sendo realizada, a consulta foi cancelada.");
        }

        return tcs.getTask();
    }

    /**
     * Retrieves the coordinates of a given {@link Place}
     *
     * @param p Place to retrieve the coordinates.
     * @return {@link bolts.Task} containing the operation result.
     */
    public Task<LatLng> getLocationAsync(Place p) {
        final Task<LatLng>.TaskCompletionSource tcs = Task.create();

        Log.d(TAG, "[getLocationAsync] Iniciando busca por coordenadas de um local.");

        // builds the uri to request the place details for the GooglePlace API
        StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_DETAILS + OUT_JSON);
        sb.append("?key=" + API_KEY);
        sb.append("&placeid=" + p.getGooglePlaceId());

        HttpURLConnection conn = null;
        try {
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();

            // as the variable have to be final to be used inside the anonymous classes, and the conn
            // variable have to be initiated with null above the try .. catch, so I can close the
            // connection on the finally block, this finalConn variable was created to receive
            // the final open connection value, and be used inside the anonymous classes
            final HttpURLConnection finalConn = conn;

            // sends the request to GooglePlace API on background, and returns a JSONObject with
            // the given result
            Task.callInBackground(new Callable<JSONObject>() {
                @Override
                public JSONObject call() throws Exception {
                    InputStreamReader reader = new InputStreamReader(finalConn.getInputStream());

                    StringBuilder jsonResult = new StringBuilder();
                    // load the results into a StringBuilder
                    int read;
                    char[] buff = new char[1024];
                    while ((read = reader.read(buff)) != -1) {
                        jsonResult.append(buff, 0, read);
                    }

                    return new JSONObject(jsonResult.toString());
                }
            }).continueWith(new Continuation<JSONObject, Void>() {
                // called when the request to GooglePlaces API finishes
                @Override
                public Void then(Task<JSONObject> task) throws Exception {
                    if (! task.isFaulted() && ! task.isCancelled()) {
                        try {
                            JSONObject jsonObject = task.getResult();
                            JSONObject jsonGeometry = jsonObject.getJSONObject("result").getJSONObject("geometry");

                            double lat = jsonGeometry.getJSONObject("location").getDouble("lat");
                            double lng = jsonGeometry.getJSONObject("location").getDouble("lng");

                            tcs.setResult(new LatLng(lat, lng));
                        } catch (JSONException e) {
                            tcs.setError(e);

                            Log.e(TAG, "Cannot process JSON results", e);
                        }
                    } else if (task.isFaulted()) {
                        tcs.setError(task.getError());
                    }

                    Log.d(TAG, "[getLocationAsync] Finalizando busca por coordenadas de um local.");
                    return null;
                }
            });
        } catch (MalformedURLException e) {
            tcs.setError(e);
            Log.e(TAG, "[getLocationAsync] Error processing Places API URL", e);
        } catch (IOException e) {
            tcs.setError(e);
            Log.e(TAG, "[getLocationAsync] Error connecting to Places API", e);
        } finally {
            if (conn != null)
                conn.disconnect();
        }

        return tcs.getTask();
    }

    /**
     * Faz o parse do JSON retornado após uma consulta do tipo Autocomplete à Google Places API.
     *
     * @param jsonObj JSONObject contendo o retorno da requisição.
     * @return A lista de lugares retornados pela requisição, ou uma List vazia, caso nenhum lugar tenha sido retornado.
     * @throws JSONException Caso o JSON enviado para o método seja inválido, uma JSONException é
     *          repassada para o método chamador.
     */
    private List<Place> parseJSONAutoComplete(JSONObject jsonObj) throws JSONException {
        List<Place> resultList = null;

        JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

        // Instancia o arraylist do resultado com o tamanho da quantidade de resultados
        // retornados no json.
        resultList = new ArrayList<Place>(predsJsonArray.length());

        for (int i = 0; i < predsJsonArray.length(); i++) {
            // Obtém o place_id do local
            String placeId = predsJsonArray.getJSONObject(i).getString("place_id");

            // Faz o parse do campo terms de cada local
            JSONArray jsonTerms = predsJsonArray.getJSONObject(i).getJSONArray("terms");

            // O primeiro termo é sempre o título do local
            String titulo = jsonTerms.getJSONObject(0).getString("value");
            int tam = jsonTerms.length();

            StringBuilder sbEndereco = new StringBuilder();
            for (int j = 1; j < tam - 3; j++) {
                if (sbEndereco.toString().equals(""))
                    sbEndereco.append(jsonTerms.getJSONObject(j).getString("value"));
                else
                    sbEndereco.append(", " + jsonTerms.getJSONObject(j).getString("value"));
            }
            // Os três ultimos termos são sempre cidade, estado e país, fiz o parse
            // separado para manter o padrão exibido pelo Google na descrição do local
            if (tam > 3) {
                if (sbEndereco.toString().equals(""))
                    sbEndereco.append(jsonTerms.getJSONObject(tam - 3).getString("value"));
                else
                    sbEndereco.append(", " + jsonTerms.getJSONObject(tam - 3).getString("value"));
            }

            if (tam > 2) {
                if (sbEndereco.toString().equals(""))
                    sbEndereco.append(jsonTerms.getJSONObject(tam - 2).getString("value"));
                else
                    sbEndereco.append(" - " + jsonTerms.getJSONObject(tam - 2).getString("value"));
            }

            if (tam > 1) {
                if (sbEndereco.toString().equals(""))
                    sbEndereco.append(jsonTerms.getJSONObject(tam - 1).getString("value"));
                else
                    sbEndereco.append(", " + jsonTerms.getJSONObject(tam - 1).getString("value"));
            }

            resultList.add(new Place(titulo, sbEndereco.toString(), placeId));
        }

        return resultList;
    }
}
