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

/**
 * System's Ride Model
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
@ParseClassName("Ride")
public class Ride extends ParseObject {

    public static final String FIELD_DATETIME = "datetime";
    public static final String FIELD_DRIVER = "driver";
    public static final String FIELD_STARTING_POINT_LAT = "starting_point_lat";
    public static final String FIELD_STARTING_POINT_LNG = "starting_point_lng";
    public static final String FIELD_STARTING_POINT_TITLE = "starting_point_title";
    public static final String FIELD_DESTINATION_LAT = "destination_lat";
    public static final String FIELD_DESTINATION_LNG = "destination_lng";
    public static final String FIELD_DESTINATION_TITLE = "destination_title";
    public static final String FIELD_SEATS = "seats";
    public static final String FIELD_DETAILS = "details";

    private static Map<String, Ride> instances = new HashMap<String, Ride>();

    public static void storeInstance(String key, Ride value) {
        instances.put(key, value);
    }

    public static Ride getStoredInstance(String key) {
        Ride r = instances.get(key);
        instances.remove(key);

        return r;
    }

    /**
     * Required default constructor
     */
    public Ride() { }

    public Ride(Calendar datetime, User driver, int seats,
                String details, Place startingPoint, Place destination) {
        setDatetime(datetime);
        setDriver(driver);
        setSeatsAvailable(seats);
        setDetails(details);
        setStartingPoint(startingPoint);
        setDestination(destination);
    }

    public String getId() {
        return getObjectId();
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
     * Retrieves a list of rides, that have a specific user as driver, ie, retrieves all
     * the rides offered by that user.
     *
     * @param u The user that you want to recover the ride offers
     * @return A {@link bolts.Task}that finishes after the search, if all occurs well,
     *         this {@link bolts.Task} will contain a list of rides.
     */
    public static Task<List<Ride>> getByDriverAsync(final User u) {
        final Task<List<Ride>>.TaskCompletionSource tcs = Task.create();

        ParseQuery<Ride> query = ParseQuery.getQuery(Ride.class);

        // includes the driver data, to display on list screen
        query.include(FIELD_DRIVER);
        query.whereEqualTo(FIELD_DRIVER, u);

        query.findInBackground(new FindCallback<Ride>() {
            @Override
            public void done(List<Ride> rides, ParseException e) {
                if ( e == null ) {
                    tcs.setResult(rides);
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
    public static Task<List<Ride>> getFriendsOffersAsync(User currentUser) {
        final Task<List<Ride>>.TaskCompletionSource tcs = Task.create();

        // selects all friendships where the currentUser is the follower
        ParseQuery<Friendship> qFriendship = ParseQuery.getQuery(Friendship.class);
        qFriendship.whereEqualTo(Friendship.FIELD_FOLLOWER, currentUser);

        // gets all rides offered by the users followed by the currentUser
        ParseQuery<Ride> query = ParseQuery.getQuery(Ride.class);
        query.whereMatchesKeyInQuery(FIELD_DRIVER, Friendship.FIELD_FOLLOWING, qFriendship);

        // includes the driver data, to display on list screen
        query.include(FIELD_DRIVER);

        query.findInBackground(new FindCallback<Ride>() {
            @Override
            public void done(List<Ride> rides, ParseException e) {
                if ( e == null ) {
                    tcs.setResult(rides);
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
    public static Task<List<Ride>> getRidesAsPassengerAsync(User user) {
        final Task<List<Ride>>.TaskCompletionSource tcs = Task.create();

        // creates a query to look for all rows on RidePassenger table where user is the passenger
        // the query includes the ride field, thereby the query result will contain all rides
        // that user is passenger
        ParseQuery<RidePassenger> passengerQuery = ParseQuery.getQuery(RidePassenger.class);
        passengerQuery.whereEqualTo(RidePassenger.FIELD_PASSENGER, user);
        passengerQuery.include(RidePassenger.FIELD_RIDE);
        passengerQuery.include(RidePassenger.FIELD_RIDE + "." + Ride.FIELD_DRIVER);

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
                    List<Ride> resultList = new ArrayList<Ride>();

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
}
