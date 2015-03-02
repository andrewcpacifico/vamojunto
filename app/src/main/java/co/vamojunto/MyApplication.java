/*
 * Copyright (c) 2015. Vamo Junto Ltda. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Vamo Junto Ltda,
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you entered into with Vamo Junto Ltda.
 *
 * See LICENSE.txt
 */

package co.vamojunto;

import android.app.Application;
import android.util.Log;

import com.facebook.Session;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

import co.vamojunto.model.Carona;
import co.vamojunto.model.Usuario;

/**
 * @author Andrew C. Pacifico (andrewcpacifico@gmail.com)
 * @since 0.1.0
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        String appId = getResources().getString(R.string.parse_app_id);
        String clientId = getResources().getString(R.string.parse_client_id);

        ParseObject.registerSubclass(Usuario.class);
        ParseObject.registerSubclass(Carona.class);
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
