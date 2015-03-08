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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import co.vamojunto.R;
import co.vamojunto.model.Place;
import co.vamojunto.model.Ride;
import co.vamojunto.model.RideRequest;
import co.vamojunto.model.User;
import co.vamojunto.ui.activities.NewRideActivity;
import co.vamojunto.ui.activities.NewRideRequestActivity;
import co.vamojunto.ui.adapters.ListRideRequestsRecyclerViewAdapter;
import co.vamojunto.util.Globals;

/**
 *  A {@link android.support.v4.app.Fragment} to list all the rides that a user have requested.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class ListMyRequestsFragment extends Fragment {


    private static final String TAG = "ListMyRequestsFragment";

    // the constants below are used to identify the views loaded by the ViewFlipper
    private static final int VIEW_PROGRESS = 0;
    private static final int VIEW_ERROR = 1;
    private static final int VIEW_DEFAULT = 2;

    /**
     * RecyclerView where the rides are displayed
     */
    private RecyclerView mRidesRecyclerView;

    /**
     * LayoutManager used by the mRidesRecyclerView
     */
    private LinearLayoutManager mRequestsLayoutManager;

    /**
     * Adapter used to manage the data of mRidesRecyclerView
     */
    private ListRideRequestsRecyclerViewAdapter mRequestsAdapter;

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
     * Required default constructor
     */
    public ListMyRequestsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list_my_requests, container, false);

        initComponents(rootView);

        loadMyRequests();

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == NewRideRequestActivity.REQ_CODE) {
                final RideRequest r = data.getParcelableExtra(NewRideRequestActivity.RES_RIDE);

                // was necessary to use a delay to add the item to the screen, so that the RecyclerView
                // could show the animation, and positioning at the new item
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addItem(r);
                    }
                }, 1000);

                Toast.makeText(getActivity(), getString(R.string.carona_cadastrada), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Initializates the screen components
     *
     * @param rootView The Fragment's inflated layout.
     */
    public void initComponents(View rootView) {
        mRidesRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_requests_recycler_view);

        mRidesRecyclerView.setHasFixedSize(true);

        mRequestsLayoutManager = new LinearLayoutManager(rootView.getContext());
        mRidesRecyclerView.setLayoutManager(mRequestsLayoutManager);

        mRequestsAdapter = new ListRideRequestsRecyclerViewAdapter(getActivity(), new ArrayList<RideRequest>());
        mRidesRecyclerView.setAdapter(mRequestsAdapter);

        Button requestButton = (Button) rootView.findViewById(R.id.ok_button);
        requestButton.setText(getText(R.string.request_ride));
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NewRideRequestActivity.class);
                getParentFragment().startActivityForResult(intent, NewRideRequestActivity.REQ_CODE);
            }
        });

        mViewFlipper = (ViewFlipper) rootView.findViewById(R.id.flipper);

        // looks for the error screen views
        mErrorScreenMsgTextView = (TextView) rootView.findViewById(R.id.error_screen_message_text_view);
        mErrorScreenRetryButton = (Button) rootView.findViewById(R.id.error_screen_retry_button);
        mErrorScreenRetryButton.setOnClickListener(new View.OnClickListener() {
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
        mViewFlipper.setDisplayedChild(VIEW_DEFAULT);

        Place p = new Place(2, 2);
        p.setTitulo("UFAM");

        List<RideRequest> lst = new ArrayList<RideRequest>();
        lst.add(new RideRequest(User.getCurrentUser(), Calendar.getInstance(), "", p, p));
        lst.add(new RideRequest(User.getCurrentUser(), Calendar.getInstance(), "", p, p));
        lst.add(new RideRequest(User.getCurrentUser(), Calendar.getInstance(), "", p, p));
        lst.add(new RideRequest(User.getCurrentUser(), Calendar.getInstance(), "", p, p));
        lst.add(new RideRequest(User.getCurrentUser(), Calendar.getInstance(), "", p, p));


        mRequestsAdapter.setDataset(lst);

        // Loads the rides from the cloud, only if the user is connected to the Internet
//        if (NetworkUtil.isConnected(getActivity())) {
//            Ride.getByDriverAsync((User) User.getCurrentUser()).continueWith(new Continuation<List<Ride>, Void>() {
//                @Override
//                public Void then(Task<List<Ride>> task) throws Exception {
//                    mViewFlipper.setDisplayedChild(VIEW_DEFAULT);
//
//                    if (!task.isFaulted() && !task.isCancelled()) {
//                        List<Ride> lstRides = task.getResult();
//                        Collections.sort(lstRides, new Comparator<Ride>() {
//                            @Override
//                            public int compare(Ride lhs, Ride rhs) {
//                                return rhs.getCreatedAt().compareTo(lhs.getCreatedAt());
//                            }
//                        });
//
//                        mRequestsAdapter.setDataset(lstRides);
//                    } else {
//                        Log.e(TAG, task.getError().getMessage());
//
//                        displayErrorScreen();
//                    }
//
//                    return null;
//                }
//            });
//        } else {
//            displayErrorScreen(getString(R.string.erro_msg_no_internet_connection));
//        }
    }

    /**
     * Switches the viewFlipper to display the error screen. and customizes the error message.
     *
     * @param errorMsg The message displayed on the screen, if the param value is null, the default
     *                 error message is used.
     */
    private void displayErrorScreen(String errorMsg) {
        if (errorMsg == null)
            mErrorScreenMsgTextView.setText(getString(R.string.default_error_screen_message));
        else
            mErrorScreenMsgTextView.setText(errorMsg);

        mViewFlipper.setDisplayedChild(VIEW_ERROR);
    }

    /**
     * Switches the viewFlipper to display the error screen using the default error screen message.
     */
    private void displayErrorScreen() {
        mErrorScreenMsgTextView.setText(getString(R.string.default_error_screen_message));

        mViewFlipper.setDisplayedChild(VIEW_ERROR);
    }

}
