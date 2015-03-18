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

package co.vamojunto.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bolts.Task;

/**
 * The System's RideRequest Model
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
@ParseClassName("RideRequest")
public class RideRequest extends ParseObject {

    private static final String TAG ="model.RideRequest";

    private static final String FIELD_REQUESTER = "requester";
    private static final String FIELD_DATETIME = "datetime";
    private static final String FIELD_STARTING_POINT_LAT = "starting_point_lat";
    private static final String FIELD_STARTING_POINT_LNG = "starting_point_lng";
    private static final String FIELD_STARTING_POINT_TITLE = "starting_point_title";
    private static final String FIELD_DESTINATION_LAT = "destination_lat";
    private static final String FIELD_DESTINATION_LNG = "destination_lng";
    private static final String FIELD_DESTINATION_TITLE = "destination_title";
    private static final String FIELD_DETAILS = "details";

    private static Map<String, RideRequest> instances = new HashMap<String, RideRequest>();

    public static void storeInstance(String key, RideRequest value) {
        instances.put(key, value);
    }

    public static RideRequest getStoredInstance(String key) {
        RideRequest r = instances.get(key);
        instances.remove(key);

        return r;
    }

    /**
     * Required default constructor
     */
    public RideRequest() {}

    public RideRequest(User requester, Calendar datetime, String details, Place startingPoint, Place destination) {
        setRequester(requester);
        setDatetime(datetime);
        setDetails(details);
        setStartingPoint(startingPoint);
        setDestination(destination);
    }

    public String getId() {
        return getObjectId();
    }

    public User getRequester() {
        return (User) get(FIELD_REQUESTER);
    }

    public void setRequester(User u) {
        this.put(FIELD_REQUESTER, u);
    }

    public Calendar getDatetime() {
        Calendar c = Calendar.getInstance();
        c.setTime(getDate(FIELD_DATETIME));

        return c;
    }

    public void setDatetime(Calendar datetime) {
        put(FIELD_DATETIME, datetime.getTime());
    }

    public String getDetails() {
        return getString(FIELD_DETAILS);
    }

    public void setDetails(String details) {
        put(FIELD_DETAILS, details);
    }

    public Place getStartingPoint() {
        Place p = new Place(getDouble(FIELD_STARTING_POINT_LAT), getDouble(FIELD_STARTING_POINT_LNG));
        p.setTitulo(getString(FIELD_STARTING_POINT_TITLE));

        return p;
    }

    public void setStartingPoint(Place startingPoint) {
        put(FIELD_STARTING_POINT_LAT, startingPoint.getLatitude());
        put(FIELD_STARTING_POINT_LNG, startingPoint.getLongitude());
        put(FIELD_STARTING_POINT_TITLE, startingPoint.getTitulo());
    }

    public Place getDestination() {
        Place p = new Place(getDouble(FIELD_DESTINATION_LAT), getDouble(FIELD_DESTINATION_LNG));
        p.setTitulo(getString(FIELD_DESTINATION_TITLE));

        return p;
    }

    public void setDestination(Place destino) {
        put(FIELD_DESTINATION_LAT, destino.getLatitude());
        put(FIELD_DESTINATION_LNG, destino.getLongitude());
        put(FIELD_DESTINATION_TITLE, destino.getTitulo());
    }

    /**
     * Retrieves a list of rides requested by a specific user.
     *
     * @param u The user that you want to recover the ride requests
     * @return A {@link bolts.Task}that finishes after the search, if all occurs well,
     *         this {@link bolts.Task} will contain a list of ride requests.
     */
    public static Task<List<RideRequest>> getByRequesterAsync(final User u) {
        final Task<List<RideRequest>>.TaskCompletionSource tcs = Task.create();

        ParseQuery<RideRequest> query = ParseQuery.getQuery(RideRequest.class);
        query.whereEqualTo(FIELD_REQUESTER, u);

        query.findInBackground(new FindCallback<RideRequest>() {
            @Override
            public void done(List<RideRequest> requests, ParseException e) {
                if ( e == null ) {
                    tcs.setResult(requests);
                } else {
                    tcs.setError(e);
                }
            }
        });

        return tcs.getTask();
    }

}
