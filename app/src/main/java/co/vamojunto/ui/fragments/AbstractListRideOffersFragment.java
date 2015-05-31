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


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.RideOffer;
import co.vamojunto.ui.activities.RideDetailsActivity;
import co.vamojunto.ui.adapters.RidesRecyclerViewAdapter;
import co.vamojunto.util.NetworkUtil;
import co.vamojunto.util.TextUtil;
import co.vamojunto.util.UIUtil;

/**
 * An abstract fragment to display a list of rides.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 1.0.2
 */
public abstract class AbstractListRideOffersFragment extends FilterableFeedFragment {

    private static final String TAG = "co.vamojunto";

    /**
     * Code of the view that displays a progress bar on the viewflipper.
     *
     * @since 0.1.0
     */
    private static final int PROGRESS_VIEW = 0;

    /**
     * Code of the view that displays an error screen on the viewflipper.
     *
     * @since 0.1.0
     */
    private static final int ERROR_VIEW = 1;

    /**
     * Code of the view that displays the default view on the viewflipper.
     *
     * @since 0.1.0
     */
    private static final int DEFAULT_VIEW = 2;

    /**
     * Adapter used to manage the data of mRidesRecyclerView
     *
     * @since 0.1.0
     */
    protected RidesRecyclerViewAdapter mRidesAdapter;

    /**
     * ViewFlipper used to alternate between the ProgressBar, that is displayed when the rides
     * are loaded, the error screen displayed when any error occurs, and the main screen with
     * the rides list.
     *
     * @since 0.1.0
     */
    private ViewFlipper mViewFlipper;

    /**
     * The {@link android.widget.TextView} that displays a error message, on the error screen View
     *
     * @since 0.1.0
     */
    private TextView mErrorScreenMsgTextView;

    /**
     * The {@link android.widget.Button} used to retry an action that failed on error screen.
     *
     * @since 0.1.0
     */
    private Button mErrorScreenRetryButton;

    /**
     * Icon displayed on error screen.
     *
     * @since 0.1.0
     */
    private ImageView mErrorScreenIcon;

    /**
     * A Handler to run code on the main thread.
     *
     * @since 0.1.0
     */
    protected Handler mHandler;

    public AbstractListRideOffersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutResource(), container, false);

        mHandler = new Handler();
        setHasOptionsMenu(true);

        initComponents(rootView);

        loadRides();

        return rootView;
    }

    /**
     * Initializates the screen components
     *
     * @param rootView The Fragment's inflated layout.
     * @since 0.1.0
     */
    protected void initComponents(View rootView) {
        RecyclerView ridesRecyclerView = (RecyclerView) rootView.findViewById(R.id.rides_recycler_view);
        ridesRecyclerView.setHasFixedSize(true);

        // inits the RecyclerView LayoutManager
        LinearLayoutManager ridesLayoutManager = new LinearLayoutManager(rootView.getContext());
        ridesRecyclerView.setLayoutManager(ridesLayoutManager);

        // inits the RecyclerView Adapter
        mRidesAdapter = new RidesRecyclerViewAdapter(getActivity(),
                new ArrayList<RideOffer>(), new RidesRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                RideOffer choosenRideOffer = mRidesAdapter.getItem(position);
                Intent intent = new Intent(AbstractListRideOffersFragment.this.getActivity(),
                        RideDetailsActivity.class);

                RideOffer.storeInstance(RideDetailsActivity.EXTRA_RIDE, choosenRideOffer);
                startActivity(intent);
            }
        });
        ridesRecyclerView.setAdapter(mRidesAdapter);

        mViewFlipper = (ViewFlipper) rootView.findViewById(R.id.flipper);

        // looks for the error screen views
        mErrorScreenMsgTextView = (TextView) rootView.findViewById(R.id.error_screen_message_text_view);
        mErrorScreenRetryButton = (Button) rootView.findViewById(R.id.error_screen_retry_button);
        mErrorScreenRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRides();
            }
        });
        mErrorScreenIcon = (ImageView) rootView.findViewById(R.id.error_screen_message_icon);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_list_rides, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.search_menu) {
            FilterFeedFragment filterFragment = new FilterFeedFragment();
            filterFragment.show(AbstractListRideOffersFragment.this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFeedFilter(Bundle filterValues) {
        // check user's network connection before filter feed
        if (! NetworkUtil.isConnected(getActivity())) {
            Toast.makeText(
                    getActivity(),
                    getString(R.string.errormsg_no_internet_connection),
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        Map<String, String> filterMap = new HashMap<>();

        // display a progress bar
        UIUtil.startLoading(getActivity(), getString(R.string.filtering));

        // if the user entered a value for starting point filtering, add it to the filter map
        String startingPoint = TextUtil.normalize(filterValues.getString(FILTER_STARTING_POINT));
        if (! startingPoint.equals("")) {
            filterMap.put(RideOffer.FIELD_LC_STARTING_POINT_TITLE, startingPoint);
        }

        // if the user entered a value for destination filtering, add it to the filter map
        String destination = TextUtil.normalize(filterValues.getString(FILTER_DESTINATION));
        if (! destination.equals("")) {
            filterMap.put(RideOffer.FIELD_LC_DESTINATION_TITLE, destination);
        }

        this.filter(filterMap).continueWith(new Continuation<List<RideOffer>, Void>() {
            @Override
            public Void then(final Task<List<RideOffer>> task) throws Exception {
                if (! task.isCancelled() && !task.isFaulted()) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            UIUtil.stopLoading();

                            List<RideOffer> lst = task.getResult();
                            mRidesAdapter.setDataset(lst);

                            if (lst.size() == 0) {
                                displayErrorScreen(
                                        getString(R.string.errormsg_no_results_found),
                                        false,
                                        R.drawable.ic_sad
                                );
                            } else {
                                mViewFlipper.setDisplayedChild(DEFAULT_VIEW);
                            }
                        }
                    });
                } else {
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.errormsg_default),
                            Toast.LENGTH_LONG
                    ).show();

                    if (task.isFaulted()) {
                        Log.e(TAG, "Error on filter feed results", task.getError());
                    } else {
                        Log.e(TAG, "Task cancelled. This shouldn't be happening");
                    }
                }

                return null;
            }
        });
    }

    /**
     * Filter the feed items.
     *
     * @return A {@link java.util.List} with the filtered items to display on feed.
     * @since 0.1.0
     */
    protected abstract Task<List<RideOffer>> filter(Map<String, String> filterValues);

    /**
     * Getter for the fragment layout
     *
     * @return The layout resource.
     * @since 0.1.0
     */
    protected @LayoutRes int getLayoutResource() {
        return R.layout.fragment_list_rides;
    }

    /**
     * Defines if this list is an offline feed or not. This value is used to know if it is necessary
     * to check the user connection, before to call the getRides task.
     *
     * @return <code>true</code> if this list will be displayed on an offline feed,
     *         or <code>false</code> if it is not.
     * @since 0.1.0
     */
    protected abstract boolean isOfflineFeed();

    /**
     * Get a list of rides that will be displayed on the feed.
     *
     * @return A {@link bolts.Task} containing the list as result.
     * @since 0.1.0
     */
    protected abstract Task<List<RideOffer>> getRidesAsync();

    /**
     * TODO this method documentation
     */
    protected void loadRides() {
        mViewFlipper.setDisplayedChild(PROGRESS_VIEW);

        // check for user's network connection if this fragment is not used on an offline feed
        if (! isOfflineFeed() && ! NetworkUtil.isConnected(getActivity())) {
            displayErrorScreen(getString(R.string.errormsg_no_internet_connection));
        } else {
            Task<List<RideOffer>> loadRidesTask = getRidesAsync();

            // check if the getRideAsync was correctly implemented and returns a valid Task
            if (loadRidesTask != null) {
                Log.i(TAG, "Loading rides...");

                loadRidesTask.continueWith(new Continuation<List<RideOffer>, Void>() {
                    @Override
                    public Void then(final Task<List<RideOffer>> task) throws Exception {
                        // check if Fragment is attached to some Activity, before change the view
                        if (getActivity() != null) {
                            // force the code to run on the main thread
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mViewFlipper.setDisplayedChild(DEFAULT_VIEW);

                                    if (!task.isFaulted() && !task.isCancelled()) {
                                        List<RideOffer> lstRideOffers = task.getResult();

                                        mRidesAdapter.setDataset(lstRideOffers);

                                        // if there is no ride, displays a specific message to the user
                                        if (lstRideOffers.size() == 0) {
                                            displayNoRideMessage();
                                        }
                                    } else {
                                        Log.e(TAG, "Error on load ride offers", task.getError());

                                        displayErrorScreen();
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, "Fragment detached from Activity");
                        }

                        return null;
                    }
                });
            }
        }
    }

    /**
     * Displays a message to user, when there is no ride to display on feed.
     *
     * @since 0.1.0
     */
    protected void displayNoRideMessage() {
        displayErrorScreen(getString(R.string.no_ride_to_display));
        mErrorScreenRetryButton.setVisibility(View.GONE);
        mErrorScreenIcon.setImageResource(R.drawable.ic_sad);
    }

    /**
     * Display the error screen, with a full personalization.
     *
     * @param errorMsg The message to display.
     * @param hasButton Defines if the reload button has to be visible.
     * @param iconResource The resource of the error icon.
     *
     * @since 0.2.0
     */
    protected void displayErrorScreen(String errorMsg, boolean hasButton, int iconResource) {
        displayErrorScreen(errorMsg);
        if (hasButton) {
            mErrorScreenRetryButton.setVisibility(View.VISIBLE);
        } else {
            mErrorScreenRetryButton.setVisibility(View.GONE);
        }
        mErrorScreenIcon.setImageResource(iconResource);
    }

    /**
     * Switches the viewFlipper to display the error screen. and customizes the error message.
     *
     * @param errorMsg The message displayed on the screen, if the param value is null, the default
     *                 error message is used.
     * @since 0.1.0
     */
    protected void displayErrorScreen(String errorMsg) {
        if (errorMsg == null)
            mErrorScreenMsgTextView.setText(getString(R.string.errormsg_default));
        else
            mErrorScreenMsgTextView.setText(errorMsg);

        mViewFlipper.setDisplayedChild(ERROR_VIEW);
    }

    /**
     * Switches the viewFlipper to display the error screen using the default error screen message.
     *
     * @since 0.1.0
     */
    protected void displayErrorScreen() {
        displayErrorScreen(getString(R.string.errormsg_default));
    }

}