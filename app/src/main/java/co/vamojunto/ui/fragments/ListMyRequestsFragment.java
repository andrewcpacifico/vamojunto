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


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.RideRequest;
import co.vamojunto.model.User;
import co.vamojunto.ui.activities.NewRideRequestActivity;
import co.vamojunto.ui.activities.RequestDetailsActivity;
import co.vamojunto.ui.adapters.RequestsRecyclerViewAdapter;
import co.vamojunto.util.NetworkUtil;

/**
 *  A {@link android.support.v4.app.Fragment} to list all the rides that a user have requested.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.1.1
 * @since 0.1.0
 */
public class ListMyRequestsFragment extends Fragment {


    private static final String TAG = "ListMyRequestsFragment";

    // the constants below are used to identify the views loaded by the ViewFlipper
    private static final int VIEW_PROGRESS = 0;
    private static final int VIEW_ERROR = 1;
    private static final int VIEW_DEFAULT = 2;

    /**
     * LayoutManager used by the mRidesRecyclerView
     */
    private LinearLayoutManager mRequestsLayoutManager;

    /**
     * Adapter used to manage the data of mRidesRecyclerView
     */
    private RequestsRecyclerViewAdapter mRequestsAdapter;

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
     * A handler to run code on main thread.
     *
     * @since 0.4.0
     */
    private Handler mHandler;

    /**
     * Required default constructor
     */
    public ListMyRequestsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list_my_requests, container, false);

        mHandler = new Handler();

        initComponents(rootView);

        loadMyRequests();

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == NewRideRequestActivity.REQ_CODE) {
                final RideRequest r = RideRequest.getStoredInstance(NewRideRequestActivity.RES_RIDE);

                // was necessary to use a delay to add the item to the screen, so that the RecyclerView
                // could show the animation, and positioning at the new item
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addItem(r);
                    }
                }, 500);

                Toast.makeText(getActivity(), getString(R.string.ride_requested), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Initializates the screen components
     *
     * @param rootView The Fragment's inflated layout.
     * @since 0.1.0
     */
    public void initComponents(View rootView) {
        RecyclerView ridesRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_requests_recycler_view);
        ridesRecyclerView.setHasFixedSize(true);

        mRequestsLayoutManager = new LinearLayoutManager(rootView.getContext());
        ridesRecyclerView.setLayoutManager(mRequestsLayoutManager);

        mRequestsAdapter = new RequestsRecyclerViewAdapter(
            getActivity(),
            new ArrayList<RideRequest>(),
            new RequestsRecyclerViewAdapter.OnItemClickListener() {
                @Override
                public void OnItemClick(int position) {
                    RideRequest.storeInstance(
                            RequestDetailsActivity.EXTRA_REQUEST,
                            mRequestsAdapter.getItem(position)
                    );

                    Intent intent = new Intent(getActivity(), RequestDetailsActivity.class);
                    startActivity(intent);
                }
            }
        );
        ridesRecyclerView.setAdapter(mRequestsAdapter);

        mViewFlipper = (ViewFlipper) rootView.findViewById(R.id.flipper);

        // looks for the error screen views
        mErrorScreenMsgTextView = (TextView) rootView.findViewById(R.id.error_screen_message_text_view);

        Button errorScreenRetryButton = (Button) rootView.findViewById(R.id.error_screen_retry_button);
        errorScreenRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMyRequests();
            }
        });
    }

    /**
     * Adds a ride record to the screen, this method is called after a new ride registration,so its
     * not necessary reload all the data from the cloud
     *
     * @param c RideRequest to be added to the UI.
     */
    private void addItem(RideRequest c) {
        mRequestsAdapter.addItem(c);

        // after the item addition, scrolls the recyclerview to the first position, so that the user
        // can see the inserted record
        if (mRequestsLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
            mRequestsLayoutManager.scrollToPosition(0);
        }
    }

    /**
     * Loads the user's ride requests. At the end of the method, the user's ride requests list is defined
     * as the dataset from mRideRequestsRecyclerView
     */
    public void loadMyRequests() {
        mViewFlipper.setDisplayedChild(VIEW_PROGRESS);

        // loads the ride requests from the cloud, only if the user is connected to the Internet
        if (NetworkUtil.isConnected(getActivity())) {
            RideRequest.getByRequesterAsync(User.getCurrentUser()).continueWith(new Continuation<List<RideRequest>, Void>() {
                @Override
                public Void then(final Task<List<RideRequest>> task) throws Exception {
                    // check if Fragment is attached to activity
                    if (getActivity() != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!task.isFaulted() && !task.isCancelled()) {
                                    mViewFlipper.setDisplayedChild(VIEW_DEFAULT);

                                    List<RideRequest> requestList = task.getResult();

                                    // sort the list by creation date
                                    Collections.sort(requestList, new Comparator<RideRequest>() {
                                        @Override
                                        public int compare(RideRequest lhs, RideRequest rhs) {
                                            return rhs.getCreatedAt().compareTo(lhs.getCreatedAt());
                                        }
                                    });

                                    mRequestsAdapter.setDataset(requestList);
                                } else {
                                    Log.e(TAG, "Error on load user requests", task.getError());

                                    displayErrorScreen();
                                }

                            }
                        });
                    }

                    return null;
                }
            });
        } else {
            displayErrorScreen(getString(R.string.errormsg_no_internet_connection));
        }
    }

    /**
     * Switches the viewFlipper to display the error screen. and customizes the error message.
     *
     * @param errorMsg The message displayed on the screen, if the param value is null, the default
     *                 error message is used.
     */
    private void displayErrorScreen(String errorMsg) {
        if (errorMsg == null)
            mErrorScreenMsgTextView.setText(getString(R.string.errormsg_default));
        else
            mErrorScreenMsgTextView.setText(errorMsg);

        mViewFlipper.setDisplayedChild(VIEW_ERROR);
    }

    /**
     * Switches the viewFlipper to display the error screen using the default error screen message.
     */
    private void displayErrorScreen() {
        mErrorScreenMsgTextView.setText(getString(R.string.errormsg_default));

        mViewFlipper.setDisplayedChild(VIEW_ERROR);
    }

}
