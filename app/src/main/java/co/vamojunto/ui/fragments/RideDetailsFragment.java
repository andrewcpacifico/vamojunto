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


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.parse.ParseQuery;

import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.Ride;
import co.vamojunto.model.SeatRequest;
import co.vamojunto.model.User;
import co.vamojunto.ui.activities.MainActivity;
import co.vamojunto.ui.activities.RideDetailsActivity;
import co.vamojunto.ui.activities.SeatRequestsActivity;
import co.vamojunto.ui.widget.ExpandableHeightGridView;
import co.vamojunto.util.DateUtil;
import co.vamojunto.util.NetworkUtil;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A {@link android.support.v4.app.Fragment} where the user can view the details from a ride
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class RideDetailsFragment extends android.support.v4.app.Fragment {

    private static final String TAG = "RideDetailsFragment";

    private static final int PASSENGERS_VIEW_PROGRESS = 0;
    private static final int PASSENGERS_VIEW_MAIN = 1;
    private static final int PASSENGERS_VIEW_ERROR = 2;

    private static final int VIEW_PROGRESS = 0;
    private static final int VIEW_ERROR = 1;
    private static final int VIEW_MAIN = 2;

    private CircleImageView mDriverImageView;
    private TextView mDriverNameTextView;
    private TextView mStartingPointTextView;
    private TextView mDestinationTextView;
    private TextView mDatetimeTextView;
    private TextView mDetailsTextView;
    private Button mRequestSeatButton;
    private TextView mSeatsAvailableTextView;

    /**
     * An adapter used by the {@link android.widget.GridView} where the confirmed passengers on
     * the ride will be displayed.
     *
     * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
     * @version 1.0.0
     * @since 0.1.0
     */
    private static class PassengersGridAdapter extends BaseAdapter {

        private List<User> mDataset;
        private Context mContext;
        private Handler mHandler;

        public PassengersGridAdapter(Context context, List<User> dataset) {
            mContext = context;
            mDataset = dataset;
            mHandler = new Handler(Looper.getMainLooper());
        }

        public void setDataset(List<User> dataset) {
            mDataset = dataset;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getCount() {
            if (mDataset == null)
                return 0;

            return mDataset.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View gridView;

            if (convertView == null) {  // if it's not recycled, initialize some attributes
                gridView = LayoutInflater.from(mContext)
                        .inflate(R.layout.item_passenger, parent, false);
            } else {
                gridView = convertView;
            }

            if (mDataset.get(position).getProfileImage() != null) {
                CircleImageView passengerPicture =
                        (CircleImageView) gridView.findViewById(R.id.passenger_picture);
                passengerPicture.setImageBitmap(mDataset.get(position).getProfileImage());
            }

            TextView passengerName = (TextView) gridView.findViewById(R.id.passenger_name);
            passengerName.setText(mDataset.get(position).getName());

            return gridView;
        }

        /**
         * Checks if the adapater dataset has any entry for a given user.
         *
         * @param user The user to find in dataset.
         * @return <code>true</code> if the user is on dataset, and <code>false</code> if not.
         */
        public boolean hasPassenger(User user) {
            if (mDataset == null)
                return false;

            return mDataset.indexOf(user) != -1;
        }
    }

    /**
     * The ride to be displayed on the screen
     */
    private Ride mRide;

    /**
     * This field is used when no ride instance is passed to activity. In this cases a ride id is
     * required to fetch data before display the screen to user.
     */
    private String mRideId;

    /**
     * The edit ride menu item
     */
    private MenuItem mEditMenu;

    /**
     * The Grid where the ride passengers is displayed
     */
    private ExpandableHeightGridView mPassengersGrid;

    /**
     * Adapter for the passengers GridView
     */
    private PassengersGridAdapter mAdapter;

    /**
     * ViewFlipper used to alternate between views on the passengers area
     */
    private ViewFlipper mPassengersViewFlipper;

    /**
     * ViewFlipper used to alternate between views on main area.
     */
    private ViewFlipper mViewFlipper;

    /**
     * TextView used to display a message to the user, when an error occurs on the passengers loading
     */
    private TextView mPassengersMessage;

    /**
     * If an error occurs when the passengers is being loaded, this button is used to retry
     * load the ride passengers
     */
    private TextView mPassengersRetryButton;

    /**
     * Progress dialog displayed when some network task is being executed.
     */
    private ProgressDialog mProgressDialog;

    /**
     * Handler used to run code on the main thread
     */
    private Handler mHandler;

    public RideDetailsFragment() {
        mHandler = new Handler(Looper.myLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ride_details, container, false);

        // gets the data sent to activity
        mRide = Ride.getStoredInstance(RideDetailsActivity.EXTRA_RIDE);

        initComponents(rootView);

        setHasOptionsMenu(true);

        return rootView;
    }

    /**
     * Always load the confirmed passengers on fragment start.
     */
    @Override
    public void onStart() {
        super.onStart();

        // if the ride data was already fetched, show the ride passengers on screen
        if (mRide != null) {
            showPassengers();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handles action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_INITIAL_VIEW, MainActivity.VIEW_MY_RIDES);

            NavUtils.navigateUpTo(getActivity(), intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Initializes the screen
     *
     * @param rootView The Fragment's inflated layout.
     */
    private void initComponents(View rootView) {
        mDriverImageView = (CircleImageView) rootView.findViewById(R.id.driver_picture);

        mDriverNameTextView = (TextView) rootView.findViewById(R.id.driver_name_text_view);

        mStartingPointTextView = (TextView) rootView.findViewById(R.id.starting_point_text_view);

        mDestinationTextView = (TextView) rootView.findViewById(R.id.destination_text_view);

        mDatetimeTextView = (TextView) rootView.findViewById(R.id.datetime_text_view);

        mDetailsTextView = (TextView) rootView.findViewById(R.id.detais_text_view);

        mRequestSeatButton = (Button) rootView.findViewById(R.id.ask_button);

        mSeatsAvailableTextView = (TextView) rootView.findViewById(R.id.seats_available_text_view);

        mPassengersViewFlipper =
                (ViewFlipper) rootView.findViewById(R.id.passengers_view_flipper);

        mViewFlipper = (ViewFlipper) rootView.findViewById(R.id.flipper);

        mPassengersGrid = (ExpandableHeightGridView) rootView.findViewById(R.id.grid_passengers);
        mAdapter = new PassengersGridAdapter(getActivity(), null);
        mPassengersGrid.setAdapter(mAdapter);
        mPassengersGrid.setExpanded(true);

        mPassengersMessage = (TextView) rootView.findViewById(R.id.error_message);

        mPassengersRetryButton = (TextView) rootView.findViewById(R.id.retry_button);
        mPassengersRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPassengers();
            }
        });

        // if no ride was sent to activity, fetch ride data and shows a progress bar to user while
        // this process is being executed
        if (mRide == null) {
            mRideId = getActivity().getIntent()
                    .getStringExtra(RideDetailsActivity.EXTRA_RIDE_ID);
            fetchRideData();
        } else {
            bindRideData();
        }
    }

    /**
     * Binds the data from mRide to corresponding components on screen.
     */
    private void bindRideData() {
        // finishes the method if mRide have no data to display
        if (mRide == null) return;

        // checks if the driver have a profile image
        if (mRide.getDriver().getProfileImage() != null)
            mDriverImageView.setImageBitmap(mRide.getDriver().getProfileImage());

        mDriverNameTextView.setText(mRide.getDriver().getName());

        mStartingPointTextView.setText(getString(R.string.from) + ": " +
                mRide.getStartingPoint().getTitulo());

        mDestinationTextView.setText(getString(R.string.to) + ": " +
                mRide.getDestination().getTitulo());

        mDatetimeTextView.setText(getString(R.string.when) + ": " +
                DateUtil.getFormattedDateTime(getActivity(), mRide.getDatetime()));

        mDetailsTextView.setText(getString(R.string.details) + ": " + mRide.getDetails());

        // configures the bottom screen button based on the following cases:
        //   - if the user is not the driver of this ride, and the ride still have seats available,
        // the button will be displayed and will be used to request a seat on the ride
        //   - if the user is not the driver of this ride, but the ride do not have any seats
        // available, the button will be hide
        //   - if the user is the driver of this ride, the button will always be displayed and
        // will be used to redirect the user to a screen where he can view the seat requests
        // made to this ride.
        if ( ! mRide.getDriver().equals(User.getCurrentUser()) ) {
            // case 1, the user is not the driver, and there is seats available
            if (mRide.getSeatsAvailable() > 0) {
                // sets the button action, to request a seat on the ride
                mRequestSeatButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestSeatButtonOnClick();
                    }
                });
                // case 2, the user is not the driver, but there is no seats available
            } else {
                // hide the button
                mRequestSeatButton.setVisibility(View.GONE);
            }
            // case 3, the user is the driver
        } else {
            // changes the button label
            mRequestSeatButton.setText(getString(R.string.view_requests));

            // sets the button action, to show the SeatRequestsActivity
            mRequestSeatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // sends the ride to SeatRequestsActivity
                    Ride.storeInstance(SeatRequestsActivity.INPUT_RIDE, mRide);

                    Intent intent = new Intent(getActivity(), SeatRequestsActivity.class);
                    startActivity(intent);
                }
            });
        }

        if (mRide.getSeatsAvailable() > 0) {
            mSeatsAvailableTextView.setText(getString(R.string.seats_available) + ": "
                    + mRide.getSeatsAvailable());
        } else {
            mSeatsAvailableTextView.setText(getString(R.string.no_seats_available));
        }

        showPassengers();
    }

    /**
     * Called when the user clicks on the "request a seat" button. Sends the request to the
     * database, so the driver can view int, and can approve or reject.
     */
    private void requestSeatButtonOnClick() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setTitle(getString(R.string.type_a_message));
        alert.setMessage(getString(R.string.enter_a_message_to_seat_request));

        // Set an EditText view to get user input
        final EditText input = new EditText(getActivity());
        alert.setView(input);

        alert.setPositiveButton(getString(R.string.send_seat_request), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String msg = input.getText().toString();
                requestSeat(msg);
            }
        });

        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // cancel
            }
        });

        alert.show();
    }

    /**
     * Request a seat for the current user on the viewing ride.
     *
     * @param message The message sent by the user.
     */
    private void requestSeat(String message) {
        final SeatRequest request = new SeatRequest(User.getCurrentUser(), mRide, message);

        // checks if user is connected to the Internet
        if ( ! NetworkUtil.isConnected(getActivity())) {
            Toast.makeText(getActivity(),
                    getString(R.string.errormsg_no_internet_connection),
                    Toast.LENGTH_LONG).show();
            return;
        } else {
            startLoading(getString(R.string.sending_seat_request));
        }

        // checks if the current user is already a passenger of the ride
        // TODO handle this error, when the passengers aren't loaded
        if (mAdapter.hasPassenger(User.getCurrentUser())) {
            Toast.makeText(getActivity(),
                    getString(R.string.errormsg_already_passenger),
                    Toast.LENGTH_LONG).show();

            stopLoading();
            return;
        }

        request.exists().continueWithTask(new Continuation<Boolean, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Boolean> task) throws Exception {
                if (task.isFaulted()) {
                    return Task.forError(task.getError());
                }

                // checks if the user already sent a request to this ride
                if (!task.getResult()) {
                    return request.send();
                }

                return Task.forError(new Exception("Request already sent"));
            }
        }).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                stopLoading();

                // checks if the user successfully sent the request
                if (!task.isCancelled() && !task.isFaulted()) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),
                                    getString(R.string.seat_request_sent),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (task.isFaulted()) {
                    // checks if the user tried to send a request to same ride more than once
                    if (task.getError().getMessage().equals("Request already sent")) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(),
                                        getString(R.string.errormsg_request_already_sent),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                        // if any other error occurs
                    } else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(),
                                        getString(R.string.errormsg_default),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    Log.e(TAG, task.getError().getMessage());
                } else {
                    Log.e(TAG, "Task cancelled.");
                }

                return null;
            }
        });
    }

    /**
     * Displays the list of confirmed passengers in this ride
     */
    private void showPassengers() {
        mPassengersViewFlipper.setDisplayedChild(PASSENGERS_VIEW_PROGRESS);

        if (NetworkUtil.isConnected(getActivity())) {
            mRide.getPassengers().continueWith(new Continuation<List<User>, Void>() {
                @Override
                public Void then(Task<List<User>> task) throws Exception {
                    if (!task.isCancelled() && !task.isFaulted()) {
                        List<User> l = task.getResult();

                        // if there is any passenger on this ride, show them, if not, show a message
                        // to the user
                        if (l.size() > 0) {
                            mAdapter.setDataset(l);
                            mPassengersViewFlipper.setDisplayedChild(PASSENGERS_VIEW_MAIN);
                        } else {
                            mPassengersViewFlipper.setDisplayedChild(PASSENGERS_VIEW_ERROR);
                            mPassengersMessage.setText(getString(R.string.no_passengers));
                            mPassengersRetryButton.setVisibility(View.GONE);
                        }
                    } else if (task.isFaulted()) {
                        Log.d(TAG, task.getError().getMessage());

                        mPassengersViewFlipper.setDisplayedChild(PASSENGERS_VIEW_ERROR);
                        mPassengersMessage.setText(getString(R.string.error_load_passengers));
                        mPassengersRetryButton.setVisibility(View.VISIBLE);
                    }

                    return null;
                }
            });
        } else {
            mPassengersViewFlipper.setDisplayedChild(PASSENGERS_VIEW_ERROR);
            mPassengersMessage.setText(getString(R.string.errormsg_no_internet_connection));
            mPassengersRetryButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Displays a ProgressDialog on the screen
     */
    private void startLoading(String msg) {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(msg);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    /**
     * Dismisses the ProgressDialog
     */
    private void stopLoading() {
        mProgressDialog.dismiss();
        mProgressDialog = null;
    }

    /**
     * Fetches the ride data from cloud, and updates the screen to show the data.
     */
    private void fetchRideData() {
        mViewFlipper.setDisplayedChild(VIEW_PROGRESS);
        Log.i(TAG, "Fetching ride data to display...");

        ParseQuery<Ride> query = ParseQuery.getQuery(Ride.class);
        query.include(Ride.FIELD_DRIVER);
        query.getInBackground(mRideId).continueWith(new Continuation<Ride, Void>() {
            @Override
            public Void then(Task<Ride> task) throws Exception {
                if (! task.isCancelled() && ! task.isFaulted()) {
                    Log.i(TAG, "Ride data fetched.");

                    // task result contains the ride instance with complete data for mRide
                    mRide = task.getResult();

                    // visual components have to be executed on the main thread
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // displays the main screen
                            mViewFlipper.setDisplayedChild(VIEW_MAIN);

                            // binds mRide data to screen components
                            bindRideData();
                        }
                    });
                }

                return null;
            }
        });
    }
}

