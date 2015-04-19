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

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * A model class for the RidePassenger relational table.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 1.0.0
 */
@ParseClassName("RidePassenger")
public class RidePassenger extends ParseObject {

    /**
     * The name of the "passenger" field on the cloud table.
     *
     * @since 0.1.0
     */
    public static final String FIELD_PASSENGER = "passenger";

    /**
     * The name of the "ride" field on the cloud table.
     *
     * @since 0.1.0
     */
    public static final String FIELD_RIDE = "ride";

    public RidePassenger() { /* required default constructor. DO NOT DELETE OR EDIT THIS */ }

    /**
     * Get the passenger field.
     *
     * @return A {@link User} instance of the passenger field.
     * @since 0.1.0
     */
    public User getPassenger() {
        return (User) get(FIELD_PASSENGER);
    }

    /**
     * Set the passenger field.
     *
     * @since 0.1.0
     */
    public void setPassenger(User user) {
        put(FIELD_PASSENGER, user);
    }

    /**
     * Get the ride field.
     *
     * @return A {@link co.vamojunto.model.Ride} instance of the ride field.
     * @since 0.1.0
     */
    public Ride getRide() {
        return (Ride) get(FIELD_RIDE);
    }

    /**
     * Set the ride field.
     *
     * @since 0.1.0
     */
    public void setRide(Ride ride) {
        put(FIELD_RIDE, ride);
    }

}
