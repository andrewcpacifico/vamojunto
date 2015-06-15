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

package co.vamojunto.util;

/**
 * Created by andrew on 24/02/15.
 */
public class Globals {

    public static final String PACKAGE = "co.vamojunto";
    public static final String DEFAULT_PREF_NAME = PACKAGE;

    // preference keys
    public static final String FETCHED_FRIENDS_PREF_KEY = "fetched_friends";
    public static final String LAT_PREF_KEY = "lat";
    public static final String LNG_PREF_KEY = "lng";
    public static final String PARSE_PUSH_SUBSCRIBED = "push_subscribed";
    public static final String PARSE_SAVED_INSTALLATION = "parse_saved_install";
    public static final String VERSION = "version";
    public static final String ZOOM_PREF_KEY = "zoom";
    // end of preference keys

    public static final float DEFAULT_ZOOM_LEVEL = 17f;
    public static final double MANAUS_LAT = -3.065635;
    public static final double MANAUS_LNG = -59.995240;

    // activities request codes
    public static final int NEW_RIDE_ACTIVITY_REQUEST_CODE = 1;
    public static final int NEW_RIDE_REQ_ACTIVITY_REQUEST_CODE = 2;


}
