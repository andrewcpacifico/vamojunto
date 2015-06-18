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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/**
 * Helper to handle with Facebook actions.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 1.1.0
 */
public class FacebookHelper {

    private static final String TAG = "FacebookHelper";

    /**
     * Field for user email, on user json returned from GraphAPI.
     *
     * @since 0.1.1
     */
    public static final String USER_EMAIL = "email";

    /**
     * Field for user name, on user json returned from GraphAPI.
     *
     * @since 0.1.1
     */
    public static final String USER_NAME = "name";

    /**
     * Field for user id, on user json returned from GraphAPI.
     *
     * @since 0.1.1
     */
    public static final String USER_ID = "id";

    public static class  Permissions {
        public static String USER_EMAIL = "email";
        public static String USER_FRIENDS = "user_friends";
    }

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
     * Get the current user data from his Facebook account.
     *
     * @since 0.1.1
     */
    public static Task<JSONObject> getFBUserAsync() {
        final Task<JSONObject>.TaskCompletionSource tcs = Task.create();

        Log.i(TAG, "starting a ME request for Facebook...");
        GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                        Log.i(TAG, "ME request finished...");

                        if (jsonObject != null) {
                            tcs.setResult(jsonObject);
                        } else {
                            tcs.setError(new Exception(graphResponse.getError().getErrorMessage()));
                        }
                    }
                }
        ).executeAsync();

        return tcs.getTask();
    }


    /**
     * Gets the list with facebook ids of the friends of current user.
     *
     * @return A {@link bolts.Task} containing the list of ids of the user's friends.
     * @since 0.1.1
     */
    public static Task<List<String>> getMyFriendsAsync() {
        final Task<List<String>>.TaskCompletionSource tcs = Task.create();

        Log.i(TAG, "Looking for user friends...");

        GraphRequest.newMyFriendsRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(JSONArray jsonArray, GraphResponse graphResponse) {
                        Log.i(TAG, "My friends request finished...");

                        List<String> resultList;

                        try {
                            int nResults = jsonArray.length();
                            resultList = new ArrayList<>(nResults);

                            for (int i = 0; i < nResults; i++) {
                                JSONObject jsonUser = jsonArray.getJSONObject(i);
                                resultList.add(jsonUser.getString("id"));
                            }

                            tcs.setResult(resultList);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error on parsing json.", e);
                        }
                    }
                }
        ).executeAsync();

        return tcs.getTask();
    }

//    public static Intent getOpenFacebookIntent(Context context, String userFbId) {
//        PackageManager pm = context.getPackageManager();
//        Uri uri;
//        String facebookUrl = "https://www.facebook.com/app_scoped_user_id/" + userFbId;
//        try {
//            pm.getPackageInfo("com.facebook.katana", 0);
//            uri = Uri.parse("fb://facewebmodal/f?href=" + facebookUrl);
//        } catch (PackageManager.NameNotFoundException e) {
//            uri = Uri.parse(facebookUrl);
//        }
//        return new Intent(Intent.ACTION_VIEW, uri);
//    }

}