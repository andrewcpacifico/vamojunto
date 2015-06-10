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

package co.vamojunto.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.List;

import co.vamojunto.R;
import co.vamojunto.model.User;

/**
 * Created by Andrew C. Pacifico <andrewcpacifico@gmail.com> on 07/06/15.
 */
public abstract class VamoJuntoActivity extends AppCompatActivity {

    private static String TAG = "VamoJuntoActivity";

    protected Toolbar mAppBar;

    protected Handler mHandler;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    final protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        // checks if the user is authenticated, if not, displays the login screen.
        if (User.getCurrentUser() == null) {
            Log.i(TAG, "User not authenticated. Displaying the login screen...");

            // starts the login screen to the user
            Intent intent = new Intent(this, LoginActivity.class);
            this.startActivity(intent);
            this.finish();

            // if user is authenticated, setups the main screen
        } else {
            setContentView(getContentView());

            // setups the application bar
            mAppBar = (Toolbar) findViewById(R.id.tool_bar);
            //setSupportActionBar(mAppBar);

            onCreated(savedInstanceState);
        }

    }

    protected abstract @LayoutRes int getContentView();

    protected void onCreated(Bundle savedInstanceState) { }

    public Toolbar getAppBar() {
        return mAppBar;
    }
}
