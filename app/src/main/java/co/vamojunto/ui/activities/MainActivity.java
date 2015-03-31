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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.facebook.Session;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import co.vamojunto.R;
import co.vamojunto.model.User;
import co.vamojunto.ui.adapters.NavigationDrawerAdapter;
import co.vamojunto.ui.fragments.MainFragment;
import co.vamojunto.ui.fragments.MinhasCaronasFragment;

/**
 * System's Main Activity
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 1.0.0
 */
public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";

    public static final int VIEW_MY_RIDES = 0;

    public static final String EXTRA_INITIAL_VIEW = TAG + ".InitialView";

    /**
     * Application bar
     */
    private Toolbar mToolbar;

    /**
     * RecyclerView containing the items on application's navigation drawer
     */
    private RecyclerView mRecyclerView;

    /**
     * Adapter used by mRecyclerView
     */
    private RecyclerView.Adapter mAdapter;

    /**
     * LayoutManager used by mRecyclerView
     */
    private RecyclerView.LayoutManager mLayoutManager;

    /**
     * NavigationDrawer instance
     */
    private DrawerLayout mDrawerLayout;

    /**
     *
     */
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * Authenticated user
     */
    private User mCurrentUser;


/*************************************************************************************************
 *
 * Activity's events
 *
 *************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // checks if the user is authenticated, if not, displays the login screen.
        mCurrentUser = User.getCurrentUser();
        if (mCurrentUser == null) {
            Log.i(TAG, "User not authenticated. Displaying the login screen...");

            // starts the login screen to the user
            Intent intent = new Intent(this, SplashActivity.class);
            this.startActivity(intent);
            this.finish();

        // if user is authenticated, setups the main screen
        } else {
            // Associate the device with a user
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            installation.put("user",ParseUser.getCurrentUser());
            installation.saveInBackground();

            setContentView(R.layout.activity_main);

            // checks if an initial view was sent to activity
            if (getIntent().hasExtra(EXTRA_INITIAL_VIEW)) {
                int viewCode = getIntent().getIntExtra(EXTRA_INITIAL_VIEW, -1);
                displayView(viewCode);
            } else if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new MainFragment())
                        .commit();
            }

            // setups the application bar
            mToolbar = (Toolbar) findViewById(R.id.tool_bar);
            setSupportActionBar(mToolbar);

            // setups the application's NavigationDrawer
            initDrawer();
        }
    }

/***************************************************************************************************
 *
 * Other methods
 *
 **************************************************************************************************/

    /**
     * Setups the NavigationDrawer
     */
    private void initDrawer() {
        mRecyclerView = (RecyclerView) findViewById(R.id.nav_drawer_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        Bitmap imgUsuario = mCurrentUser.getProfileImage();

        mAdapter = new NavigationDrawerAdapter(this, mCurrentUser.getName(),
                mCurrentUser.getEmail(), imgUsuario, new NavigationDrawerAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, int position) {
                navigationDrawerItemClicked(position);
            }
        });

        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                // added to fix the bug where the NavigationDrawer was placed under the screen,
                // if the screen has a map. This error happened on Android 4.0.4
                mDrawerLayout.bringChildToFront(drawerView);
                mDrawerLayout.requestLayout();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    /**
     * Handles the clicks on NavigationDrawer items
     *
     * @param position Position of the item clicked, used to define what action have to be executed.
     */
    private void navigationDrawerItemClicked(int position) {
        displayView(position - 1);

        // always closes the navigation drawer after the click
        mDrawerLayout.closeDrawers();
    }

    protected void displayView(int code) {
        switch (code) {
            // Primeiro item do menu.
            case VIEW_MY_RIDES:
                // Carrega o Fragment MinhasCaronas para a tela principal.
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new MinhasCaronasFragment()).commit();
                break;

            case 3:
                new Session(this).closeAndClearTokenInformation();
                ParseUser.logOut();

                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;

            default:
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new MainFragment()).commit();
                break;
        }
    }
}
