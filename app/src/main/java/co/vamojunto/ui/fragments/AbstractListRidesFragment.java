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
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.Ride;
import co.vamojunto.ui.activities.RideDetailsActivity;
import co.vamojunto.ui.adapters.ListMyRidesRecyclerViewAdapter;
import co.vamojunto.util.NetworkUtil;

/**
 * An abstract fragment to display a list of rides.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 1.0.0
 */
public abstract class AbstractListRidesFragment extends Fragment {

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
     * RecyclerView where the rides are displayed
     */
    private RecyclerView mRidesRecyclerView;

    /**
     * LayoutManager used by the mRidesRecyclerView
     */
    private LinearLayoutManager mRidesLayoutManager;

    /**
     * Adapter used to manage the data of mRidesRecyclerView
     */
    private ListMyRidesRecyclerViewAdapter mRidesAdapter;

    /**
     * ViewFlipper used to alternate between the ProgressBar, that is displayed when the rides
     * are loaded, the error screen displayed when any error occurs, and the main screen with
     * the rides list.
     */
    private ViewFlipper mViewFlipper;

    /**
     * The {@link android.widget.TextView} that displays a error message, on the error screen View
     */
    private TextView mErrorScreenMsgTextView;

    /**
     * The {@link android.widget.Button} used to retry an action that failed on error screen.
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

    public AbstractListRidesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutResource(), container, false);

        mHandler = new Handler();

        initComponents(rootView);

        loadRides();

        return rootView;
    }

    /**
     * Initializates the screen components
     *
     * @param rootView The Fragment's inflated layout.
     */
    protected void initComponents(View rootView) {
        // inits the RecyclerView
        mRidesRecyclerView = (RecyclerView) rootView.findViewById(R.id.rides_recycler_view);
        mRidesRecyclerView.setHasFixedSize(true);

        // inits the RecyclerView LayoutManager
        mRidesLayoutManager = new LinearLayoutManager(rootView.getContext());
        mRidesRecyclerView.setLayoutManager(mRidesLayoutManager);

        // inits the RecyclerView Adapter
        mRidesAdapter = new ListMyRidesRecyclerViewAdapter(getActivity(),
                new ArrayList<Ride>(), new ListMyRidesRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                Ride choosenRide = mRidesAdapter.getItem(position);
                Intent intent = new Intent(AbstractListRidesFragment.this.getActivity(),
                        RideDetailsActivity.class);

                Ride.storeInstance(RideDetailsActivity.EXTRA_RIDE, choosenRide);
                startActivity(intent);
            }
        });
        mRidesRecyclerView.setAdapter(mRidesAdapter);

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

    /**
     * Getter for the fragment layout
     *
     * @return The layout resource.
     * @since 0.1.0
     */
    protected abstract int getLayoutResource();

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
    protected abstract Task<List<Ride>> getRidesAsync();

    /**
     * TODO this method documentation
     */
    protected void loadRides() {
        mViewFlipper.setDisplayedChild(PROGRESS_VIEW);

        // check for user's network connection if this fragment is not used on an offline feed
        if (! isOfflineFeed() && ! NetworkUtil.isConnected(getActivity())) {
            displayErrorScreen(getString(R.string.errormsg_no_internet_connection));
        } else {
            Task<List<Ride>> loadRidesTask = getRidesAsync();

            // check if the getRideAsync was correctly implemented and returns a valid Task
            if (loadRidesTask != null) {
                Log.i(TAG, "Loading rides...");

                loadRidesTask.continueWith(new Continuation<List<Ride>, Void>() {
                    @Override
                    public Void then(final Task<List<Ride>> task) throws Exception {
                        // force the code to run on the main thread
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mViewFlipper.setDisplayedChild(DEFAULT_VIEW);

                                if (!task.isFaulted() && !task.isCancelled()) {
                                    List<Ride> lstRides = task.getResult();
                                    Collections.sort(lstRides, new Comparator<Ride>() {
                                        @Override
                                        public int compare(Ride lhs, Ride rhs) {
                                            return rhs.getCreatedAt().compareTo(lhs.getCreatedAt());
                                        }
                                    });

                                    mRidesAdapter.setDataset(lstRides);

                                    // if there is no ride, displays a specific message to the user
                                    if (lstRides.size() == 0) {
                                        displayNoRideMessage();
                                    }
                                } else {
                                    Log.e(TAG, task.getError().getMessage());

                                    displayErrorScreen();
                                }
                            }
                        });

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
     * Switches the viewFlipper to display the error screen. and customizes the error message.
     *
     * @param errorMsg The message displayed on the screen, if the param value is null, the default
     *                 error message is used.
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
     */
    protected void displayErrorScreen() {
        mErrorScreenMsgTextView.setText(getString(R.string.errormsg_default));

        mViewFlipper.setDisplayedChild(ERROR_VIEW);
    }

}