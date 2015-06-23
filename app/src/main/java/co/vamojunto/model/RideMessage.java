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
 * A model class for a message sent to a RideOffer.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.8.0
 * @version 1.0
 */
@ParseClassName("RideMessage")
public class RideMessage extends ParseObject {

    /**
     * Field that stores the message itself
     *
     * @since 0.8.0
     */
    public static String FIELD_MESSAGE = "message";

    /**
     * Field that stores the user that sent the message.
     *
     * @since 0.8.0
     */
    public static String FIELD_SENDER = "sender";

    /**
     * Field that stores the ride offer that the message was sent to.
     *
     * @since 0.8.0
     */
    public static String FIELD_OFFER = "ride_offer";

    public RideMessage() { /* required empty constructor. DO NOT CHANGE THIS */ }

    /**
     * A constructor to initialize fields.
     *
     * @param message The message to set.
     * @param sender The sender of the message.
     * @param offer The request to set.
     * @since 0.8.0
     */
    public RideMessage(String message, User sender, RideOffer offer) {
        setMessage(message);
        setSender(sender);
        setRideOffer(offer);
    }

    /**
     * Get the message itself.
     *
     * @return The message sent.
     * @since 0.8.0
     */
    public String getMessage() {
        return getString(FIELD_MESSAGE);
    }

    /**
     * Set the message field.
     *
     * @param message The message to set.
     * @since 0.8.0
     */
    public void setMessage(String message) {
        put(FIELD_MESSAGE, message);
    }

    /**
     * Get the sender of the message
     *
     * @return The user who sent the message.
     * @since 0.8.0
     */
    public User getSender() {
        return (User) getParseUser(FIELD_SENDER);
    }

    /**
     * Set the sender of the message.
     *
     * @param sender The sender of the message.
     * @since 0.8.0
     */
    public void setSender(User sender) {
        put(FIELD_SENDER, sender);
    }

    /**
     * Get the ride offer that this message was sent for.
     *
     * @return The ride offer.
     * @since 0.8.0
     */
    public RideOffer getRideOffer() {
        return (RideOffer) get(FIELD_OFFER);
    }

    /**
     * Set the ride offer field.
     *
     * @param offer The ride offer to set.
     * @since 0.8.0
     */
    public void setRideOffer(RideOffer offer) {
        put(FIELD_OFFER, offer);
    }

}
