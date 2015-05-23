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
import co.vamojunto.util.TextUtil;

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

    public static final String FIELD_REQUESTER = "requester";
    public static final String FIELD_DATETIME = "datetime";
    public static final String FIELD_STARTING_POINT_LAT = "starting_point_lat";
    public static final String FIELD_STARTING_POINT_LNG = "starting_point_lng";
    public static final String FIELD_STARTING_POINT_TITLE = "starting_point_title";
    public static final String FIELD_DESTINATION_LAT = "destination_lat";
    public static final String FIELD_DESTINATION_LNG = "destination_lng";
    public static final String FIELD_DESTINATION_TITLE = "destination_title";
    public static final String FIELD_DETAILS = "details";
    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_LC_STARTING_POINT_TITLE = "lc_starting_point_title";
    public static final String FIELD_LC_DESTINATION_TITLE = "lc_destination_title";

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
        put(FIELD_LC_STARTING_POINT_TITLE, TextUtil.normalize(startingPoint.getTitulo()));
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
        put(FIELD_LC_DESTINATION_TITLE, TextUtil.normalize(destino.getTitulo()));
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
        query.include(FIELD_REQUESTER);
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

    /**
     * Get a list of ride requests made by the friends of a given user.
     *
     * @param currentUser The user to find the friend's requests.
     * @return A {@link bolts.Task} containing the list of requests as result.
     * @since 0.1.0
     */
    public static Task<List<RideRequest>> getFriendsRequestsAsync(User currentUser) {
        // selects all friendships where the currentUser is the follower
        ParseQuery<Friendship> qFriendship = ParseQuery.getQuery(Friendship.class);
        qFriendship.whereEqualTo(Friendship.FIELD_FOLLOWER, currentUser);

        // gets all rides requested by the users followed by the currentUser
        ParseQuery<RideRequest> query = ParseQuery.getQuery(RideRequest.class);
        query.whereMatchesKeyInQuery(FIELD_REQUESTER, Friendship.FIELD_FOLLOWING, qFriendship);

        // includes the requester data, to display on list screen
        query.include(FIELD_REQUESTER);

        return query.findInBackground();
    }

    /**
     * Get a list of messages sent to this request.
     *
     * @since 0.1.0
     * @return A {@link Task} containing a {@link java.util.List} with all messages sent to
     *         this request as result.
     */
    public Task<List<RequestMessage>> getMessagesAsync() {
        ParseQuery<RequestMessage> query = ParseQuery.getQuery(RequestMessage.class);
        query.whereEqualTo(RequestMessage.FIELD_REQUEST, this);
        query.orderByAscending("createdAt");
        query.include(RequestMessage.FIELD_SENDER);

        return query.findInBackground();
    }

    public static Task<List<RideRequest>> getFilteredFriendsRequestsAsync(
            User user,
            Map<String, String> filterValues
    ) {
        ParseQuery<Friendship> qFriendship = ParseQuery.getQuery(Friendship.class);
        qFriendship.whereEqualTo(Friendship.FIELD_FOLLOWER, user);

        // gets all rides requested by the users followed by the user
        ParseQuery<RideRequest> query = ParseQuery.getQuery(RideRequest.class);
        query.whereMatchesKeyInQuery(FIELD_REQUESTER, Friendship.FIELD_FOLLOWING, qFriendship);

        // includes the requester data, to display on list screen
        query.include(FIELD_REQUESTER);

        query.orderByDescending(FIELD_CREATED_AT);

        // filter by starting point
        String startingPoint = filterValues.get(FIELD_LC_STARTING_POINT_TITLE);
        if (startingPoint != null) {
            query.whereStartsWith(FIELD_LC_STARTING_POINT_TITLE, startingPoint);
        }

        // filter by destination
        String destination = filterValues.get(FIELD_LC_DESTINATION_TITLE);
        if (destination != null) {
            query.whereStartsWith(FIELD_LC_DESTINATION_TITLE, destination);
        }

        return query.findInBackground();
    }

    /**
     * Get a list of rides requested by the users of a given company. The current user is excluded
     * from results.
     *
     * @param code The company code
     * @return The {@link bolts.Task} with the result.
     * @since 0.3.0
     */
    public static Task<List<RideRequest>> getRequestsByCompany(String code) {
        // select the company with the given code
        ParseQuery<Company> companyQuery = ParseQuery.getQuery(Company.class);
        companyQuery.whereEqualTo(Company.FIELD_CODE, code);

        // select all approved entries on the userCompany relation, where the company
        // are the wanted company
        ParseQuery<UserCompany> userCompanyQuery = ParseQuery.getQuery(UserCompany.class);
        userCompanyQuery.whereMatchesQuery(UserCompany.FIELD_COMPANY, companyQuery);
        userCompanyQuery.whereEqualTo(UserCompany.FIELD_STATUS, UserCompany.Status.APPROVED.getValue());

        ParseQuery<RideRequest> requestQuery = ParseQuery.getQuery(RideRequest.class);
        requestQuery.whereMatchesKeyInQuery(FIELD_REQUESTER, UserCompany.FIELD_USER, userCompanyQuery);
        requestQuery.whereNotEqualTo(FIELD_REQUESTER, User.getCurrentUser());
        requestQuery.include(FIELD_REQUESTER);
        requestQuery.orderByDescending(FIELD_CREATED_AT);

        return requestQuery.findInBackground();
    }

    /**
     * Get a list of rides requested by the users of a given company. The current user is excluded
     * from results.
     *
     * @param code The company code
     * @param filters The filters to apply on the requests search.
     * @return The {@link bolts.Task} with the result.
     * @since 0.3.0
     */
    public static Task<List<RideRequest>> getRequestsByCompany(String code, Map<String, String> filters) {
        // select the company with the given code
        ParseQuery<Company> companyQuery = ParseQuery.getQuery(Company.class);
        companyQuery.whereEqualTo(Company.FIELD_CODE, code);

        // select all approved entries on the userCompany relation, where the company
        // are the wanted company
        ParseQuery<UserCompany> userCompanyQuery = ParseQuery.getQuery(UserCompany.class);
        userCompanyQuery.whereMatchesQuery(UserCompany.FIELD_COMPANY, companyQuery);
        userCompanyQuery.whereEqualTo(UserCompany.FIELD_STATUS, UserCompany.Status.APPROVED.getValue());

        ParseQuery<RideRequest> requestQuery = ParseQuery.getQuery(RideRequest.class);
        requestQuery.whereMatchesKeyInQuery(FIELD_REQUESTER, UserCompany.FIELD_USER, userCompanyQuery);
        requestQuery.whereNotEqualTo(FIELD_REQUESTER, User.getCurrentUser());
        requestQuery.include(FIELD_REQUESTER);
        requestQuery.orderByDescending(FIELD_CREATED_AT);

        // filter by starting point
        String startingPointFIlter = filters.get(FIELD_LC_STARTING_POINT_TITLE);
        if (startingPointFIlter != null) {
            requestQuery.whereStartsWith(FIELD_LC_STARTING_POINT_TITLE, startingPointFIlter);
        }

        // filter by destination
        String destinationFilter = filters.get(FIELD_LC_DESTINATION_TITLE);
        if (destinationFilter != null) {
            requestQuery.whereStartsWith(FIELD_LC_DESTINATION_TITLE, destinationFilter);
        }

        return requestQuery.findInBackground();
    }
}
