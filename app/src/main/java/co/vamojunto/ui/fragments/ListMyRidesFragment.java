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
import co.vamojunto.model.RideOffer;
import co.vamojunto.ui.activities.NewRideActivity;
import co.vamojunto.R;
import co.vamojunto.ui.activities.RideDetailsActivity;
import co.vamojunto.ui.adapters.RidesRecyclerViewAdapter;
import co.vamojunto.model.User;
import co.vamojunto.util.Globals;
import co.vamojunto.util.NetworkUtil;

/**
 * {@link android.support.v4.app.Fragment} to list all the rides that a user is participating, as
 * a driver or passenger.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.1
 * @since 0.1.0
 */
public class ListMyRidesFragment extends Fragment {

    private static final String TAG = "ListMyRidesFragment";

    // the constants below are used to identify the views loaded by the ViewFlipper
    private static final int VIEW_PROGRESS = 0;
    private static final int VIEW_ERRO = 1;
    private static final int VIEW_PADRAO = 2;

    /**
     * LayoutManager used by the mRidesRecyclerView
     */
    private LinearLayoutManager mRidesLayoutManager;

    /**
     * Adapter used to manage the data of mRidesRecyclerView
     */
    private RidesRecyclerViewAdapter mRidesAdapter;

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
    public ListMyRidesFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list_my_rides, container, false);

        mHandler = new Handler();

        initComponents(rootView);

        loadMyRides();

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Globals.NEW_RIDE_ACTIVITY_REQUEST_CODE) {
                final RideOffer c = RideOffer.getStoredInstance(NewRideActivity.RES_RIDE);

                // was necessary to use a delay to add the item to the screen, so that the RecyclerView
                // could show the animation, and positioning at the new item
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addItem(c);
                    }
                }, 500);

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
        // inits the RecyclerView
        RecyclerView ridesRecyclerView = (RecyclerView) rootView.findViewById(R.id.rides_recycler_view);
        ridesRecyclerView.setHasFixedSize(true);

        // inits the RecyclerView LayoutManager
        mRidesLayoutManager = new LinearLayoutManager(rootView.getContext());
        ridesRecyclerView.setLayoutManager(mRidesLayoutManager);

        // inits the RecyclerView Adapter
        mRidesAdapter = new RidesRecyclerViewAdapter(getActivity(),
                new ArrayList<RideOffer>(), new RidesRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                RideOffer choosenRideOffer = mRidesAdapter.getItem(position);
                Intent intent = new Intent(ListMyRidesFragment.this.getActivity(),
                        RideDetailsActivity.class);

                RideOffer.storeInstance(RideDetailsActivity.EXTRA_RIDE, choosenRideOffer);
                startActivity(intent);
            }
        });
        ridesRecyclerView.setAdapter(mRidesAdapter);

        mViewFlipper = (ViewFlipper) rootView.findViewById(R.id.flipper);

        // looks for the error screen views
        mErrorScreenMsgTextView = (TextView) rootView.findViewById(R.id.error_screen_message_text_view);

        Button errorScreenRetryButton = (Button) rootView.findViewById(R.id.error_screen_retry_button);
        errorScreenRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMyRides();
            }
        });
    }

    /**
     * Adds a ride record to the screen, this method is called after a new ride registration,so its
     * not necessary reload all the data from the cloud
     *
     * @param c Ride to be added to the UI.
     */
    private void addItem(RideOffer c) {
        mRidesAdapter.addItem(c);

        // after the item addition, scrolls the recyclerview to the first position, so that the user
        // can see the inserted record
        if (mRidesLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
            mRidesLayoutManager.scrollToPosition(0);
        }
    }

    /**
     * Loads the user's ride offers. At the end of the method, the user's rides list is defined
     * as the dataset from mRideRecyclerView
     *
     * @since 0.1.0
     */
    public void loadMyRides() {
        mViewFlipper.setDisplayedChild(VIEW_PROGRESS);

        // Loads the rides from the cloud, only if the user is connected to the Internet
        if (NetworkUtil.isConnected(getActivity())) {
            RideOffer.getByDriverAsync(User.getCurrentUser()).continueWith(new Continuation<List<RideOffer>, Void>() {
                @Override
                public Void then(final Task<List<RideOffer>> task) throws Exception {
                    if (getActivity() != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mViewFlipper.setDisplayedChild(VIEW_PADRAO);

                                if (!task.isFaulted() && !task.isCancelled()) {
                                    List<RideOffer> lstRideOffers = task.getResult();
                                    Collections.sort(lstRideOffers, new Comparator<RideOffer>() {
                                        @Override
                                        public int compare(RideOffer lhs, RideOffer rhs) {
                                            return rhs.getCreatedAt().compareTo(lhs.getCreatedAt());
                                        }
                                    });

                                    mRidesAdapter.setDataset(lstRideOffers);
                                } else {
                                    Log.e(TAG, "Error on load user offers", task.getError());

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

        mViewFlipper.setDisplayedChild(VIEW_ERRO);
    }

    /**
     * Switches the viewFlipper to display the error screen using the default error screen message.
     */
    private void displayErrorScreen() {
        mErrorScreenMsgTextView.setText(getString(R.string.errormsg_default));

        mViewFlipper.setDisplayedChild(VIEW_ERRO);
    }
}
