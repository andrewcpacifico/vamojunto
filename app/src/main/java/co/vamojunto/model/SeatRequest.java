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

import bolts.Task;

/**
 * Model class for a seat request
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
@ParseClassName("SeatRequest")
public class SeatRequest extends ParseObject implements Parcelable {

    private static final String FIELD_USER_ID = "user_id";
    private static final String FIELD_RIDE_ID = "ride_id";
    private static final String FIELD_STATUS = "status";

    public static final int STATUS_WAITING = 0;
    public static final int STATUS_CONFIRMED = 1;
    public static final int STATUS_REJECTED = 2;

    public SeatRequest() {
        // required default constructor
    }

    public SeatRequest(User u, Ride r) {
        this.setUser(u);
        this.setRide(r);
        this.setStatus(STATUS_WAITING);
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
        this.setStatus(STATUS_CONFIRMED);

        return saveInBackground();
    }

    /**
     * Rejects the seat request on the ride
     */
    public void reject() {
        this.setStatus(STATUS_REJECTED);
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
        dest.writeParcelable(getUser(), flags);
        dest.writeParcelable(getRide(), flags);
        dest.writeInt(getStatus());

    }

    public static final Parcelable.Creator<SeatRequest> CREATOR = new Parcelable.Creator<SeatRequest>() {
        public SeatRequest createFromParcel(Parcel in) {
            SeatRequest sr = ParseObject.createWithoutData(SeatRequest.class, in.readString());

            sr.setUser((User) in.readParcelable(User.class.getClassLoader()));
            sr.setRide((Ride) in.readParcelable(Ride.class.getClassLoader()));
            sr.setStatus(in.readInt());

            return sr;
        }

        public SeatRequest[] newArray(int size) {
            return new SeatRequest[size];
        }
    };

}