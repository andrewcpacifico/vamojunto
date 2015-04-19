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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import co.vamojunto.R;
import co.vamojunto.ui.fragments.RideDetailsFragment;

/**
 * Activity where the user can view the details of a specific ride.
 *
 * @author Andrew C. Pacifico <andrewpcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class RideDetailsActivity extends ActionBarActivity {

    public static final String TAG = "RideDetailsActivity";

    // constants used to define the input extras names
    public static final String EXTRA_RIDE = TAG + ".ride";

    // constants used to define the input extras names
    public static final String EXTRA_RIDE_ID = TAG + ".rideId";

    /**
     * The Activity toolbar
     */
    private Toolbar mToolbarActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_details);

        RideDetailsFragment fragment = new RideDetailsFragment();
        fragment.setArguments(getIntent().getExtras());
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }

        setupAppBar();
    }

    /**
     * Initializes the activity's App Bar
     */
    private void setupAppBar() {
        mToolbarActionBar = (Toolbar) findViewById(R.id.tool_bar);

        setSupportActionBar(mToolbarActionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Overriding the onBackPressed method, to make an "up" navigation when this activity is
     * opened from a notification.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (isTaskRoot()) {
            // code to navigate up to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_INITIAL_VIEW, MainActivity.VIEW_MY_RIDES);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
            finish();
        } else {
            finish();
        }
    }
}
