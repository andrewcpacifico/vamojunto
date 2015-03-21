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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.Ride;
import co.vamojunto.model.SeatRequest;
import co.vamojunto.ui.activities.SeatRequestsActivity;
import co.vamojunto.ui.adapters.SeatRequestRecyclerViewAdapter;
import co.vamojunto.util.NetworkUtil;

/**
 * A {@link android.support.v4.app.Fragment} where the user can view all seat requests made to a
 * specific ride offer.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class SeatRequestsFragment extends Fragment {

    private static final String TAG = "SeatRequestsFragment";

    // constants to identify view on mViewFlipper
    private static final int VIEW_PROGRESS = 0;
    private static final int VIEW_ERROR = 1;
    private static final int VIEW_LIST = 2;

    /**
     * The RecyclerView with the list of requests
     */
    private RecyclerView mRecyclerView;

    /**
     * The {@link android.support.v7.widget.RecyclerView.LayoutManager} used by the mRecyclerView
     */
    private LinearLayoutManager mLayoutManager;

    /**
     * The adapter used by the mRecyclerView
     */
    private SeatRequestRecyclerViewAdapter mAdapter;

    /**
     * The ride to get the seat requests to show.
     */
    private Ride mRide;

    /**
     * The {@link android.widget.TextView} that displays a error message, on the error screen View
     */
    private TextView mErrorScreenMessageTextView;

    /**
     * The button displayed on error screen view
     */
    private Button mErrorScreenButton;

    /**
     * {@link android.widget.ViewFlipper} used to switch between ProgressBar, error screen, and the
     * list of items
     */
    private ViewFlipper mViewFlipper;

    /**
     * {@link Handler} used to run code on the main thread
     */
    private Handler mHandler;

    public SeatRequestsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_seat_requests, container, false);

        // retrieves the input ride
        mRide = Ride.getStoredInstance(SeatRequestsActivity.INPUT_RIDE);

        initComponents(rootView);

        return rootView;
    }

    /**
     * Initializes the screen components
     *
     * @param rootView The Fragment's inflated layout.
     */
    private void initComponents(View rootView) {
        // inflates the recycler view
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.seat_requests);

        // instantiates the layout manager, and pass it to the recycler view
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // instantiates the adapter and pass it to the recycler view
        mAdapter = new SeatRequestRecyclerViewAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        // inflates the ViewFlipper
        mViewFlipper = (ViewFlipper) rootView.findViewById(R.id.view_flipper);

        // inflates the error screen messageTextView
        mErrorScreenMessageTextView = (TextView) rootView.findViewById(R.id.error_screen_message_text_view);

        // inflates the error screen button and defines its default action
        mErrorScreenButton = (Button) rootView.findViewById(R.id.error_screen_retry_button);
        mErrorScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSeatRequests();
            }
        });

        loadSeatRequests();
    }

    /**
     * Loads all seat requests made to this ride from the cloud. The requests loaded are passed to
     * the screen's RecyclerView adapter, so the items are displayed on the screen.
     */
    private void loadSeatRequests() {
        mViewFlipper.setDisplayedChild(VIEW_PROGRESS);

        //Loads the seat requests from the cloud, only if the user is connected to the Internet
        if (NetworkUtil.isConnected(getActivity())) {
            SeatRequest.getByRide(mRide).continueWith(new Continuation<List<SeatRequest>, Void>() {
                @Override
                public Void then(Task<List<SeatRequest>> task) throws Exception {
                    // checks if task finished with no problems
                    if (!task.isFaulted() && !task.isCancelled()) {
                        // switches the mViewFlipper to RecyclerView
                        mViewFlipper.setDisplayedChild(VIEW_LIST);

                        // gets the list of items returned by task, and use it as mAdapter dataset
                        List<SeatRequest> lst = task.getResult();
                        mAdapter.setDataset(lst);
                    } else if (task.isFaulted()) {
                        Log.e(TAG, task.getError().getMessage());

                        // checks if mHandler have been already instantiated
                        if (mHandler == null)
                            mHandler = new Handler();

                        // displays the error message to user
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mErrorScreenMessageTextView.setText(getString(R.string.errormsg_default));
                                mViewFlipper.setDisplayedChild(VIEW_ERROR);
                            }
                        });
                    } else {
                        Log.e(TAG, "loadSeatRequests: task cancelled");
                    }

                    return null;
                }
            });
        }else {
            mErrorScreenMessageTextView.setText(getString(R.string.errormsg_no_internet_connection));
            mViewFlipper.setDisplayedChild(VIEW_ERROR);
        }
    }
}
