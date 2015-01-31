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

import com.parse.Parse;
import com.parse.ParseObject;

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

        Parse.initialize(this, "pry1CBPCYtfTqonbY3sarxqQ4xWO5OQTfMw1GRDS", "5QcfmQFUPRU3gOttwoANgiDqYzULfnouneliLryP");

        /*Log.i("Parse", "Instanciando...");
        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();
        Log.i("Parse", "Salvando...");*/
    }
}
