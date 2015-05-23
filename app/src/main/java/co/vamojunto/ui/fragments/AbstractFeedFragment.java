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
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.Ride;
import co.vamojunto.model.User;
import co.vamojunto.model.UserCompany;
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

    public static final int SCREEN_DEFAULT = 0;
    public static final int SCREEN_NOT_AUTHORIZED = 1;

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

    /**
     * Code for error children view on the viewFlipper.
     *
     * @since 0.3.0
     */
    private static final int VIEW_ERROR = 2;

    /**
     * The container where the feed fragment was inflated in. The container is stored as a field
     * because if the user is not authorized to access the feed, another layout have to be inflated
     * to container.
     *
     * @since 0.3.0
     */
    private ViewGroup mContainer;

    /**
     * A Handler to run code on the main thread.
     *
     * @since 0.3.0
     */
    private Handler mHandler;

    /**
     * A ViewFlipper to switch between default screen, the progressBar, and the error screen.
     *
     * @since 0.3.0
     */
    private ViewFlipper mFlipper;

    /**
     * The TextView for the message on the error screen.
     *
     * @since 0.3.0
     */
    private TextView mErrorScreenMsgTextView;

    /**
     * The Button on the error screen. The button is inflated so we can hide the button.
     *
     * @since 0.3.0
     */
    private Button mErrorScreenRetryButton;

    public AbstractFeedFragment() {
        // Required empty public constructor
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_default_feed, container, false);

        mContainer = container;
        mHandler = new Handler();

        // check if the user is authorized to access the feed
        isAuthorized().continueWith(new Continuation<UserCompany.Status, Void>() {
            @Override
            public Void then(final Task<UserCompany.Status> task) throws Exception {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        UserCompany.Status approvationStatus = task.getResult();

                        if (approvationStatus == UserCompany.Status.APPROVED) {
                            displayDefaultScreen(rootView);
                        } else if (approvationStatus == UserCompany.Status.REJECTED) {
                            changeContent(SCREEN_NOT_AUTHORIZED);
                        } else {
                            displayErrorScreen(getString(R.string.ufam_feed_waiting_msg), false);
                        }
                    }
                });

                return null;
            }
        });

        initComponents(rootView);

        return rootView;
    }

    protected void initComponents(View rootView) {
        // inflate the viewFlipper of the screen
        mFlipper = (ViewFlipper) rootView.findViewById(R.id.flipper);

        // inflate components of error screen
        mErrorScreenMsgTextView =
                (TextView) rootView.findViewById(R.id.error_screen_message_text_view);
        mErrorScreenRetryButton = (Button) rootView.findViewById(R.id.error_screen_retry_button);
    }

    /**
     * If the user was not authorized to access the feed, a new layout have to be inflated to
     * fragment container, this method is responsible to do that.
     *
     * @since 0.3.0
     */
    public void changeContent(final int content) {
        int layoutRes = (content == SCREEN_DEFAULT)
                ? R.layout.fragment_default_feed
                : getNotAuthorizedLayoutRes();

        mContainer.removeAllViews();

        View rootView = LayoutInflater.from(getActivity())
                .inflate(layoutRes, mContainer, true);

        if (content == SCREEN_NOT_AUTHORIZED) {
            initNotAuthorizedComponents(rootView);
        } else {
            initComponents(rootView);
        }
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
                .setTitle(getTitle());

        setHasOptionsMenu(true);

        mFlipper.setDisplayedChild(VIEW_DEFAULT);
    }

    protected Handler getHandler() {
        return mHandler;
    }

    /**
     * Display the error screen with default configurations.
     *
     * @since 0.3.0
     */
    protected void displayErrorScreen() {
        displayErrorScreen(getString(R.string.errormsg_default));
    }

    /**
     * Display the error screen with a custom message.
     *
     * @param msg The message to display.
     * @since 0.3.0
     */
    protected void displayErrorScreen(String msg) {
        displayErrorScreen(msg, true);
    }

    /**
     * Display the error screen with a custom message, and with the option to hide the retry button.
     *
     * @param msg The message to display.
     * @param hasButton Define if the retry button have to be displayed.
     * @since 0.3.0
     */
    protected void displayErrorScreen(String msg, boolean hasButton) {
        if (hasButton) {
            mErrorScreenRetryButton.setVisibility(View.VISIBLE);
        } else {
            mErrorScreenRetryButton.setVisibility(View.GONE);
        }

        mErrorScreenMsgTextView.setText(msg);
        mFlipper.setDisplayedChild(VIEW_ERROR);
    }

    /**
     * Define the authorization status for user accessing the feed. The {@link UserCompany.Status}
     * are used to define the user authorization.
     *
     * @return A Task containing the result for the user status searching.
     * @since 0.3.0
     */
    protected abstract Task<UserCompany.Status> isAuthorized();

    /**
     * Inflate the components on the screen displayed to user if h eis not authorized to access
     * the feed. Usually this screen will have a registration form, and on this method the components
     * of this form are inflated.
     *
     * @param rootView The inflated layout for the not authorized screen.
     * @since 0.3.0
     */
    protected abstract void initNotAuthorizedComponents(View rootView);

    /**
     * Return the layout resource of the screen displayed to user if he is not authorized to access
     * the feed.
     *
     * @return The layout resource.
     * @since 0.3.0
     */
    protected abstract @LayoutRes int getNotAuthorizedLayoutRes();

    /**
     * Return a String to use as the title of the screen.
     *
     * @return The title of the screen.
     * @since 0.3.0
     */
    protected abstract String getTitle();

    /**
     * Return the fragment that displays the list of ride offers on this feed.
     *
     * @return The ride offers listing fragment.
     * @since 0.3.0
     */
    protected abstract AbstractListRideOffersFragment getListOffersFragment();

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
