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

package co.vamojunto.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import bolts.Task;

/**
 * Utility class for tasks that involve network operations
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 2.0
 * @since 0.1.0
 */
public class NetworkUtil {

    /**
     * Checks if the user is connected to the Internet.
     *
     * @return <code>true</code> if the user is connected and <code>false</code> if not.
     * @since 0.1.0
     */
    public static boolean isConnected(Context c) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Make a POST request to a given based on a given URI.
     *
     * @param paramMap The params to send on the request.
     * @param url The URI.
     * @return A task containing the result of the request.
     */
    public static Task<String> postData(Map<String, String> paramMap, String url) {
        final Task<String>.TaskCompletionSource tcs = Task.create();

        // create a new HttpClient and Post Header
        final HttpClient httpclient = new DefaultHttpClient();
        final HttpPost httpPost = new HttpPost(url);

        try {
            // add the data
            List<NameValuePair> nameValuePairs = new ArrayList<>(paramMap.size());

            Iterator<Map.Entry<String, String>> it = paramMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> pair = it.next();
                nameValuePairs.add(new BasicNameValuePair(pair.getKey(), pair.getValue()));
                it.remove(); // avoids a ConcurrentModificationException
            }

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

            Task.callInBackground(new Callable<Void>() {
                @Override
                public Void call() {
                    try {
                        // execute HTTP Post Request
                        HttpResponse response = httpclient.execute(httpPost);
                        InputStream inputStream = response.getEntity().getContent();
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        StringBuilder stringBuilder = new StringBuilder();
                        String bufferedStrChunk;
                        while ((bufferedStrChunk = bufferedReader.readLine()) != null) {
                            stringBuilder.append(bufferedStrChunk);
                        }

                        tcs.setResult(stringBuilder.toString());
                    } catch (IOException e) {
                        Log.e("[postData]", "Error on send post data", e);
                        tcs.setError(e);
                    }

                    return null;
                }
            });
        } catch (IOException e) {
            tcs.setError(e);
            Log.e("[postData]", "Error on send post data", e);
        }

        return tcs.getTask();
    }

}
