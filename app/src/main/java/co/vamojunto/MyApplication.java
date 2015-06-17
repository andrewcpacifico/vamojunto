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
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import co.vamojunto.model.Company;
import co.vamojunto.model.Friendship;
import co.vamojunto.model.RequestMessage;
import co.vamojunto.model.RideOffer;
import co.vamojunto.model.RidePassenger;
import co.vamojunto.model.RideRequest;
import co.vamojunto.model.SeatRequest;
import co.vamojunto.model.User;
import co.vamojunto.model.UserCompany;
import co.vamojunto.util.GeneralUtil;
import co.vamojunto.util.Globals;

/**
 * @author Andrew C. Pacifico (andrewcpacifico@gmail.com)
 * @since 0.1.0
 * @version 1.0
 */
public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // get the default preferences for app
        final SharedPreferences settings = getSharedPreferences(
            Globals.DEFAULT_PREF_NAME,
            Context.MODE_PRIVATE
        );

        // defines an editor to preferences
        final SharedPreferences.Editor editor = settings.edit();

        String versionInstalled = settings.getString(Globals.VERSION, "undef");
        if (! versionInstalled.equals("v0.6.0")) {
            GeneralUtil.clearApplicationData(this);
            editor.clear();
            editor.putString(Globals.VERSION, "v0.6.0");
            editor.commit();
        }

        // Enable Crash Reporting
        ParseCrashReporting.enable(this);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(getApplicationContext());

        String appId = getResources().getString(R.string.parse_app_id);
        String clientId = getResources().getString(R.string.parse_client_id);

        ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(RideOffer.class);
        ParseObject.registerSubclass(RideRequest.class);
        ParseObject.registerSubclass(SeatRequest.class);
        ParseObject.registerSubclass(Friendship.class);
        ParseObject.registerSubclass(RidePassenger.class);
        ParseObject.registerSubclass(RequestMessage.class);
        ParseObject.registerSubclass(Company.class);
        ParseObject.registerSubclass(UserCompany.class);

        Parse.initialize(this, appId, clientId);
        ParseFacebookUtils.initialize(this);

        // gets the preference that defines if the current user have already saved his installation
        // on parse database
        final boolean savedInstallation =
                settings.getBoolean(Globals.PARSE_SAVED_INSTALLATION, false);

        if (! savedInstallation) {
            ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.i(TAG, "Installation successfully saved on background");

                        editor.putBoolean(Globals.PARSE_SAVED_INSTALLATION, true);
                        editor.apply();
                    } else {
                        Log.e(TAG, "Error on save installation", e);
                    }
                }
            });
        }

        // gets the preference that defines if the current user have already subscribed to
        // parse push service
        final boolean subscribedToPush =
                settings.getBoolean(Globals.PARSE_PUSH_SUBSCRIBED, false);

        // if the user has not subscribed, subscribe in background and changes this status on
        // application preferences
        if (! subscribedToPush) {
            ParsePush.subscribeInBackground("", new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");

                        editor.putBoolean(Globals.PARSE_PUSH_SUBSCRIBED, true);
                        editor.apply();
                    } else {
                        Log.e("com.parse.push", "failed to subscribe for push", e);
                    }
                }
            });
        }

        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);

       /* new Session(this).closeAndClearTokenInformation();

        ParseUser.logOut();*/

        /*Log.i("Parse", "Instanciando...");
        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();
        Log.i("Parse", "Salvando...");*/
    }
}
