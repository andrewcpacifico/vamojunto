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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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
import co.vamojunto.ui.activities.MainActivity;
import co.vamojunto.ui.activities.NewRideActivity;
import co.vamojunto.R;
import co.vamojunto.ui.activities.RideDetailsActivity;
import co.vamojunto.ui.adapters.ListRidesRecyclerViewAdapter;
import co.vamojunto.model.Ride;
import co.vamojunto.model.User;
import co.vamojunto.util.Globals;
import co.vamojunto.util.NetworkUtil;

/**
 * {@link android.support.v4.app.Fragment} to list all the rides that a user is participating, as
 * a driver or passenger.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class ListMyRidesFragment extends Fragment {

    private static final String TAG = "ListMyRidesFragment";

    // the constants below are used to identify the views loaded by the ViewFlipper
    private static final int VIEW_PROGRESS = 0;
    private static final int VIEW_ERRO = 1;
    private static final int VIEW_PADRAO = 2;

    /**
     * RecyclerView where the rides are displayed
     */
    private RecyclerView mRidesRecyclerView;

    /**
     * LayoutManager used by the mRidesRecyclerView
     */
    private LinearLayoutManager mOfertasLayoutManager;

    /**
     * Adapter used to manage the data of mRidesRecyclerView
     */
    private ListRidesRecyclerViewAdapter mRidesAdapter;

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
    public ListMyRidesFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_lista_caronas, container, false);

        initComponents(rootView);

        loadMyRides();

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Globals.NEW_RIDE_ACTIVITY_REQUEST_CODE) {
                final Ride c = Ride.getStoredInstance(NewRideActivity.RES_RIDE);

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
        mRidesRecyclerView = (RecyclerView) rootView.findViewById(R.id.lista_caronas_recycler_view);
        mRidesRecyclerView.setHasFixedSize(true);

        // inits the RecyclerView LayoutManager
        mOfertasLayoutManager = new LinearLayoutManager(rootView.getContext());
        mRidesRecyclerView.setLayoutManager(mOfertasLayoutManager);

        // inits the RecyclerView Adapter
        mRidesAdapter = new ListRidesRecyclerViewAdapter(getActivity(),
                new ArrayList<Ride>(), new ListRidesRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                Ride choosenRide = mRidesAdapter.getItem(position);
                Intent intent = new Intent(ListMyRidesFragment.this.getActivity(),
                        RideDetailsActivity.class);
                Ride.storeInstance(RideDetailsActivity.EXTRA_RIDE, choosenRide);
                startActivity(intent);

//                NotificationCompat.Builder mBuilder =
//                        new NotificationCompat.Builder(getActivity())
//                                .setSmallIcon(R.drawable.ic_launcher)
//                                .setContentTitle("My notification")
//                                .setContentText("Hello World!");
//                // Creates an explicit intent for an Activity in your app
//                Intent resultIntent = new Intent(getActivity(), RideDetailsActivity.class);
//
//                // The stack builder object will contain an artificial back stack for the
//                // started Activity.
//                // This ensures that navigating backward from the Activity leads out of
//                // your application to the Home screen.
//                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getActivity());
//                // Adds the back stack for the Intent (but not the Intent itself)
//                stackBuilder.addParentStack(MainActivity.class);
//                // Adds the Intent that starts the Activity to the top of the stack
//                stackBuilder.addNextIntent(resultIntent);
//                PendingIntent resultPendingIntent =
//                        stackBuilder.getPendingIntent(
//                                0,
//                                PendingIntent.FLAG_UPDATE_CURRENT
//                        );
//                mBuilder.setContentIntent(resultPendingIntent);
//                NotificationManager mNotificationManager =
//                        (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
//                // mId allows you to update the notification later on.
//                mNotificationManager.notify(1, mBuilder.build());
            }
        });
        mRidesRecyclerView.setAdapter(mRidesAdapter);

        Button okButton = (Button) rootView.findViewById(R.id.ok_button);
        okButton.setText(getText(R.string.oferecer_carona));
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NewRideActivity.class);
                getParentFragment().startActivityForResult(intent, Globals.NEW_RIDE_ACTIVITY_REQUEST_CODE);
            }
        });

        mViewFlipper = (ViewFlipper) rootView.findViewById(R.id.flipper);

        // looks for the error screen views
        mErrorScreenMsgTextView = (TextView) rootView.findViewById(R.id.error_screen_message_text_view);
        mErrorScreenRetryButton = (Button) rootView.findViewById(R.id.error_screen_retry_button);
        mErrorScreenRetryButton.setOnClickListener(new View.OnClickListener() {
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
    private void addItem(Ride c) {
        mRidesAdapter.addItem(c);

        // after the item addition, scrolls the recyclerview to the first position, so that the user
        // can see the inserted record
        if (mOfertasLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
            mOfertasLayoutManager.scrollToPosition(0);
        }
    }

    /**
     * Loads the user's ride offers. At the end of the method, the user's rides list is defined
     * as the dataset from mRideRecyclerView
     */
    public void loadMyRides() {
        mViewFlipper.setDisplayedChild(VIEW_PROGRESS);

        // Loads the rides from the cloud, only if the user is connected to the Internet
        if (NetworkUtil.isConnected(getActivity())) {
            Ride.getByDriverAsync((User) User.getCurrentUser()).continueWith(new Continuation<List<Ride>, Void>() {
                @Override
                public Void then(Task<List<Ride>> task) throws Exception {
                    mViewFlipper.setDisplayedChild(VIEW_PADRAO);

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
