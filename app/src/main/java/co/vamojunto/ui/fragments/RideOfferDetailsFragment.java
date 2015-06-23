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
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.Place;
import co.vamojunto.model.RideMessage;
import co.vamojunto.model.RideOffer;
import co.vamojunto.model.User;
import co.vamojunto.ui.activities.MainActivity;
import co.vamojunto.ui.activities.RideDetailsActivity;
import co.vamojunto.ui.activities.VamoJuntoActivity;
import co.vamojunto.ui.activities.ViewLocationActivity;
import co.vamojunto.util.DateUtil;
import co.vamojunto.util.Log;
import co.vamojunto.util.NetworkUtil;
import co.vamojunto.util.UIUtil;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A {@link android.support.v4.app.Fragment} where the user can view the details from a ride offer.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 2.0
 * @since 0.1.0
 */
public class RideOfferDetailsFragment extends android.support.v4.app.Fragment {

    private static final String TAG = "RideOfferDetailsFragment";

    private static final int VIEW_PROGRESS = 0;
    private static final int VIEW_ERROR = 1;
    private static final int VIEW_DEFAULT = 2;

    /**
     * The ride to be displayed on the screen
     *
     * @since 0.1.0
     */
    private RideOffer mRideOffer;

    /**
     * Handler used to run code on the main thread
     *
     * @since 0.1.0
     */
    private Handler mHandler;

    /**
     * ViewFlipper used to alternate between views on main area.
     *
     * @since 0.1.0
     */
    private ViewFlipper mViewFlipper;

    /**
     * Inflated TextView with a message on error screen.
     *
     * @since 0.1.0
     */
    private TextView mErrorScreenMsgTextView;

    /**
     * Inflated EditText with the message to send to requester.
     *
     * @since 0.1.0
     */
    private EditText mMessageEditText;

    /**
     * RecyclerView to list the messages sent on this request wall.
     *
     * @since 0.1.0
     */
    private RecyclerView mMessagesRecyclerView;

    /**
     * Adapter for mMessagesRecyclerView
     *
     * @since 0.1.0
     */
    private MessagesAdapter mMessagesAdapter;

    public RideOfferDetailsFragment() { /* required empty constructor*/ }

    /**
     * Creates an instance of this fragment. Use this method instead of the class constructor.
     *
     * @return A new {@link RideOfferDetailsFragment} instance.
     */
    public static RideOfferDetailsFragment newInstance() {
        return new RideOfferDetailsFragment();
    }

    /**
     * Initializes screen components.
     *
     * @param rootView Inflated layout.
     * @since 0.1.0
     */
    private void initComponents(View rootView) {
        // inflates the error screen widgets
        mErrorScreenMsgTextView = (TextView) rootView.findViewById(R.id.error_screen_message_text_view);
        Button retryButton = (Button) rootView.findViewById(R.id.error_screen_retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchRideData();
            }
        });

        mViewFlipper = (ViewFlipper) rootView.findViewById(R.id.flipper);

        LinearLayoutManager messagesLayoutManager = new LinearLayoutManager(getActivity());
        mMessagesAdapter = new MessagesAdapter(getActivity(), mRideOffer);

        mMessagesRecyclerView = (RecyclerView) rootView.findViewById(R.id.ride_details_recyclerview);
        mMessagesRecyclerView.setAdapter(mMessagesAdapter);
        mMessagesRecyclerView.setLayoutManager(messagesLayoutManager);

        mMessageEditText = (EditText) rootView.findViewById(R.id.message_edit_text);
        // on edittext click, scroll the recyclerview to last position
        mMessageEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMessagesRecyclerView.scrollToPosition(mMessagesAdapter.getItemCount() - 1);
                    }
                }, 1600);
            }
        });

        ImageButton sendMessageButton = (ImageButton) rootView.findViewById(R.id.send_message_button);
        // on sendMessageButton click, adds a new message to the recyclerview, and sends this
        // message to server, then it will be displayed on the ride details screen.
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String typedMessage = mMessageEditText.getText().toString().trim();

                if (typedMessage.equals("")) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.errormsg_required_field)
                            .content(R.string.errormsg_ride_message_required)
                            .positiveText(R.string.ok)
                            .build().show();
                } else {
                    // the message to add on the recyclerview
                    RideMessage message = new RideMessage(
                        typedMessage,
                        User.getCurrentUser(),
                        mRideOffer
                    );

                    // adds message to recyclerview
                    final int position = mMessagesAdapter.addItem(message);
                    mMessageEditText.setText("");
                    mMessagesRecyclerView.scrollToPosition(mMessagesAdapter.getItemCount() - 1);

                    // save message on cloud database
                    message.saveInBackground().continueWith(new Continuation<Void, Void>() {
                        @Override
                        public Void then(Task<Void> task) throws Exception {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mMessagesAdapter.notifyItemChanged(position);
                                }
                            });

                            return null;
                        }
                    });
                }
            }
        });
    }

    /**
     * Load the messages sent to the viewing ride offer.
     */
    private void loadMessages() {
        mRideOffer.getMessagesAsync().continueWith(new Continuation<List<RideMessage>, Void>() {
            @Override
            public Void then(Task<List<RideMessage>> task) throws Exception {
                mMessagesAdapter.setMessageDataset(task.getResult());

                return null;
            }
        });
    }

    /**
     * Fetches the ride data from cloud, and updates the screen to show the data.
     */
    private void fetchRideData() {
        // do nothing with no internet connection
        if (NetworkUtil.isConnected(getActivity())) {
            displayErrorScreen(R.string.errormsg_no_internet_connection);
            return;
        }

        mViewFlipper.setDisplayedChild(VIEW_PROGRESS);
        Log.i(TAG, "Fetching ride data to display...");

        String rideId = getActivity().getIntent()
                .getStringExtra(RideDetailsActivity.EXTRA_RIDE_ID);

        RideOffer.fetchData(rideId).continueWith(new Continuation<RideOffer, Void>() {
            @Override
            public Void then(Task<RideOffer> task) throws Exception {
                if (!task.isCancelled() && !task.isFaulted()) {
                    Log.i(TAG, "Ride data fetched.");

                    // task result contains the ride instance with complete data for mRide
                    mRideOffer = task.getResult();

                    mMessagesAdapter.setRideOffer(mRideOffer);

                    // visual components have to be executed on the main thread
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadMessages();
                            mViewFlipper.setDisplayedChild(VIEW_DEFAULT);
                        }
                    });
                } else {
                    displayErrorScreen();
                }

                return null;
            }
        });
    }

    /**
     * Display the error screen with default error message.
     */
    private void displayErrorScreen() {
        displayErrorScreen(R.string.errormsg_default);
    }

    /**
     * Display the error screen with a customized message.
     *
     * @param msgRes The string resource of the error message.
     */
    private void displayErrorScreen(@StringRes int msgRes) {
        mErrorScreenMsgTextView.setText(getString(msgRes));
        mViewFlipper.setDisplayedChild(VIEW_ERROR);
    }

    /**
     * Customize application bar for this fragment
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ride_details, container, false);

        // hides the keyboard by default
        UIUtil.hideKeyboard(getActivity());

        mRideOffer = RideOffer.getStoredInstance(RideDetailsActivity.EXTRA_RIDE);
        mHandler = new Handler();

        setupAppBar();
        initComponents(rootView);

        // if no ride offer instance was passed to activity, fetches the ride offer data before
        // init the screen
        if (mRideOffer == null) {
            fetchRideData();
        } else {
            mViewFlipper.setDisplayedChild(VIEW_DEFAULT);
            loadMessages();
        }

        return rootView;
    }

    /**
     * Adapter for the RecyclerView on this screen.
     *
     * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
     * @since 0.8.0
     * @version 1.0
     */
    public static class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

        /**
         * Code for the viewType used on the first view, where the request details are displayed.
         *
         * @since 0.8.0
         */
        public static int VIEW_DETAILS = 0;

        /**
         * Code for the viewType of the all other views, where the messages are displayed.
         *
         * @since 0.8.0
         */
        public static int VIEW_MESSAGE = 1;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            /*
             ---------------------------------------------------------------------------------------
             - Widgets for details view.
             ---------------------------------------------------------------------------------------
             */
            /**
             * Inflated ImageView with the requester profile image
             *
             * @since 0.1.0
             */
            private CircleImageView requesterImage;

            /**
             * Inflated TextView with the requester name.
             *
             * @since 0.1.0
             */
            private TextView requesterNameTextView;

            /**
             * Inflated TextView with the request starting point.
             *
             * @since 0.1.0
             */
            private TextView startingPointTextView;

            /**
             * Inflated TextView with the request destination point.
             *
             * @since 0.1.0
             */
            private TextView destinationTextView;

            /**
             * Inflated TextView with the request date and time.
             *
             * @since 0.1.0
             */
            private TextView datetimeTextView;

            /**
             * Inflated TextView with the request details.
             *
             * @since 0.1.0
             */
            private TextView detailsTextView;

            /*
             ---------------------------------------------------------------------------------------
             - Widgets for message items.
             ---------------------------------------------------------------------------------------
             */
            /**
             * Inflated image with the profile picture of the message sender.
             *
             * @since 0.1.0
             */
            private CircleImageView senderImageView;

            /**
             * Inflated TextView with the name of the message sender.
             *
             * @since 0.1.0
             */
            private TextView senderNameTextView;

            /**
             * Inflated TextView with the sent message.
             *
             * @since 0.1.0
             */
            private TextView messageTextView;

            /**
             * Panel where the starting point is displayed
             */
            private LinearLayout startingPointPanel;

            /**
             * Panel where the destination is displayed
             */
            private LinearLayout destinationPanel;

            /**
             * Inflated TextView with the message date.
             *
             * @since 0.1.0
             */
            private TextView messageTimeTextView;

            public ViewHolder(View itemView, int viewType) {
                super(itemView);

                if (viewType == VIEW_DETAILS) {
                    requesterImage = (CircleImageView) itemView.findViewById(R.id.driver_picture);
                    requesterNameTextView = (TextView) itemView.findViewById(R.id.driver_name_text_view);
                    startingPointTextView = (TextView) itemView.findViewById(R.id.starting_point_text_view);
                    destinationTextView = (TextView) itemView.findViewById(R.id.destination_text_view);
                    datetimeTextView = (TextView) itemView.findViewById(R.id.datetime_text_view);
                    detailsTextView = (TextView) itemView.findViewById(R.id.details_text_view);
                    startingPointPanel = (LinearLayout) itemView.findViewById(R.id.starting_point);
                    destinationPanel = (LinearLayout) itemView.findViewById(R.id.destination);
                } else {
                    senderImageView = (CircleImageView) itemView.findViewById(R.id.user_image);
                    senderNameTextView = (TextView) itemView.findViewById(R.id.user_name_text_view);
                    messageTextView = (TextView) itemView.findViewById(R.id.message_text_view);
                    messageTimeTextView = (TextView) itemView.findViewById(R.id.message_time_text_view);
                }
            }

        }

        /**
         * The current context.
         */
        private Context mContext;

        /**
         * The ride offer to display the details on the first item of the recyclerview.
         */
        private RideOffer mRideOffer;

        /**
         * A handler to run code on the main thread.
         */
        private Handler mHandler;

        /**
         * The dataset containing the messages sent to this ride offer. These messages will be
         * displayed on the recyclerview.
         */
        private List<RideMessage> mMessageDataset;

        /**
         * A class constructor to initialize some adapter fields.
         *
         * @param context The current context.
         * @param offer The offer to display the details.
         */
        public MessagesAdapter(Context context, RideOffer offer) {
            mContext = context;
            mRideOffer = offer;
            mHandler = new Handler();
        }

        /**
         * Return the request date on a specific format for this feed.
         *
         * @param date The request date.
         * @return The formatted date.
         * @since 0.1.0
         */
        private String getFormattedDate(Date date) {
            if (date == null) {
                return mContext.getString(R.string.sending);
            }

            long diff = new Date().getTime() - date.getTime();
//            long diffSeconds = (diff / 1000) % 60;
            long diffMinutes = (diff / (1000 * 60)) % 60;
            long diffHours = (diff / (1000 * 60 * 60));

            // if the message was sent a week ago, just return the formatted date
            if (diffHours > 168) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);

                return DateUtil.getFormattedDateTime(mContext, cal);

                // if the message was sent this week, but not today or yesterday return a message
                // with the days past since the message sending
            } else if (diffHours >= 48) {
                return diffHours/24 + " " + mContext.getString(R.string.days_ago);

                // if the message was sent yesterday
            } else if (diffHours >= 24) {
                return mContext.getString(R.string.yesterday);

                // if the message was sent more than a hour ago, return a message with the hours
                // past since the message sending
            } else if (diffHours > 1) {
                return diffHours + " " + mContext.getString(R.string.hours_ago);

            } else if (diffHours == 1) {
                return mContext.getString(R.string.an_hour_ago);

                // if the message was sent more than a minute ago, return a message with the minutes
                // past since the message sending
            } else if (diffMinutes > 1) {
                return diffMinutes + " " + mContext.getString(R.string.minutes_ago);
            } else {
                return mContext.getString(R.string.now);
            }
        }

        /**
         * Bind the ride data on the recycler view.
         *
         * @param viewHolder The inflated viewHolder to bind the data.
         */
        private void bindRideDetails(final ViewHolder viewHolder) {
            if (mRideOffer == null) {
                return;
            }

            viewHolder.requesterNameTextView.setText(mRideOffer.getDriver().getName());

            viewHolder.startingPointTextView.setText(mRideOffer.getStartingPoint().getTitulo());
            viewHolder.startingPointPanel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openLocationDetails(mRideOffer.getStartingPoint());
                }
            });

            viewHolder.destinationTextView.setText(mRideOffer.getDestination().getTitulo());
            viewHolder.destinationPanel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openLocationDetails(mRideOffer.getDestination());
                }
            });

            viewHolder.datetimeTextView
                    .setText(DateUtil.getFormattedDateTime(mContext, mRideOffer.getDatetime()));

            String rideDetails = mRideOffer.getDetails();
            if (rideDetails.trim().equals("")) {
                viewHolder.detailsTextView.setText(mContext.getString(R.string.no_additional_details));
            } else {
                viewHolder.detailsTextView.setText(rideDetails);
            }

            mRideOffer.getDriver().getProfileImage().continueWith(new Continuation<Bitmap, Void>() {
                @Override
                public Void then(Task<Bitmap> task) throws Exception {
                    viewHolder.requesterImage.setImageBitmap(task.getResult());

                    return null;
                }
            });
        }

        /**
         * Bind a message data on the recyclerview.
         *
         * @param viewHolder The inflated viewHolder where the data will be placed.
         * @param message The message to display
         */
        private void bindMessage(final ViewHolder viewHolder, RideMessage message) {
            viewHolder.senderNameTextView.setText(message.getSender().getName());
            viewHolder.messageTextView.setText(message.getMessage());
            viewHolder.messageTimeTextView.setText(getFormattedDate(message.getCreatedAt()));

            message.getSender().getProfileImage().continueWith(new Continuation<Bitmap, Void>() {
                @Override
                public Void then(Task<Bitmap> task) throws Exception {
                    viewHolder.senderImageView.setImageBitmap(task.getResult());

                    return null;
                }
            });
        }

        /**
         * Display the ViewLocationActivity for a given place. This method is called when the user
         * clicks on the ride starting point or destination.
         *
         * @param p The place to display
         */
        private void openLocationDetails(Place p) {
            Place.storeInstance(ViewLocationActivity.INITIAL_PLACE, p);
            Intent intent = new Intent(mContext, ViewLocationActivity.class);
            mContext.startActivity(intent);
        }

        /**
         * Set the data of the ride offer to display the details.
         *
         * @param offer The {@link RideOffer} instance.
         */
        public void setRideOffer(RideOffer offer) {
            mRideOffer = offer;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemChanged(0);
                }
            });
        }

        /**
         * Set the message dataset for the recyclerview.
         *
         * @param dataset The new dataset.
         */
        public void setMessageDataset(List<RideMessage> dataset) {
            mMessageDataset = dataset;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }

        /**
         * Add an item to the recyclerview.
         *
         * @param message The message to add.
         * @return The position where the item was inserted.
         */
        public int addItem(RideMessage message) {
            mMessageDataset.add(message);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                notifyItemInserted(mMessageDataset.size() + 1);
                }
            });

            return mMessageDataset.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
            View v;

            if (getItemViewType(position) == VIEW_DETAILS) {
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.recyclerview_ride_details, viewGroup, false);
            } else {
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.recyclerview_ride_details_messages, viewGroup, false);
            }

            return new ViewHolder(v, getItemViewType(position));
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, int position) {
            if (getItemViewType(position) == VIEW_DETAILS) {
                bindRideDetails(viewHolder);
            } else {
                // the message to display on this item
                RideMessage message = mMessageDataset.get(position - 1);

                bindMessage(viewHolder, message);
            }
        }

        @Override
        public int getItemCount() {
            int messageCount;

            if (mMessageDataset == null) {
                messageCount = 0;
            } else {
                messageCount = mMessageDataset.size();
            }

            return messageCount + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return VIEW_DETAILS;
            }

            return VIEW_MESSAGE;
        }
    }

}

