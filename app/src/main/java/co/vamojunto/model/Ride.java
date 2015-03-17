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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bolts.Task;

/**
 * System's Ride Model
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
@ParseClassName("Carona")
public class Ride extends ParseObject implements Parcelable {

    private static final String FIELD_DATETIME = "data_hora";
    private static final String FIELD_DRIVER = "motorista";
    private static final String FIELD_STARTING_POINT_LAT = "origem_lat";
    private static final String FIELD_STARTING_POINT_LNG = "origem_lng";
    private static final String FIELD_STARTING_POINT_TITLE = "origem_titulo";
    private static final String FIELD_DESTINATION_LAT = "destino_lat";
    private static final String FIELD_DESTINATION_LNG = "destino_lng";
    private static final String FIELD_DESTINATION_TITLE = "destino_titulo";
    private static final String FIELD_SEATS = "num_lugares";
    private static final String FIELD_DETAILS = "detalhes";

    private static Map<String, Ride> instances = new HashMap<String, Ride>();

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

    public static void storeInstance(String key, Ride value) {
        instances.put(key, value);
    }

    public static Ride getStoredInstance(String key) {
        Ride r = instances.get(key);
        instances.remove(key);

        return r;
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
        query.include(FIELD_DRIVER);
        //query.whereEqualTo(FIELD_DRIVER, u);

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

/***************************************************************************************************
 *
 * Turning into a Parcelable object
 *
 ***************************************************************************************************/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getId());
        dest.writeSerializable(getDatetime());
        dest.writeParcelable(getDestination(), flags);
        dest.writeString(getDetails());
        dest.writeParcelable(getDriver(), flags);
        dest.writeInt(getSeatsAvailable());
        dest.writeParcelable(getStartingPoint(), flags);
    }

    public static final Parcelable.Creator<Ride> CREATOR = new Parcelable.Creator<Ride>() {
        public Ride createFromParcel(Parcel in) {
            Ride c = ParseObject.createWithoutData(Ride.class, in.readString());

            c.setDatetime((Calendar) in.readSerializable());
            c.setDestination((Place) in.readParcelable(Place.class.getClassLoader()));
            c.setDetails(in.readString());
            c.setDriver((User) in.readParcelable(User.class.getClassLoader()));
            c.setSeatsAvailable(in.readInt());
            c.setStartingPoint((Place) in.readParcelable(Place.class.getClassLoader()));

            return c;
        }

        public Ride[] newArray(int size) {
            return new Ride[size];
        }
    };

}
