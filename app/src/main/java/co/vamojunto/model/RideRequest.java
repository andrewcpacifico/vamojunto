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

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Calendar;

/**
 * The System's RideRequest Model
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
@ParseClassName("RideRequest")
public class RideRequest extends ParseObject implements Parcelable {

    private static final String TAG ="model.RideRequest";

    private static final String FIELD_REQUESTOR = "requestor";
    private static final String FIELD_DATETIME = "datetime";
    private static final String FIELD_STARTING_POINT_LAT = "starting_point_lat";
    private static final String FIELD_STARTING_POINT_LNG = "starting_point_lng";
    private static final String FIELD_STARTING_POINT_TITLE = "starting_point_title";
    private static final String FIELD_DESTINATION_LAT = "destination_lat";
    private static final String FIELD_DESTINATION_LNG = "destination_lng";
    private static final String FIELD_DESTINATION_TITLE = "destination_title";
    private static final String FIELD_DETAILS = "details";

    /**
     * Required default constructor
     */
    public RideRequest() {}

    public RideRequest(User requestor, Calendar datetime, String details, Place startingPoint, Place destination) {
        setRequestor(requestor);
        setDatetime(datetime);
        setDetails(details);
        setStartingPoint(startingPoint);
        setDestination(destination);
    }

    public String getId() {
        return getObjectId();
    }

    public User getRequestor() {
        return (User) get(FIELD_REQUESTOR);
    }

    public void setRequestor(User u) {
        this.put(FIELD_REQUESTOR, u);
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
        dest.writeParcelable(getRequestor(), flags);
        dest.writeSerializable(getDatetime());
        dest.writeParcelable(getDestination(), flags);
        dest.writeString(getDetails());
        dest.writeParcelable(getStartingPoint(), flags);
    }

    public static final Parcelable.Creator<RideRequest> CREATOR = new Parcelable.Creator<RideRequest>() {
        public RideRequest createFromParcel(Parcel in) {
            RideRequest rideRequest = ParseObject.createWithoutData(RideRequest.class, in.readString());

            rideRequest.setRequestor((User) in.readParcelable(User.class.getClassLoader()));
            rideRequest.setDatetime((Calendar) in.readSerializable());
            rideRequest.setDestination((Place) in.readParcelable(Place.class.getClassLoader()));
            rideRequest.setDetails(in.readString());
            rideRequest.setStartingPoint((Place) in.readParcelable(Place.class.getClassLoader()));

            return rideRequest;
        }

        public RideRequest[] newArray(int size) {
            return new RideRequest[size];
        }
    };

}
