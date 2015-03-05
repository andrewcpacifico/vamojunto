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
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/**
 * A simple GeocodingHelper
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class GeocodingHelper {
    public static final String TAG = "GeocodingHelper";

    /**
     * Makes the reverse geocoding process, ie converts a pair of coordinates into an address
     *
     * @param context The context to instantiate the Geocoder.
     * @param latLng The pair of coordinates.
     * @return A {@link bolts.Task} containing the result of this work.
     */
    public static Task<Address> reverseGeocodeInBackground(Context context, final LatLng latLng) {
        final Task<Address>.TaskCompletionSource tcs = Task.create();

        final Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        Task.callInBackground(new Callable<List<Address>>() {
            @Override
            public List<Address> call() throws IOException {
                return geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            }
        }).continueWith(new Continuation<List<Address>, Void>() {
            @Override
            public Void then(Task<List<Address>> task) throws IOException {
                List<Address> lstEndereco = task.getResult();

                if ( lstEndereco != null ) {
                    tcs.setResult(lstEndereco.get(0));
                }

                return null;
            }
        });

        return tcs.getTask();
    }
}
