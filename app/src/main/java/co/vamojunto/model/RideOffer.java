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

import android.content.Context;
import android.content.SharedPreferences;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.util.Globals;
import co.vamojunto.util.TextUtil;

/**
 * System's Ride Model
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 4.0
 * @since 0.1.0
 */
@ParseClassName("Ride")
public class RideOffer extends ParseObject {

    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_DATETIME = "datetime";
    public static final String FIELD_DRIVER = "driver";
    public static final String FIELD_STARTING_POINT_LAT = "starting_point_lat";
    public static final String FIELD_STARTING_POINT_LNG = "starting_point_lng";
    public static final String FIELD_STARTING_POINT_TITLE = "starting_point_title";
    public static final String FIELD_LC_STARTING_POINT_TITLE = "lc_starting_point_title";
    public static final String FIELD_DESTINATION_LAT = "destination_lat";
    public static final String FIELD_DESTINATION_LNG = "destination_lng";
    public static final String FIELD_DESTINATION_TITLE = "destination_title";
    public static final String FIELD_LC_DESTINATION_TITLE = "lc_destination_title";
    public static final String FIELD_SEATS = "seats";
    public static final String FIELD_DETAILS = "details";
    public static final String FIELD_STATUS = "status";

    public enum Status {
        ACTIVE(1), CANCELLED(-1);

        private int mCode;

        private static Map<Integer, Status> mMap = new HashMap<>();

        static {
            mMap.put(1, ACTIVE);
            mMap.put(-1, CANCELLED);
        }

        Status(int code) {
            mCode = code;
        }

        public static Status valueOf(int code) {
            return mMap.get(code);
        }

        public int getValue() {
            return mCode;
        }
    }

    private static Map<String, RideOffer> instances = new HashMap<String, RideOffer>();

    public static void storeInstance(String key, RideOffer value) {
        instances.put(key, value);
    }

    public static RideOffer getStoredInstance(String key) {
        RideOffer r = instances.get(key);
        instances.remove(key);

        return r;
    }

    /**
     * Required default constructor
     */
    public RideOffer() { }

    public RideOffer(Calendar datetime, User driver, int seats,
                     String details, Place startingPoint, Place destination) {
        setDatetime(datetime);
        setDriver(driver);
        setSeatsAvailable(seats);
        setDetails(details);
        setStartingPoint(startingPoint);
        setDestination(destination);
        setStatus(Status.ACTIVE);
    }

    public String getId() {
        return getObjectId();
    }

    public Status getStatus() {
        return Status.valueOf(getInt(FIELD_STATUS));
    }

    public void setStatus(Status status) {
        put(FIELD_STATUS, status.getValue());
    }

    public boolean isActive() {
        return this.getStatus() == Status.ACTIVE;
    }

    public Calendar getDatetime() {
        Calendar c = Calendar.getInstance();
        c.setTime(getDate(FIELD_DATETIME));

        return c;
    }

    public void setDatetime(Calendar datetime) {
        put(FIELD_DATETIME, datetime.getTime());
    }

    public User getDriver() {
        return (User) getParseUser(FIELD_DRIVER);
    }

    public void setDriver(User motorista) {
        put(FIELD_DRIVER, motorista);
    }

    public int getSeatsAvailable() {
        return getInt(FIELD_SEATS);
    }

    public void setSeatsAvailable(int numLugares) {
        put(FIELD_SEATS, numLugares);
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
     * Get a list of messages sent to this ride offer.
     *
     * @return A {@link Task} containing a {@link java.util.List} with all messages sent to
     *         this request as result.
     */
    public Task<List<RideMessage>> getMessagesAsync() {
        ParseQuery<RideMessage> query = ParseQuery.getQuery(RideMessage.class);
        query.whereEqualTo(RideMessage.FIELD_OFFER, this);
        query.orderByAscending("createdAt");
        query.include(RideMessage.FIELD_SENDER);

        return query.findInBackground();
    }

    /**
     * Fetch the data of a ride offer with a given id.
     *
     * @param id The id of the offer.
     * @return A Task containing the fetched ride offer, if succeed.
     */
    public static Task<RideOffer> fetchData(String id) {
        ParseQuery<RideOffer> query = ParseQuery.getQuery(RideOffer.class);
        query.include(RideOffer.FIELD_DRIVER);
        return query.getInBackground(id);
    }

    /**
     * Cancel the ride offer. The driver can cancel the ride offer on the ride details screen. When
     * ge does that, all ride passengers are notified and the ride is marked as canceled on the
     * cloud.
     *
     * The ride is still visible for a couple of days (the number of days not defined yet), and will
     * be displayed as canceled to other app users, than it will be effectively deleted from the
     * database.
     *
     * @since 0.4.0
     */
    public Task<Void> cancel() {
        setStatus(Status.CANCELLED);

        Map<String, Object> params = new HashMap<>();
        params.put("id", getId());
        params.put("driver_name", getDriver().getName());

        return ParseCloud.callFunctionInBackground("cancelRideOffer", params);
    }

    /**
     * Retrieves a list of rides, that have a specific user as driver, ie, retrieves all
     * the rides offered by that user.
     *
     * @param u The user that you want to recover the ride offers
     * @return A {@link bolts.Task}that finishes after the search, if all occurs well,
     *         this {@link bolts.Task} will contain a list of rides.
     */
    public static Task<List<RideOffer>> getByDriverAsync(final User u) {
        final Task<List<RideOffer>>.TaskCompletionSource tcs = Task.create();

        ParseQuery<RideOffer> query = ParseQuery.getQuery(RideOffer.class);

        // includes the driver data, to display on list screen
        query.include(FIELD_DRIVER);
        query.whereEqualTo(FIELD_DRIVER, u);
        query.orderByDescending(FIELD_DATETIME);

        query.findInBackground(new FindCallback<RideOffer>() {
            @Override
            public void done(List<RideOffer> rideOffers, ParseException e) {
                if (e == null) {
                    tcs.setResult(rideOffers);
                } else {
                    tcs.setError(e);
                }
            }
        });

        return tcs.getTask();
    }

    /**
     * Get all passengers confirmed on this ride.
     *
     * @return A {@link bolts.Task} containing a {@link java.util.List} of all confirmed passengers.
     * @since 0.1.0
     */
    public Task<List<User>> getPassengers() {
        final Task<List<User>>.TaskCompletionSource tcs = Task.create();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("RidePassenger");
        query.whereEqualTo("ride", this);
        query.include("passenger");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> rides, ParseException e) {
                if ( e == null ) {
                    List<User> lst = new ArrayList<User>();
                    for (ParseObject o: rides)
                        lst.add((User) o.getParseObject("passenger"));

                    tcs.setResult(lst);
                } else {
                    tcs.setError(e);
                }
            }
        });

        return tcs.getTask();
    }

    /**
     * Gets a list of rides offered by the users followed by a given user. Usually this method is
     * called passing the currentUser as argument, to display the friends feed.
     *
     * @param currentUser The user to get the friends offers.
     * @return A {@link bolts.Task} containing the result of the operation.
     */
    public static Task<List<RideOffer>> getFriendsOffersAsync(User currentUser) {
        final Task<List<RideOffer>>.TaskCompletionSource tcs = Task.create();

        // selects all friendships where the currentUser is the follower
        ParseQuery<Friendship> qFriendship = ParseQuery.getQuery(Friendship.class);
        qFriendship.whereEqualTo(Friendship.FIELD_FOLLOWER, currentUser);

        // gets all rides offered by the users followed by the currentUser
        ParseQuery<RideOffer> query = ParseQuery.getQuery(RideOffer.class);
        query.whereMatchesKeyInQuery(FIELD_DRIVER, Friendship.FIELD_FOLLOWING, qFriendship);
        query.whereEqualTo(FIELD_STATUS, Status.ACTIVE.getValue());

        // includes the driver data, to display on list screen
        query.include(FIELD_DRIVER);
        query.orderByDescending(FIELD_DATETIME);

        query.findInBackground(new FindCallback<RideOffer>() {
            @Override
            public void done(List<RideOffer> rideOffers, ParseException e) {
                if ( e == null ) {
                    tcs.setResult(rideOffers);
                } else {
                    tcs.setError(e);
                }
            }
        });

        return tcs.getTask();
    }

    /**
     * Get a list of rides that a given user is confirmed as passenger.
     *
     * @param user The user to look for the rides.
     * @return A {@link bolts.Task} containing a {@link java.util.List} of rides as result.
     */
    public static Task<List<RideOffer>> getRidesAsPassengerAsync(User user) {
        final Task<List<RideOffer>>.TaskCompletionSource tcs = Task.create();

        // creates a query to look for all rows on RidePassenger table where user is the passenger
        // the query includes the ride field, thereby the query result will contain all rides
        // that user is passenger
        ParseQuery<RidePassenger> passengerQuery = ParseQuery.getQuery(RidePassenger.class);
        passengerQuery.whereEqualTo(RidePassenger.FIELD_PASSENGER, user);
        passengerQuery.include(RidePassenger.FIELD_RIDE);
        passengerQuery.include(RidePassenger.FIELD_RIDE + "." + RideOffer.FIELD_DRIVER);

        // run the query in background, on the query completion processes the method return
        passengerQuery.findInBackground().continueWith(new Continuation<List<RidePassenger>, Void>() {
            @Override
            public Void then(Task<List<RidePassenger>> task) throws Exception {
                if (task.isCancelled()) {
                    tcs.setCancelled();
                } else if (task.isFaulted()) {
                    tcs.setError(task.getError());
                } else {
                    // list containing all records on the RidePassenger table where the user is
                    // the passenger
                    List<RidePassenger> passengerList = task.getResult();

                    // the list to use as task result
                    List<RideOffer> resultList = new ArrayList<RideOffer>();

                    // iterate over the RidePassenger records to get only the rides, and adds them
                    // to resultList
                    for (RidePassenger passengerEntry : passengerList) {
                        resultList.add(passengerEntry.getRide());
                    }

                    tcs.setResult(resultList);
                }

                return null;
            }
        });

        return tcs.getTask();
    }

    /**
     * Get a filtered list of rides, offered by friends of a given user.
     *
     * @param user The user to get the friends offers.
     * @param filterValues Filters to apply on search.
     * @return A {@link bolts.Task} with the resulting list.
     * @see RideOffer#getFriendsOffersAsync
     * @since 0.1.0
     */
    public static Task<List<RideOffer>> getFilteredFriendsOffersAsync(
            User user,
            Map<String, String> filterValues
    ) {
        // selects all friendships where the user is the follower
        ParseQuery<Friendship> qFriendship = ParseQuery.getQuery(Friendship.class);
        qFriendship.whereEqualTo(Friendship.FIELD_FOLLOWER, user);

        // gets all rides offered by the users followed by the user
        ParseQuery<RideOffer> query = ParseQuery.getQuery(RideOffer.class);
        query.whereMatchesKeyInQuery(FIELD_DRIVER, Friendship.FIELD_FOLLOWING, qFriendship);
        query.whereEqualTo(FIELD_STATUS, Status.ACTIVE.getValue());

        // includes the driver data, to display on list screen
        query.include(FIELD_DRIVER);
        query.orderByDescending(FIELD_DATETIME);

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
     * Get a filtered list of rides, that a given user is confirmed as passenger.
     *
     * @param user The user to get the list of rides as a passenger.
     * @param filterValues The filters to apply on search.
     * @return A {@link Task} containing the filtered list of rides.
     * @see RideOffer#getRidesAsPassengerAsync(User)
     * @since 0.1.0
     */
    public static Task<List<RideOffer>> getFilteredRidesAsPassengerAsync(
            User user,
            Map<String, String> filterValues
    ) {
        final Task<List<RideOffer>>.TaskCompletionSource tcs = Task.create();

        // a query to filter the results by ride fields
        ParseQuery<RideOffer> rideQuery = ParseQuery.getQuery(RideOffer.class);

        // filter by starting point
        String startingPoint = filterValues.get(FIELD_LC_STARTING_POINT_TITLE);
        if (startingPoint != null) {
            rideQuery.whereStartsWith(
                    RideOffer.FIELD_LC_STARTING_POINT_TITLE,
                    TextUtil.normalize(startingPoint)
            );
        }

        // creates a query to look for all rows on RidePassenger table where user is the passenger
        // the query includes the ride field, thereby the query result will contain all rides
        // that user is passenger
        ParseQuery<RidePassenger> passengerQuery = ParseQuery.getQuery(RidePassenger.class);
        passengerQuery.include(RidePassenger.FIELD_RIDE);
        passengerQuery.include(RidePassenger.FIELD_RIDE + "." + RideOffer.FIELD_DRIVER);

        // filter by passenger
        passengerQuery.whereEqualTo(RidePassenger.FIELD_PASSENGER, user);

        // adds ride filters
        passengerQuery.whereMatchesQuery(RidePassenger.FIELD_RIDE, rideQuery);

        // run the query in background, on the query completion processes the method return
        passengerQuery.findInBackground().continueWith(new Continuation<List<RidePassenger>, Void>() {
            @Override
            public Void then(Task<List<RidePassenger>> task) throws Exception {
                if (task.isCancelled()) {
                    tcs.setCancelled();
                } else if (task.isFaulted()) {
                    tcs.setError(task.getError());
                } else {
                    // list containing all records on the RidePassenger table where the user is
                    // the passenger
                    List<RidePassenger> passengerList = task.getResult();

                    // the list to use as task result
                    List<RideOffer> resultList = new ArrayList<RideOffer>();

                    // iterate over the RidePassenger records to get only the rides, and adds them
                    // to resultList
                    for (RidePassenger passengerEntry: passengerList) {
                        resultList.add(passengerEntry.getRide());
                    }

                    tcs.setResult(resultList);
                }

                return null;
            }
        });

        return tcs.getTask();
    }

    /**
     * Get a list of rides offered by the users of a given company. The current user is excluded
     * from results.
     *
     * @param code The company code
     * @return The {@link bolts.Task} with the result.
     * @since 0.3.0
     */
    public static Task<List<RideOffer>> getOffersByCompany(String code) {
        // select the company with the given code
        ParseQuery<Company> companyQuery = ParseQuery.getQuery(Company.class);
        companyQuery.whereEqualTo(Company.FIELD_CODE, code);

        // select all approved entries on the userCompany relation, where the company
        // are the wanted company
        ParseQuery<UserCompany> userCompanyQuery = ParseQuery.getQuery(UserCompany.class);
        userCompanyQuery.whereMatchesQuery(UserCompany.FIELD_COMPANY, companyQuery);
        userCompanyQuery.whereEqualTo(UserCompany.FIELD_STATUS, UserCompany.Status.APPROVED.getValue());

        ParseQuery<RideOffer> rideQuery = ParseQuery.getQuery(RideOffer.class);
        rideQuery.whereMatchesKeyInQuery(FIELD_DRIVER, UserCompany.FIELD_USER, userCompanyQuery);
        rideQuery.whereNotEqualTo(FIELD_DRIVER, User.getCurrentUser());
        rideQuery.whereEqualTo(FIELD_STATUS, Status.ACTIVE.getValue());
        rideQuery.include(FIELD_DRIVER);
        rideQuery.orderByDescending(FIELD_DATETIME);

        return rideQuery.findInBackground();
    }

    /**
     * Get a list of rides offered by the users of a given company. The current user is excluded
     * from results.
     *
     * @param code The company code
     * @param filters Filters to apply on query.
     * @return The {@link bolts.Task} with the result.
     * @since 0.3.0
     */
    public static Task<List<RideOffer>> getOffersByCompany(String code, Map<String, String> filters) {
        // select the company with the given code
        ParseQuery<Company> companyQuery = ParseQuery.getQuery(Company.class);
        companyQuery.whereEqualTo(Company.FIELD_CODE, code);

        // select all approved entries on the userCompany relation, where the company
        // are the wanted company
        ParseQuery<UserCompany> userCompanyQuery = ParseQuery.getQuery(UserCompany.class);
        userCompanyQuery.whereMatchesQuery(UserCompany.FIELD_COMPANY, companyQuery);
        userCompanyQuery.whereEqualTo(UserCompany.FIELD_STATUS, UserCompany.Status.APPROVED.getValue());

        ParseQuery<RideOffer> rideQuery = ParseQuery.getQuery(RideOffer.class);
        rideQuery.whereMatchesKeyInQuery(FIELD_DRIVER, UserCompany.FIELD_USER, userCompanyQuery);
        rideQuery.whereNotEqualTo(FIELD_DRIVER, User.getCurrentUser());
        rideQuery.whereEqualTo(FIELD_STATUS, Status.ACTIVE.getValue());
        rideQuery.include(FIELD_DRIVER);
        rideQuery.orderByDescending(FIELD_DATETIME);

        // filter by starting point
        String startingPointFilter = filters.get(FIELD_LC_STARTING_POINT_TITLE);
        if (startingPointFilter != null) {
            rideQuery.whereStartsWith(FIELD_LC_STARTING_POINT_TITLE, startingPointFilter);
        }

        // filter by destination
        String destinationFilter = filters.get(FIELD_LC_DESTINATION_TITLE);
        if (destinationFilter != null) {
            rideQuery.whereStartsWith(FIELD_LC_DESTINATION_TITLE, destinationFilter);
        }

        return rideQuery.findInBackground();
    }

    /**
     * Increment a counter on the local datastore, for the number of seat requests sent to this
     * ride.
     *
     * @since 0.5.0
     */
    public static void incSeatRequests(Context context, String offerId) {
        String prefKey = "seatRequestCount" + offerId;
        SharedPreferences settings = context.getSharedPreferences(Globals.DEFAULT_PREF_NAME, 0);
        int currentCount = settings.getInt(prefKey, 0);

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(prefKey, currentCount+1);

        editor.apply();
    }

    /**
     * Set the seat request count for the ride offer to zero.
     *
     * @param context Context where this method was called.
     * @param offerId The id of the offer to clear.
     * @since 0.5.0
     */
    public static void clearSeatRequests(Context context, String offerId) {
        String prefKey = "seatRequestCount" + offerId;
        SharedPreferences settings = context.getSharedPreferences(Globals.DEFAULT_PREF_NAME, 0);

        SharedPreferences.Editor editor = settings.edit();
        editor.remove(prefKey);

        editor.apply();
    }

    /**
     * Return the seat request count for this ride offer.
     *
     * @param context Context where this method was called.
     * @param offerId The id of the offer.
     * @return The number of unread seat requests.
     * @since 0.5.0
     */
    public static int getSeatRequestsCount(Context context, String offerId) {
        String prefKey = "seatRequestCount" + offerId;
        SharedPreferences settings = context.getSharedPreferences(Globals.DEFAULT_PREF_NAME, 0);

        return settings.getInt(prefKey, 0);
    }

}
