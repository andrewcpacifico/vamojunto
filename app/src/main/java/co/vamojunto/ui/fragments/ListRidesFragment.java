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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.Ride;
import co.vamojunto.model.User;
import co.vamojunto.ui.activities.NewRideActivity;
import co.vamojunto.ui.activities.RideDetailsActivity;
import co.vamojunto.ui.adapters.ListMyRidesRecyclerViewAdapter;
import co.vamojunto.util.Globals;
import co.vamojunto.util.NetworkUtil;

/**
 * A {@link Fragment} to display a list of ride offers. The fragment allows the definition of a
 * type of ride listing. As this fragment will be used on all screens that wants to display a
 * list of rides, this is the current method to define which list it have to display.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 1.0.0
 */
public class ListRidesFragment extends Fragment {

    private static final String TAG = "ListRidesFragment";

    // the constants below are used to identify the views loaded by the ViewFlipper
    private static final int PROGRESS_VIEW = 0;
    private static final int ERROR_VIEW = 1;
    private static final int DEFAULT_VIEW = 2;

    // constants used to define which type of listing the fragment have to display
    public static final int TYPE_FRIEND = 0;
    public static final int TYPE_UFAM = 1;

    /**
     * Constant used to get the listing type argument, sent to fragment as a bundle.
     */
    public static final String ARG_TYPE = TAG + "argType";

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
     * The type of listing to dysplay, currently there are two types of listing:
     *   <ul>
     *     <li>The ride offers from the user friends.</li>
     *       <li>The ride offers from the ufam students.</li>
     *   </ul>
     */
    private int mListType;

    /**
     * Required default constructor
     */
    public ListRidesFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list_rides, container, false);

        mListType = getArguments().getInt(ARG_TYPE, -1);

        initComponents(rootView);
        loadRides();

        return rootView;
    }

    /**
     * Initializates the screen components
     *
     * @param rootView The Fragment's inflated layout.
     */
    public void initComponents(View rootView) {
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
                Intent intent = new Intent(ListRidesFragment.this.getActivity(),
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
    }

    /**
     * TODO this method documentation
     */
    public void loadRides() {
        mViewFlipper.setDisplayedChild(PROGRESS_VIEW);

        // Loads the rides from the cloud, only if the user is connected to the Internet
        if (NetworkUtil.isConnected(getActivity())) {
            Task<List<Ride>> loadRidesTask = null;

            if (mListType == TYPE_FRIEND) {
                loadRidesTask = Ride.getFriendsOffersAsync((User) User.getCurrentUser());
            }

            if (loadRidesTask != null) {
                loadRidesTask.continueWith(new Continuation<List<Ride>, Void>() {
                    @Override
                    public Void then(Task<List<Ride>> task) throws Exception {
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
                        } else {
                            Log.e(TAG, task.getError().getMessage());

                            displayErrorScreen();
                        }

                        return null;
                    }
                });
            }
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

        mViewFlipper.setDisplayedChild(ERROR_VIEW);
    }

    /**
     * Switches the viewFlipper to display the error screen using the default error screen message.
     */
    private void displayErrorScreen() {
        mErrorScreenMsgTextView.setText(getString(R.string.errormsg_default));

        mViewFlipper.setDisplayedChild(ERROR_VIEW);
    }

}

