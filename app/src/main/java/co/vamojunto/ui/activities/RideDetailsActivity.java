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

package co.vamojunto.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.Ride;
import co.vamojunto.model.SeatRequest;
import co.vamojunto.model.User;
import co.vamojunto.ui.widget.ExpandableHeightGridView;
import co.vamojunto.util.DateUtil;
import co.vamojunto.util.NetworkUtil;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Activity where the user can view the details of a specific ride.
 *
 * @author Andrew C. Pacifico <andrewpcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class RideDetailsActivity extends ActionBarActivity {

    public static final String TAG = "RideDetailsActivity";

    // constants used to define the input extras names
    public static final String EXTRA_RIDE = "ride";

    /**
     * The Activity toolbar
     */
    private Toolbar mToolbarActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_details);

        RideDetailsFragment fragment = new RideDetailsFragment();
        fragment.setArguments(getIntent().getExtras());
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }

        setupAppBar();
    }

    /**
     * Initializes the activity's App Bar
     */
    private void setupAppBar() {
        mToolbarActionBar = (Toolbar) findViewById(R.id.tool_bar);

        setSupportActionBar(mToolbarActionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * A {@link android.support.v4.app.Fragment} where the user can view the details from a ride
     *
     * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
     * @version 1.0.0
     * @since 0.1.0
     */
    public static class RideDetailsFragment extends Fragment {

        private static final int VIEW_PROGRESS = 0;
        private static final int VIEW_PASSENGERS = 1;
        private static final int VIEW_ERROR = 2;

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
        }

        /**
         * The ride to be displayed on the screen
         */
        private Ride mRide;

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

        public RideDetailsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_ride_details, container, false);

            Bundle arguments = getArguments();
            mRide = arguments.getParcelable(RideDetailsActivity.EXTRA_RIDE);

            mRide.getPassengers().continueWith(new Continuation<List<User>, Object>() {
                @Override
                public Object then(Task<List<User>> task) throws Exception {
                    List<User> lst = task.getResult();

                    return null;
                }
            });

            initComponents(rootView);

            setHasOptionsMenu(true);

            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.menu_ride_details, menu);

            // enables the edit menu only if the user is the driver of this ride
            mEditMenu = menu.findItem(R.id.action_edit);
            if (! mRide.getDriver().equals(User.getCurrentUser())) {
                mEditMenu.setEnabled(false);
                mEditMenu.setVisible(false);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // handles action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            if (id == android.R.id.home) {
                getActivity().finish();
            } else if (id == R.id.action_edit) {
                Toast.makeText(getActivity(), "Calma aí arrombado, chegou agora e já quer tirar onda?"
                    + "Deixa de ser folgado ... Se liga!", Toast.LENGTH_LONG).show();
            }

            return super.onOptionsItemSelected(item);
        }

        /**
         * Initializes the screen
         *
         * @param rootView The Fragment's inflated layout.
         */
        private void initComponents(View rootView) {
            CircleImageView driverImageView = (CircleImageView) rootView.findViewById(R.id.driver_picture);

            // checks if the driver have a profile image
            if (mRide.getDriver().getProfileImage() != null)
                driverImageView.setImageBitmap(mRide.getDriver().getProfileImage());

            TextView driverNameTextView = (TextView) rootView.findViewById(R.id.driver_name_text_view);
            driverNameTextView.setText(mRide.getDriver().getName());

            TextView startingPointTextView = (TextView) rootView.findViewById(R.id.starting_point_text_view);
            startingPointTextView.setText(getString(R.string.from) + ": " +
                    mRide.getStartingPoint().getTitulo());

            TextView destinationTextView = (TextView) rootView.findViewById(R.id.destination_text_view);
            destinationTextView.setText(getString(R.string.to) + ": " +
                    mRide.getDestination().getTitulo());

            TextView datetimeTextView = (TextView) rootView.findViewById(R.id.datetime_text_view);
            datetimeTextView.setText(getString(R.string.when) + ": " +
                    DateUtil.getFormattedDateTime(getActivity(), mRide.getDatetime()));

            TextView detailsTextView = (TextView) rootView.findViewById(R.id.detais_text_view);
            detailsTextView.setText(getString(R.string.details) + ": " + mRide.getDetails());

            // enables the request a ride button, only if the user is not the driver of this ride,
            // and the ride have any seat available
            final Button requestSeatButton = (Button) rootView.findViewById(R.id.ask_button);
            if (mRide.getDriver().equals(User.getCurrentUser()) || mRide.getSeatsAvailable() == 0) {
                requestSeatButton.setVisibility(View.GONE);

            // if the user is not the driver, defines the button onclick event
            } else {
                requestSeatButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestSeatButtonOnClick();
                    }
                });
            }

            TextView seatsAvailableTextView =
                    (TextView) rootView.findViewById(R.id.seats_available_text_view);
            if (mRide.getSeatsAvailable() > 0) {
                seatsAvailableTextView.setText(getString(R.string.seats_available) + ": "
                        + mRide.getSeatsAvailable());
            } else {
                seatsAvailableTextView.setText(getString(R.string.no_seats_available));
            }

            mPassengersViewFlipper =
                    (ViewFlipper) rootView.findViewById(R.id.passengers_view_flipper);

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

            showPassengers();
        }

        /**
         * Called when the user clicks on the "request a seat" button. Sends the request to the
         * database, so the driver can view int, and can approve or reject.
         */
        private void requestSeatButtonOnClick() {
            SeatRequest request = new SeatRequest(User.getCurrentUser(), mRide);

            startLoading(getString(R.string.sending_seat_request));
            request.saveInBackground().continueWith(new Continuation<Void, Void>() {
                @Override
                public Void then(Task<Void> task) throws Exception {
                    stopLoading();

                    if (! task.isCancelled() && ! task.isFaulted()) {
                        Toast.makeText(getActivity(),
                                getString(R.string.seat_request_sent), Toast.LENGTH_LONG).show();
                    } else if (task.isFaulted()) {
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
            mPassengersViewFlipper.setDisplayedChild(VIEW_PROGRESS);

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
                                mPassengersViewFlipper.setDisplayedChild(VIEW_PASSENGERS);
                            } else {
                                mPassengersViewFlipper.setDisplayedChild(VIEW_ERROR);
                                mPassengersMessage.setText(getString(R.string.no_passengers));
                                mPassengersRetryButton.setVisibility(View.GONE);
                            }
                        } else if (task.isFaulted()) {
                            Log.d(TAG, task.getError().getMessage());

                            mPassengersViewFlipper.setDisplayedChild(VIEW_ERROR);
                            mPassengersMessage.setText(getString(R.string.error_load_passengers));
                            mPassengersRetryButton.setVisibility(View.VISIBLE);
                        }

                        return null;
                    }
                });
            } else {
                mPassengersViewFlipper.setDisplayedChild(VIEW_ERROR);
                mPassengersMessage.setText(getString(R.string.error_msg_no_internet_connection));
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
    }
}

