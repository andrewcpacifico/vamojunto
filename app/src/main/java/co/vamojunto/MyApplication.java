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

package co.vamojunto;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

import co.vamojunto.model.Ride;
import co.vamojunto.model.RideRequest;
import co.vamojunto.model.SeatRequest;
import co.vamojunto.model.User;

/**
 * @author Andrew C. Pacifico (andrewcpacifico@gmail.com)
 * @since 0.1.0
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        //Parse.enableLocalDatastore(this);

        String appId = getResources().getString(R.string.parse_app_id);
        String clientId = getResources().getString(R.string.parse_client_id);

        ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(Ride.class);
        ParseObject.registerSubclass(RideRequest.class);
        ParseObject.registerSubclass(SeatRequest.class);
        Parse.initialize(this, appId, clientId);

       /* new Session(this).closeAndClearTokenInformation();

        ParseUser.logOut();*/

        /*Log.i("Parse", "Instanciando...");
        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();
        Log.i("Parse", "Salvando...");*/
    }
}
