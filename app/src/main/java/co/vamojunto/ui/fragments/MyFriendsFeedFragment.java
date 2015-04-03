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

package co.vamojunto.ui.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import co.vamojunto.R;
import co.vamojunto.model.User;
import co.vamojunto.ui.widget.SlidingTabLayout;

/**
 * Fragment to display the feed from user friends. All data posted by the user friends have to be
 * displayed, the rides offered and requested.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 1.0.0
 */
public class MyFriendsFeedFragment extends Fragment {

    private static final String TAG = "MyFriendsFeedFragment";

    /**
     * ProgressDialog displayed when the data is being loaded
     */
    private ProgressDialog mProDialog;

    public MyFriendsFeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_friends_feed, container, false);

        // assigning ViewPager View and setting the adapter
        ViewPager pager = (ViewPager) rootView.findViewById(R.id.pager);
        pager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));

        // assigning the Sliding Tab Layout View
        SlidingTabLayout tabs = (SlidingTabLayout) rootView.findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.ColorPrimaryDark);
            }
        });

        // setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        // changes the action bar title
        ((ActionBarActivity)getActivity()).getSupportActionBar()
                .setTitle(getString(R.string.my_friends_feed));

        setHasOptionsMenu(true);

        return rootView;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_my_friends_feed, menu);
    }

    /**
     * Adapter to fill the pages on this Fragment tabs.  Two tabs are displayed, one where the
     * user can view the rides offered by his friends, and another where the user can view the
     * rides requested by his friends.
     *
     * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
     * @since 0.1.0
     * @version 1.0.0
     */
    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return getString(R.string.ride_offers);
            else if (position == 1)
                return getString(R.string.ride_requests);

            return "";
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            if (position == 0) {
                Bundle argsBundle = new Bundle();
                argsBundle.putInt(ListRidesFragment.ARG_TYPE, ListRidesFragment.TYPE_FRIEND);

                Fragment f = new ListRidesFragment();
                f.setArguments(argsBundle);

                return f;
            }
            else if (position == 1)
                return new ListMyRequestsFragment();

            else return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

}
