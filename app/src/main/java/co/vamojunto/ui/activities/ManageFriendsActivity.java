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
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import co.vamojunto.R;
import co.vamojunto.ui.fragments.ManageFollowedFragment;
import co.vamojunto.ui.fragments.ManageFriendsFragment;

public class ManageFriendsActivity extends ActionBarActivity {

    public static final String TAG = "ManageFriendsActivity";

    /**
     * The Activity toolbar
     */
    private Toolbar mToolbarActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_friends);

        ManageFriendsFragment fragment = new ManageFriendsFragment();
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
            intent.putExtra(MainActivity.EXTRA_INITIAL_VIEW, MainActivity.VIEW_FRIENDS_FEED);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
            finish();
        } else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handles action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if (isTaskRoot()) {
                // code to navigate up to MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(MainActivity.EXTRA_INITIAL_VIEW, MainActivity.VIEW_FRIENDS_FEED);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
                finish();
            } else {
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

}
