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
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.List;

import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.User;
import co.vamojunto.ui.adapters.NavigationDrawerAdapter;
import co.vamojunto.ui.fragments.FriendsFeedFragment;
import co.vamojunto.ui.fragments.MainFragment;
import co.vamojunto.ui.fragments.MinhasCaronasFragment;
import co.vamojunto.ui.fragments.UFAMFeedFragment;
import co.vamojunto.util.Globals;

/**
 * System's Main Activity
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 2.1
 */
public class MainActivity extends VamoJuntoActivity {

    private static final String TAG = "MainActivity";

    /**
     * Code for user rides screen
     *
     * @since 0.1.0
     */
    public static final int VIEW_MY_RIDES = 0;

    /**
     * Code for user friends feed screen
     *
     * @since 0.1.0
     */
    public static final int VIEW_FRIENDS_FEED = 1;

    /**
     * Code for user friends feed screen
     *
     * @since 0.3.0
     */
    public static final int VIEW_UFAM_FEED = 2;

    public static final String EXTRA_INITIAL_VIEW = TAG + ".InitialView";

    /**
     * NavigationDrawer instance
     */
    private DrawerLayout mDrawerLayout;

    private FloatingActionMenu mFloatingMenu;

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreated(Bundle savedInstanceState) {
        super.onCreated(savedInstanceState);

        // checks if an initial view was sent to activity
        if (getIntent().hasExtra(EXTRA_INITIAL_VIEW)) {
            int viewCode = getIntent().getIntExtra(EXTRA_INITIAL_VIEW, -1);
            displayView(viewCode);
        } else if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new FriendsFeedFragment())
                    .commit();
        }

        // setups the application's NavigationDrawer
        initDrawer();

        // remove elevation from app bar because of view pager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAppBar.setElevation(0);
        }

        mFloatingMenu = (FloatingActionMenu) findViewById(R.id.floating_action_menu);
        mFloatingMenu.setClosedOnTouchOutside(true);

        FloatingActionButton fabOfferRide = (FloatingActionButton) findViewById(R.id.fab_offer_ride);
        fabOfferRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFloatingMenu.close(true);

                Intent intent = new Intent(MainActivity.this, NewRideActivity.class);
                startActivityForResult(intent, Globals.NEW_RIDE_ACTIVITY_REQUEST_CODE);
            }
        });

        FloatingActionButton fabRequestRide = (FloatingActionButton) findViewById(R.id.fab_request_ride);
        fabRequestRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFloatingMenu.close(true);

                Intent intent = new Intent(MainActivity.this, NewRideRequestActivity.class);
                startActivityForResult(intent, Globals.NEW_RIDE_REQ_ACTIVITY_REQUEST_CODE);
            }
        });

        FloatingActionButton fabSendMessage = (FloatingActionButton) findViewById(R.id.fab_send_message);
        fabSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFloatingMenu.close(true);

                Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mFloatingMenu.isOpened()) {
            mFloatingMenu.close(true);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Setups the NavigationDrawer
     *
     * @since 0.1.0
     */
    private void initDrawer() {
        // calculates the navigation drawer width
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int screenWidth = size.x;
        int actionBarSize = getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material);
        int drawerWidth = Math.min(screenWidth - actionBarSize, 6 * actionBarSize);
        View drawer = findViewById(R.id.drawer);
        ViewGroup.LayoutParams params = drawer.getLayoutParams();
        params.width = drawerWidth;
        drawer.setLayoutParams(params);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.nav_drawer_recycler_view);
        recyclerView.setHasFixedSize(true);

        RecyclerView.Adapter drawerAdapter = new NavigationDrawerAdapter(
            this,
            new NavigationDrawerAdapter.OnItemClickListener() {
                @Override
                public void OnItemClick(View v, int position) {
                    navigationDrawerItemClicked(position);
                }
            }
        );

        recyclerView.setAdapter(drawerAdapter);

        RecyclerView.LayoutManager drawerLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(drawerLayoutManager);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer_layout);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mAppBar, R.string.openDrawer, R.string.closeDrawer) {
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
        // block click events on header for now
        if (position > 0) {
            displayView(position - 1);

            // always closes the navigation drawer after the click, adds a delay to wait for animation
            // before closing the drawer
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.closeDrawers();
                }
            });
        }
    }

    /**
     * Switches the view displayed on the main screen.
     *
     * @param code The code of the view to display.
     * @since 0.1.0
     */
    protected void displayView(int code) {
        switch (code) {
            // the first item on navigation drawer displays the user's rides management screen
            case VIEW_MY_RIDES:
                // loads the MinhaCaronasFragment to the main screen
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new MinhasCaronasFragment()).commit();
                break;

            // the second item on navigation drawer displays the user's friends feed screen
            case VIEW_FRIENDS_FEED:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, FriendsFeedFragment.newInstance()).commit();
                break;

            case VIEW_UFAM_FEED:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, UFAMFeedFragment.newInstance()).commit();
                break;

            default:
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new MainFragment()).commit();
                break;
        }
    }

}
