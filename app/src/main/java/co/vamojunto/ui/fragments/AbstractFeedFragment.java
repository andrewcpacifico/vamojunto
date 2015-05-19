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

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.Ride;
import co.vamojunto.ui.widget.SlidingTabLayout;

/**
 * A simple {@link Fragment} subclass.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.3.0
 * @version 1.0.0
 */
public abstract class AbstractFeedFragment extends Fragment {

    private static final String TAG = "AbstractFeedFragment";

    /**
     * Code for loading children view on the viewFlipper
     *
     * @since 0.3.0
     */
    private static final int VIEW_LOADING = 0;

    /**
     * Code for default children view on the viewFlipper
     *
     * @since 0.3.0
     */
    private static final int VIEW_DEFAULT = 1;

    private ViewGroup mContainer;

    private Handler mHandler;

    private ViewFlipper mFlipper;

    public AbstractFeedFragment() {
        // Required empty public constructor
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_default_feed, container, false);

        mContainer = container;
        mHandler = new Handler();

        // inflate the viewFlipper of the screen
        mFlipper = (ViewFlipper) rootView.findViewById(R.id.flipper);

        // check if the user is authorized to access the feed
        isAuthorized().continueWith(new Continuation<Boolean, Void>() {
            @Override
            public Void then(final Task<Boolean> task) throws Exception {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (task.getResult()) {
                            displayDefaultScreen(rootView);
                        } else {
                            changeContent();
                        }
                    }
                });

                return null;
            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.d(TAG, "onDestroyView");
    }

    public void changeContent() {
        View rootView = LayoutInflater.from(getActivity())
                .inflate(getNotAuthorizedLayoutRes(), mContainer, true);
        initNotAuthorizedComponents(rootView);

        Log.d(TAG, "Trocou");
    }

    /**
     * Change the viewFlipper to display the default screen, and initialize the screen components.
     *
     * @param rootView The inflated layout.
     * @since 0.3.0
     */
    private void displayDefaultScreen(View rootView) {

        // assigning ViewPager View and setting the adapter
        ViewPager pager = (ViewPager) rootView.findViewById(R.id.pager);
        pager.setAdapter(new MyPagerAdapter(getChildFragmentManager(), this));

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
        ((ActionBarActivity) getActivity()).getSupportActionBar()
                .setTitle(getString(R.string.my_friends_feed));

        setHasOptionsMenu(true);

        mFlipper.setDisplayedChild(VIEW_DEFAULT);
    }

    protected abstract bolts.Task<Boolean> isAuthorized();

    protected abstract void initNotAuthorizedComponents(View rootView);

    protected abstract int getNotAuthorizedLayoutRes();

    protected abstract AbstractListRidesFragment<Ride> getListOffersFragment();

//    public abstract ListUFAMOffersFragment getListRequestsFragment();

    /**
     * Adapter to fill the pages on this Fragment tabs.  Two tabs are displayed, one where the
     * user can view the rides offered by his friends, and another where the user can view the
     * rides requested by his friends.
     *
     * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
     * @since 0.3.0
     * @version 1.0.0
     */
    private static class MyPagerAdapter extends FragmentPagerAdapter {

        private AbstractFeedFragment mContainerFragment;

        public MyPagerAdapter(FragmentManager fm, AbstractFeedFragment containerFragment) {
            super(fm);

            mContainerFragment = containerFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return mContainerFragment.getString(R.string.ride_offers);
            else if (position == 1)
                return mContainerFragment.getString(R.string.ride_requests);

            Log.e(AbstractFeedFragment.TAG, "Invalid page position");
            return "";
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return mContainerFragment.getListOffersFragment();
            }
//            else if (position == 1)
//                return mContainerFragment.getListRequestsFragment();

            else return null;
        }

        @Override
        public int getCount() {
            return 1;
        }
    }

}
