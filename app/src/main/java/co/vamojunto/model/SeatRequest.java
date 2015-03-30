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

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bolts.Continuation;
import bolts.Task;

/**
 * Model class for a seat request
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
@ParseClassName("SeatRequest")
public class SeatRequest extends ParseObject {

    // constants to define the names of the fields of the class on the cloud
    private static final String FIELD_USER_ID = "user_id";
    private static final String FIELD_RIDE_ID = "ride_id";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_MESSAGE = "message";

    // flags used to map all valid SeatRequest states
    public static final int STATUS_WAITING = 0;
    public static final int STATUS_CONFIRMED = 1;
    public static final int STATUS_REJECTED = 2;

    /**
     * Used to transfer a SeatRequest instance between Activities. An Activity can store a SeatRequest
     * and give it a key, to another Activity can user this key to get the instance.
     */
    private static Map<String, SeatRequest> instances = new HashMap<String, SeatRequest>();

    /**
     * Stores a SeatRequest instance on a global context, then this instance can be transferred
     * between Activities.
     *
     * @param key A key for that the instance can be retrieved.
     * @param value The instance itself.
     */
    public static void storeInstance(String key, SeatRequest value) {
        instances.put(key, value);
    }

    /**
     * Returns a SeatRequest instance, stored by an Activity to send it to another.
     *
     * @param key The key of the stored instance.
     * @return The instance that have to be transferred between activities.
     */
    public static SeatRequest getStoredInstance(String key) {
        SeatRequest request = instances.get(key);
        instances.remove(key);

        return request;
    }

    public SeatRequest() {
        // required default constructor
    }

    /**
     * Constructor that inits the request fields. The status of request is changed to <i>waiting</i>
     * by default.
     *
     * @param u The requester.
     * @param r The ride wanted.
     * @param message The message sent by the requester.
     */
    public SeatRequest(User u, Ride r, String message) {
        this.setUser(u);
        this.setRide(r);
        this.setMessage(message);
        this.setStatus(STATUS_WAITING);
    }

    /**
     * Checks if there is some seat request already sent by this.user to this.ride
     *
     * @return A {@link bolts.Task} containing the result of this query, the result of the Task can be
     *         <code>true</code> if the user already sent a request to this ride, and <code>false</code>
     *         if not.
     */
    public Task<Boolean> exists() {
        final Task<Boolean>.TaskCompletionSource tcs = Task.create();

        final ParseQuery<SeatRequest> query = ParseQuery.getQuery(SeatRequest.class);

        query.whereEqualTo(FIELD_USER_ID, getUser());
        query.whereEqualTo(FIELD_RIDE_ID, getRide());

        query.findInBackground(new FindCallback<SeatRequest>() {
            @Override
            public void done(List<SeatRequest> requests, ParseException e) {
                if ( e == null ) {
                    tcs.setResult(requests.size() > 0);
                } else {
                    tcs.setError(e);
                }
            }
        });

        return tcs.getTask();
    }

    /**
     * Verifies if this seat request is still waiting for response
     *
     * @return <code>true</code> if the request is waiting, and <code>false</code> if it is not
     */
    public boolean isWaiting() {
        return this.getStatus() == STATUS_WAITING;
    }

    /**
     * Verifies if this seat request received a positive response by the ride driver.
     *
     * @return <code>true</code> if the seat on the rid is confirmed, and <code>false</code> if it is not
     */
    public boolean isConfirmed() {
        return this.getStatus() == STATUS_CONFIRMED;
    }

    /**
     * Verifies if this seat request was reject by the ride driver.
     *
     * @return <code>true</code> if the request was rejected, and <code>false</code> if it is not
     */
    public boolean isRejected() {
        return this.getStatus() == STATUS_REJECTED;
    }

    /**
     * Confirms the seat request on the ride.
     */
    public Task<Void> confirm() {
        // changes the status on this instance
        setStatus(STATUS_CONFIRMED);

        // define the options to send to cloud function
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("seatRequestId", getId());
        params.put("rideId", getRide().getId());

        // call cloud function to confirm the seat
        return ParseCloud.callFunctionInBackground("confirmSeatRequest", params);
    }

    /**
     * Rejects the seat request on the ride
     */
    public Task<Void> reject() {
        // changes the status on this instance
        setStatus(STATUS_REJECTED);

        // define the options to send to cloud function
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("seatRequestId", getId());

        // call cloud function to confirm the seat
        return ParseCloud.callFunctionInBackground("rejectSeatRequest", params);
    }

    /**
     * Gets a list of SeatRequests made to a specific ride, that still waiting for a response.
     *
     * @param r The ride to get the requests.
     * @return A {@link bolts.Task} that returns the list, if no error occurs..
     */
    public static Task<List<SeatRequest>> getWaitingByRide(Ride r) {
        final Task<List<SeatRequest>>.TaskCompletionSource tcs = Task.create();

        ParseQuery<SeatRequest> query = ParseQuery.getQuery(SeatRequest.class);
        // includes the user data, to display on the list screen
        query.include(FIELD_USER_ID);
        // filters by ride
        query.whereEqualTo(FIELD_RIDE_ID, r);
        // filters by status
        query.whereEqualTo(FIELD_STATUS, STATUS_WAITING);

        query.findInBackground(new FindCallback<SeatRequest>() {
            @Override
            public void done(List<SeatRequest> requests, ParseException e) {
                if ( e == null ) {
                    tcs.setResult(requests);
                } else {
                    tcs.setError(e);
                }
            }
        });

        return tcs.getTask();
    }

    public String getId() {
        return getObjectId();
    }

    public User getUser() {
        return (User) get(FIELD_USER_ID);
    }

    public void setUser(User u) {
        User myUser = User.createWithoutData(User.class, u.getId());

        put(FIELD_USER_ID, myUser);
    }

    public Ride getRide() {
        return (Ride) get(FIELD_RIDE_ID);
    }

    public void setRide(Ride r) {
        Ride myRide = Ride.createWithoutData(Ride.class, r.getId());

        put(FIELD_RIDE_ID, myRide);
    }

    private int getStatus() {
        return (int) get(FIELD_STATUS);
    }

    private void setStatus(int s) {
        put(FIELD_STATUS, s);
    }

    @Override
    public String toString() {
        return "User: " + getUser().getName() + ", Ride: [from: " + getRide().getStartingPoint().getTitulo()
                + ", to: " + getRide().getDestination().getTitulo() + "]";
    }

    public String getMessage() {
        return getString(FIELD_MESSAGE);
    }

    public void setMessage(String msg) {
        this.put(FIELD_MESSAGE, msg);
    }

    /**
     * Calls the cloud code that saves the SeatRequest on database, and pushes the notification
     * to driver.
     *
     * @return
     */
    public Task<Void> send() {
        // define the options to send to cloud function
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", getUser().getId());
        params.put("rideId", getRide().getId());

        // call cloud function to confirm the seat
        return ParseCloud.callFunctionInBackground("requestSeat", params);
    }
}