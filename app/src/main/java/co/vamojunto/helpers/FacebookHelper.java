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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.ParseFacebookUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.model.User;

/**
 * Helper to deal with Facebook actions.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 1.0.0
 */
public class FacebookHelper {

    private static final String TAG = "FacebookHelper";

    /**
     * Returns the profile picture of a given user.
     *
     * @param userID The id of the user to get the image.
     * @return A {@link bolts.Task} containing the result of the action, that will be a {@link android.graphics.Bitmap}
     * containing the user profile picture, if no error occurs.
     * @throws IOException
     */
    public static Task<Bitmap> getProfilePictureAsync(String userID) throws IOException {
        final Task<Bitmap>.TaskCompletionSource tcs = Task.create();
        final String imageURL = "https://graph.facebook.com/" + userID + "/picture?type=large";

        Log.i(TAG, "Iniciando requisição para obtenção da imagem de perfil do usuário");
        Task.callInBackground(new Callable<Bitmap>() {
            public Bitmap call() throws IOException {
                InputStream in = new URL(imageURL).openConnection().getInputStream();

                return BitmapFactory.decodeStream(in);
            }
        }).continueWith(
                new Continuation<Bitmap, Object>() {
                    @Override
                    public Object then(Task<Bitmap> task) throws Exception {
                        Log.i(TAG, "Requisição para imagem de perfil do usuário finalizada");
                        tcs.setResult(task.getResult());

                        return null;
                    }
                }
        );

        return tcs.getTask();
    }

    /**
     * Faz uma requisição do tipo /me à API Graph do Facebook, e retorna um objeto do tipo GraphUser
     * com os dados do usuário que está autenticado.
     *
     * @return A instância da {@link bolts.Task} que contendo como resultado o GraphUser obtido na
     * requisição.
     */
    public static Task<GraphUser> getGraphUserAsync() {
        final Task<GraphUser>.TaskCompletionSource tcs = Task.create();

        Log.i(TAG, "Iniciando requisição para obtenção do email do usuário");
        Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser graphUser, Response response) {
                Log.i(TAG, "Requisição para email do usuário finalizada");

                if (graphUser != null) {
                    tcs.setResult(graphUser);
                }
            }
        }).executeAsync();

        return tcs.getTask();
    }


    /**
     * Gets the list of facebook ids, for the friends of a given user.
     *
     * @param user The user to get the friends.
     * @return A {@link java.util.List} containing the ids of the user's friends.
     * @since 0.1.0
     */
    public static Task<List<String>> getUserFriendsAsync(User user) {
        final String fbId = user.parseFacebookIdFromAuthData();
        final Task<List<String>>.TaskCompletionSource tcs = Task.create();

        Task.callInBackground(new Callable<String>() {
            @Override
            public String call() throws Exception {
                HttpURLConnection conn = null;
                StringBuilder jsonResults = new StringBuilder();

                try {
                    Request fbRequest = new Request(
                        ParseFacebookUtils.getSession(),
                        fbId + "/friends",
                        null,
                        HttpMethod.GET
                    );

                    conn = Request.toHttpConnection(fbRequest);

                    InputStreamReader in = new InputStreamReader(conn.getInputStream());

                    // Load the results into a StringBuilder
                    int read;
                    char[] buff = new char[1024];
                    while ((read = in.read(buff)) != -1) {
                        jsonResults.append(buff, 0, read);
                    }
                } catch (MalformedURLException e) {
                    Log.e(TAG, "[getUserFriendsAsync] Error processing URL", e);
                } catch (IOException e) {
                    Log.e(TAG, "[getUserFriendsAsync] Error connecting to Graph API", e);
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
                if (task.isCancelled()) {
                    tcs.setCancelled();
                } else if (task.isFaulted()) {
                    tcs.setError(task.getError());
                } else {

                    String jsonString = task.getResult();

                    try {
                        // Create a JSON object hierarchy from the results
                        JSONObject jsonObj = new JSONObject(jsonString);
                        List<String> resultList = parseUserFriendsJSON(jsonObj);

                        tcs.setResult(resultList);
                    } catch (JSONException e) {
                        Log.e(TAG, "Cannot process JSON results", e);

                        tcs.setError(e);
                    }
                }

                return null;
            }
        });

        return tcs.getTask();
    }

    /**
     * Parse the answer json, sent on a request to Facebook Graph API.
     *
     * @param jsonObj JSONObject containing the result from request.
     * @return A list containing the ids of the users.
     */
   private static List<String> parseUserFriendsJSON(JSONObject jsonObj) {
        List<String> resultList = null;

        try {
            JSONArray data = jsonObj.getJSONArray("data");

            int nResults = data.length();
            resultList = new ArrayList<>(nResults);

            for (int i = 0; i < nResults; i++) {
                JSONObject jsonUser = data.getJSONObject(i);
                resultList.add(jsonUser.getString("id"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "[parseUserFriendsJSON] Error on parsing json.");
        }

        return resultList;
    }

}