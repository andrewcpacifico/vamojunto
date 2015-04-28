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
 * A model class for a message sent to a RideRequest.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 1.0.0
 */
@ParseClassName("RequestMessage")
public class RequestMessage extends ParseObject {

    /**
     * Field that stores the message itself
     *
     * @since 0.1.0
     */
    public static String FIELD_MESSAGE = "message";

    /**
     * Field that stores the user that sent the message.
     *
     * @since 0.1.0
     */
    public static String FIELD_SENDER = "sender";

    /**
     * Field that stores the ride request that the message was sent to.
     *
     * @since 0.1.0
     */
    public static String FIELD_REQUEST = "request";

    public RequestMessage() { /* required empty constructor. DO NOT CHANGE THIS */ }

    /**
     * A constructor to initialize fields.
     *
     * @param message The message to set.
     * @param sender The sender of the message.
     * @param request The request to set.
     * @since 0.1.0
     */
    public RequestMessage(String message, User sender, RideRequest request) {
        setMessage(message);
        setSender(sender);
        setRequest(request);
    }

    /**
     * Get the message itself.
     *
     * @return The message sent.
     */
    public String getMessage() {
        return getString(FIELD_MESSAGE);
    }

    /**
     * Set the message field.
     *
     * @param message The message to set.
     */
    public void setMessage(String message) {
        put(FIELD_MESSAGE, message);
    }

    /**
     * Get the sender of the message
     *
     * @return The user who sent the message.
     */
    public User getSender() {
        return (User) getParseUser(FIELD_SENDER);
    }

    /**
     * Set the sender of the message.
     *
     * @param sender The sender of the message.
     */
    public void setSender(User sender) {
        put(FIELD_SENDER, sender);
    }

    /**
     * Get the request that this message was sent for.
     *
     * @return The request.
     */
    public RideRequest getRequest() {
        return (RideRequest) get(FIELD_REQUEST);
    }

    /**
     * Set the request field.
     *
     * @param request The request to set.
     */
    public void setRequest(RideRequest request) {
        put(FIELD_REQUEST, request);
    }

}
