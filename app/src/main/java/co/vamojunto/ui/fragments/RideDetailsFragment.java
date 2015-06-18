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


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.parse.ParseQuery;

import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.helpers.FacebookHelper;
import co.vamojunto.model.Place;
import co.vamojunto.model.RideOffer;
import co.vamojunto.model.SeatRequest;
import co.vamojunto.model.User;
import co.vamojunto.ui.activities.MainActivity;
import co.vamojunto.ui.activities.RideDetailsActivity;
import co.vamojunto.ui.activities.SeatRequestsActivity;
import co.vamojunto.ui.activities.VamoJuntoActivity;
import co.vamojunto.ui.activities.ViewLocationActivity;
import co.vamojunto.ui.widget.ExpandableHeightGridView;
import co.vamojunto.util.DateUtil;
import co.vamojunto.util.Globals;
import co.vamojunto.util.NetworkUtil;
import co.vamojunto.util.UIUtil;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A {@link android.support.v4.app.Fragment} where the user can view the details from a ride
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.1.0
 * @since 0.1.0
 */
public class RideDetailsFragment extends android.support.v4.app.Fragment
        implements Toolbar.OnMenuItemClickListener {

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
    private TextView mSeatsAvailableTextView;
    private TextView mCountTextView;
    private MenuItem mNotificationMenuItem;

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
        private final Context mContext;
        private final Handler mHandler;

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

            final CircleImageView passengerPicture =
                    (CircleImageView) gridView.findViewById(R.id.passenger_picture);

            mDataset.get(position).getProfileImage().continueWith(new Continuation<Bitmap, Void>() {
                @Override
                public Void then(final Task<Bitmap> task) throws Exception {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            passengerPicture.setImageBitmap(task.getResult());
                        }
                    });

                    return null;
                }
            });

            TextView passengerName = (TextView) gridView.findViewById(R.id.passenger_name);
            passengerName.setText(mDataset.get(position).getName());

            return gridView;
        }

        /**
         * Checks if the adapter dataset has any entry for a given user.
         *
         * @param user The user to find in dataset.
         * @return <code>true</code> if the user is on dataset, and <code>false</code> if not.
         */
        public boolean hasPassenger(User user) {
            return mDataset != null && mDataset.indexOf(user) != -1;

        }
    }

    /**
     * The ride to be displayed on the screen
     *
     * @since 0.1.0
     */
    private RideOffer mRideOffer;

    /**
     * This field is used when no ride instance is passed to activity. In this cases a ride id is
     * required to fetch data before display the screen to user.
     *
     * @since 0.1.0
     */
    private String mRideId;

    /**
     * Adapter for the passengers GridView
     *
     * @since 0.1.0
     */
    private PassengersGridAdapter mAdapter;

    /**
     * ViewFlipper used to alternate between views on the passengers area
     *
     * @since 0.1.0
     */
    private ViewFlipper mPassengersViewFlipper;

    /**
     * ViewFlipper used to alternate between views on main area.
     *
     * @since 0.1.0
     */
    private ViewFlipper mViewFlipper;

    /**
     * TextView used to display a message to the user, when an error occurs on the passengers loading
     *
     * @since 0.1.0
     */
    private TextView mPassengersMessage;

    /**
     * If an error occurs when the passengers is being loaded, this button is used to retry
     * load the ride passengers
     *
     * @since 0.1.0
     */
    private TextView mPassengersRetryButton;

    /**
     * Inflated TextView with a message on error screen.
     *
     * @since 0.1.0
     */
    private TextView mErrorMsgTextView;

    /**
     * Handler used to run code on the main thread
     *
     * @since 0.1.0
     */
    private Handler mHandler;

    /**
     * The menu item where the user have the option to delete the ride offer.
     *
     * @since 0.4.0
     */
    private MenuItem mDeleteMenuItem;

    /**
     * Inflated layout.
     *
     * @since 0.1.0
     */
    private View mRootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_ride_details, container, false);

        // gets the data sent to activity
        mRideOffer = RideOffer.getStoredInstance(RideDetailsActivity.EXTRA_RIDE);

        mHandler = new Handler();

        initComponents(mRootView);
        setupAppBar();

        return mRootView;
    }

    /**
     * Always load the confirmed passengers on fragment start.
     *
     * @since 0.1.0
     */
    @Override
    public void onStart() {
        super.onStart();

        // if the ride data was already fetched, show the ride passengers on screen, and update
        // the driver menu on the app bar
        if (mRideOffer != null) {
            showPassengers();
            displayDriverMenu();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // handles action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_delete) {
            deleteMenuItemAction();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Toolbar getAppBar() {
        return ((VamoJuntoActivity) getActivity()).getAppBar();
    }

    /**
     * Customize application bar for this fragment
     *
     * @since 0.6.0
     */
    private void setupAppBar() {
        Toolbar appBar = ((VamoJuntoActivity) getActivity()).getAppBar();
        appBar.setTitle(R.string.title_activity_ride_details);
        appBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        appBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity().isTaskRoot()) {
                    // code to navigate up to MainActivity
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_INITIAL_VIEW, MainActivity.VIEW_MY_RIDES);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(intent);
                    getActivity().finish();
                } else {
                    getActivity().finish();
                }
            }
        });

        Menu menu = appBar.getMenu();
        appBar.setOnMenuItemClickListener(this);
        appBar.inflateMenu(R.menu.menu_ride_details);

        // the delete menu is not visible by default, later when the ride data is fetched, if the
        // user is the driver, the menu will be visible again
        mDeleteMenuItem = menu.findItem(R.id.action_delete);
        mDeleteMenuItem.setVisible(false);

        mNotificationMenuItem = menu.findItem(R.id.action_notifications);
        mNotificationMenuItem.setVisible(false);
        mCountTextView = (TextView) mNotificationMenuItem.getActionView().findViewById(R.id.hotlist_hot);

        // define the actions for the notification menu, the message displayed on long click, and
        // the the action to execute when the menu is clicked
        new NotificationMenuItemListener(
                mNotificationMenuItem.getActionView(),
                getString(R.string.view_seat_requests)
        ) {
            @Override
            public void onClick(View v) {
                // sends the ride to SeatRequestsActivity
                RideOffer.storeInstance(SeatRequestsActivity.INPUT_RIDE, mRideOffer);

                Intent intent = new Intent(getActivity(), SeatRequestsActivity.class);
                startActivity(intent);
            }
        };

        displayDriverMenu();
    }

    /**
     * Called when the delete menu item is clicked by user. This method is responsible for cancel
     * the ride offer.
     *
     * @since 0.4.0
     */
    private void deleteMenuItemAction() {
        MaterialDialog.Builder confirmDialog =
                new MaterialDialog.Builder(getActivity())
                    .title(R.string.cancel_ride_confirmation_title)
                    .content(R.string.cancel_ride_confirmation_message)
                    .positiveText(R.string.yes)
                    .negativeText(R.string.no);

        confirmDialog.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                UIUtil.startLoading(getActivity(), getString(R.string.cancelling_ride));
                mRideOffer.cancel().continueWith(new Continuation<Void, Void>() {
                    @Override
                    public Void then(final Task<Void> task) throws Exception {
                        UIUtil.stopLoading();

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!task.isFaulted() && !task.isCancelled()) {
                                    new MaterialDialog.Builder(getActivity())
                                            .title(R.string.ride_offer_cancelled_title)
                                            .content(R.string.ride_offer_successfully_cancelled)
                                            .positiveText(R.string.ok)
                                            .dismissListener(new DialogInterface.OnDismissListener() {
                                                @Override
                                                public void onDismiss(DialogInterface dialog) {
                                                    bindRideData();
                                                    displayDriverMenu();
                                                }
                                            })
                                            .show();
                                } else if (task.isFaulted()) {
                                    new MaterialDialog.Builder(getActivity())
                                            .title(R.string.error)
                                            .content(R.string.errormsg_ride_cancel)
                                            .positiveText(R.string.ok)
                                            .show();

                                    Log.e(TAG, "Error on cancel ride", task.getError());
                                }
                            }
                        });

                        return null;
                    }
                });
            }
        });

        confirmDialog.show();
    }

    /**
     * Initializes the screen
     *
     * @param rootView The Fragment's inflated layout.
     * @since 0.1.0
     */
    private void initComponents(View rootView) {
        // inflates the error screen widgets
        mErrorMsgTextView = (TextView) rootView.findViewById(R.id.error_screen_message_text_view);

        Button tryAgainButton = (Button) rootView.findViewById(R.id.error_screen_retry_button);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchRideData();
            }
        });

        mDriverImageView = (CircleImageView) rootView.findViewById(R.id.requester_picture);
        mDriverNameTextView = (TextView) rootView.findViewById(R.id.requester_name_text_view);
        mStartingPointTextView = (TextView) rootView.findViewById(R.id.starting_point_text_view);
        mDestinationTextView = (TextView) rootView.findViewById(R.id.destination_text_view);
        mDatetimeTextView = (TextView) rootView.findViewById(R.id.datetime_text_view);
        mDetailsTextView = (TextView) rootView.findViewById(R.id.details_text_view);
        mSeatsAvailableTextView = (TextView) rootView.findViewById(R.id.seats_available_text_view);
        mPassengersViewFlipper =
                (ViewFlipper) rootView.findViewById(R.id.passengers_view_flipper);
        mViewFlipper = (ViewFlipper) rootView.findViewById(R.id.flipper);


        // the grid where the ride's passengers are displayed
        ExpandableHeightGridView mPassengersGrid = (ExpandableHeightGridView) rootView.findViewById(R.id.grid_passengers);
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
        if (mRideOffer == null) {
            mRideId = getActivity().getIntent()
                    .getStringExtra(RideDetailsActivity.EXTRA_RIDE_ID);
            fetchRideData();
        } else {
            bindRideData();
        }
    }

    /**
     * Display the menu items on action bar, with options that have to be available only if the
     * user is the driver.
     *
     * @since 0.4.0
     */
    private void displayDriverMenu() {
        if (mDeleteMenuItem != null && mNotificationMenuItem != null && mRideOffer != null) {
            if (mRideOffer.getDriver().equals(User.getCurrentUser()) && mRideOffer.isActive()) {
                mDeleteMenuItem.setVisible(true);
                mNotificationMenuItem.setVisible(true);

                updateNotificationCount(RideOffer.getSeatRequestsCount(getActivity(),
                        mRideOffer.getId()));

                //
                // display a tutorial for the notifications icon on the first time the user
                // open the ride details activity for a owned ride offer
                //
                final SharedPreferences settings = getActivity().getSharedPreferences(
                        Globals.DEFAULT_PREF_NAME,
                        Context.MODE_PRIVATE
                );
                // check if the tutorial for the ask a seat fab, have been already displayed to user
                boolean firstTime = settings.getBoolean(TAG + "seatreqnotif_firstTime", true);
                // get an editor for the preferences
                final SharedPreferences.Editor editor = settings.edit();
                if (firstTime) {
                    UIUtil.showCase(
                            getActivity(),
                            getString(R.string.tooltiptitle_seatreqnotif),
                            getString(R.string.tooltipmsg_seatreqnotif),
                            mNotificationMenuItem.getActionView()
                    ).continueWith(new Continuation<Void, Void>() {
                        @Override
                        public Void then(Task<Void> task) throws Exception {
                            UIUtil.showCase(
                                    getActivity(),
                                    getString(R.string.tooltiptitle_cancel_offer),
                                    getString(R.string.tooltipmsg_cancel_offer),
                                    getAppBar().findViewById(R.id.action_delete)
                            );

                            return null;
                        }
                    });
                    //change the preference value
                    editor.putBoolean(TAG + "seatreqnotif_firstTime", false);
                    editor.putBoolean(TAG + "offerCancel_firstTime", false);
                    editor.apply();
                }
            } else {
                mDeleteMenuItem.setVisible(false);
                mNotificationMenuItem.setVisible(false);
            }
        }
    }

    /**
     * Update the notification count on actionBar icon.
     *
     * @param count The new notification count.
     * @since 0.4.0
     */
    private void updateNotificationCount(int count) {
        if (count == 0) {
            mCountTextView.setVisibility(View.INVISIBLE);
        } else {
            mCountTextView.setVisibility(View.VISIBLE);
            mCountTextView.setText(String.valueOf(count));
        }
    }

    /**
     * Binds the data from mRide to corresponding components on screen.
     *
     * @since 0.1.0
     */
    private void bindRideData() {
        // finishes the method if mRide have no data to display
        if (mRideOffer == null) return;

        mRideOffer.getDriver().getProfileImage().continueWith(new Continuation<Bitmap, Void>() {
            @Override
            public Void then(Task<Bitmap> task) throws Exception {
                mDriverImageView.setImageBitmap(task.getResult());
//                mDriverImageView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent facebookIntent = FacebookHelper.getOpenFacebookIntent(
//                                getActivity(),
//                                mRideOffer.getDriver().getFacebookId()
//                        );
//                        startActivity(facebookIntent);
//                    }
//                });

                return null;
            }
        });

        mDriverNameTextView.setText(mRideOffer.getDriver().getName());

        mStartingPointTextView.setText(getString(R.string.from) + ": " +
                mRideOffer.getStartingPoint().getTitulo());
        mStartingPointTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Place.storeInstance(ViewLocationActivity.INITIAL_PLACE, mRideOffer.getStartingPoint());
                Intent intent = new Intent(getActivity(), ViewLocationActivity.class);
                startActivity(intent);
            }
        });

        mDestinationTextView.setText(getString(R.string.to) + ": " +
                mRideOffer.getDestination().getTitulo());
        mDestinationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Place.storeInstance(ViewLocationActivity.INITIAL_PLACE, mRideOffer.getDestination());
                Intent intent = new Intent(getActivity(), ViewLocationActivity.class);
                startActivity(intent);
            }
        });

        mDatetimeTextView.setText(getString(R.string.when) + ": " +
                DateUtil.getFormattedDateTime(getActivity(), mRideOffer.getDatetime()));
        mDetailsTextView.setText(getString(R.string.details) + ": " + mRideOffer.getDetails());

        FloatingActionButton fabAskSeat = (FloatingActionButton) mRootView.findViewById(R.id.fab_ask_seat);
        boolean isDriver = mRideOffer.getDriver().equals(User.getCurrentUser());

        if (isDriver || mRideOffer.getSeatsAvailable() == 0) {
            fabAskSeat.setVisibility(View.GONE);
        } else {
            fabAskSeat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestSeatButtonOnClick();
                }
            });

            // get the default preferences for app
            final SharedPreferences settings = getActivity().getSharedPreferences(
                    Globals.DEFAULT_PREF_NAME,
                    Context.MODE_PRIVATE
            );
            // check if the tutorial for the ask a seat fab, have been already displayed to user
            boolean firstTime = settings.getBoolean(TAG + "ask_seat_firstTime", true);
            if (firstTime) {
                UIUtil.showCase(
                        getActivity(),
                        getString(R.string.tooltiptitle_ask_seat),
                        getString(R.string.tooltipmsg_ask_seat),
                        fabAskSeat,
                        true
                );

                // change the preference value
                final SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(TAG + "ask_seat_firstTime", false);
                editor.apply();
            }
        }

        displayDriverMenu();

        if (mRideOffer.getSeatsAvailable() > 0) {
            mSeatsAvailableTextView.setText(getString(R.string.seats_available) + ": "
                    + mRideOffer.getSeatsAvailable());
        } else {
            mSeatsAvailableTextView.setText(getString(R.string.no_seats_available));
        }

        // displays the main screen
        mViewFlipper.setDisplayedChild(VIEW_MAIN);

        if (mRideOffer.isActive()) {
            showPassengers();
        } else {
            mRootView.findViewById(R.id.cancelled_view).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Called when the user clicks on the "request a seat" button. Sends the request to the
     * database, so the driver can view int, and can approve or reject.
     *
     * @since 0.1.0
     */
    private void requestSeatButtonOnClick() {
        new MaterialDialog.Builder(getActivity())
                .content(R.string.enter_a_message_to_seat_request)
                .title(R.string.type_a_message)
                .positiveText(R.string.send_seat_request)
                .negativeText(R.string.cancel)
                .input(getString(R.string.message_to_driver), "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog materialDialog, CharSequence input) {
                        String msg = input.toString().trim();

                        if (msg.equals("")) {
                            new MaterialDialog.Builder(getActivity())
                                    .title(R.string.errormsg_invalid_message)
                                    .content(R.string.errormsg_seat_req_msg_required)
                                    .iconRes(R.drawable.ic_error)
                                    .show();
                        } else {
                            requestSeat(msg);
                        }
                    }
                }).build().show();
    }

    /**
     * Request a seat for the current user on the viewing ride.
     *
     * @param message The message sent by the user.
     * @since 0.1.0
     */
    private void requestSeat(String message) {
        final SeatRequest request = new SeatRequest(User.getCurrentUser(), mRideOffer, message);

        // checks if user is connected to the Internet
        if ( ! NetworkUtil.isConnected(getActivity())) {
            Toast.makeText(getActivity(),
                    getString(R.string.errormsg_no_internet_connection),
                    Toast.LENGTH_LONG).show();
            return;
        } else {
            UIUtil.startLoading(getActivity(), getString(R.string.sending_seat_request));
        }

        // checks if the current user is already a passenger of the ride
        // TODO handle this error, when the passengers aren't loaded
        if (mAdapter.hasPassenger(User.getCurrentUser())) {
            Toast.makeText(getActivity(),
                    getString(R.string.errormsg_already_passenger),
                    Toast.LENGTH_LONG).show();

            UIUtil.stopLoading();
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
                UIUtil.stopLoading();

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
     *
     * @since 0.1.0
     */
    private void showPassengers() {
        if (! mRideOffer.isActive()) {
            return;
        }

        mPassengersViewFlipper.setDisplayedChild(PASSENGERS_VIEW_PROGRESS);

        if (NetworkUtil.isConnected(getActivity())) {
            mRideOffer.getPassengers().continueWith(new Continuation<List<User>, Void>() {
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
                        Log.e(TAG, task.getError().getMessage(), task.getError());

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
     * Fetches the ride data from cloud, and updates the screen to show the data.
     *
     * @since 0.1.0
     */
    private void fetchRideData() {
        if (NetworkUtil.isConnected(getActivity())) {
            mViewFlipper.setDisplayedChild(VIEW_PROGRESS);
            Log.i(TAG, "Fetching ride data to display...");

            ParseQuery<RideOffer> query = ParseQuery.getQuery(RideOffer.class);
            query.include(RideOffer.FIELD_DRIVER);
            query.getInBackground(mRideId).continueWith(new Continuation<RideOffer, Void>() {
                @Override
                public Void then(Task<RideOffer> task) throws Exception {
                    if (!task.isCancelled() && !task.isFaulted()) {
                        Log.i(TAG, "Ride data fetched.");

                        // task result contains the ride instance with complete data for mRide
                        mRideOffer = task.getResult();

                        // visual components have to be executed on the main thread
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // binds mRide data to screen components
                                bindRideData();
                            }
                        });
                    } else {
                        mViewFlipper.setDisplayedChild(VIEW_ERROR);
                        mErrorMsgTextView.setText(getString(R.string.errormsg_default));
                    }

                    return null;
                }
            });
        } else {
            mViewFlipper.setDisplayedChild(VIEW_ERROR);
            mErrorMsgTextView.setText(getString(R.string.errormsg_no_internet_connection));
        }
    }

    static abstract class NotificationMenuItemListener implements View.OnClickListener, View.OnLongClickListener {
        private String hint;
        private View view;

        NotificationMenuItemListener(View view, String hint) {
            this.view = view;
            this.hint = hint;
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override abstract public void onClick(View v);

        @Override public boolean onLongClick(View v) {
            final int[] screenPos = new int[2];
            final Rect displayFrame = new Rect();
            view.getLocationOnScreen(screenPos);
            view.getWindowVisibleDisplayFrame(displayFrame);
            final Context context = view.getContext();
            final int width = view.getWidth();
            final int height = view.getHeight();
            final int midy = screenPos[1] + height / 2;
            final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            Toast cheatSheet = Toast.makeText(context, hint, Toast.LENGTH_SHORT);
            if (midy < displayFrame.height()) {
                cheatSheet.setGravity(Gravity.TOP | Gravity.RIGHT,
                        screenWidth - screenPos[0] - width / 2, height);
            } else {
                cheatSheet.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, height);
            }
            cheatSheet.show();
            return true;
        }
    }

}

