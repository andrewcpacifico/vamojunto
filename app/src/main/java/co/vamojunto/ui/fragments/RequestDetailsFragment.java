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
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.parse.ParseQuery;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.RequestMessage;
import co.vamojunto.model.RideRequest;
import co.vamojunto.model.User;
import co.vamojunto.ui.activities.MainActivity;
import co.vamojunto.ui.activities.RequestDetailsActivity;
import co.vamojunto.util.DateUtil;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A {@link android.support.v4.app.Fragment} where the user can view the details of a ride request
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class RequestDetailsFragment extends Fragment {

    private static final String TAG = "RequestDetailsFragment";

    private static final int VIEW_LOADING = 0;
    private static final int VIEW_ERROR = 1;
    private static final int VIEW_DEFAULT = 2;

    /**
     * The request to display the details.
     *
     * @since 0.1.0
     */
    private RideRequest mRequest;

    /**
     * Inflated EditText with the message to send to requester.
     *
     * @since 0.1.0
     */
    private EditText mMessageEditText;

    /**
     * Inflated ImageButton used to send the message to requester.
     *
     * @since 0.1.0
     */
    private ImageButton mSendMessageButton;

    /**
     * RecyclerView to list the messages sent on this request wall.
     *
     * @since 0.1.0
     */
    private RecyclerView mMessagesRecyclerView;

    /**
     * LayoutManager for mMessagesRecyclerView
     *
     * @since 0.1.0
     */
    private LinearLayoutManager mMessagesLayoutManager;

    /**
     * Adapter for mMessagesRecyclerView
     *
     * @since 0.1.0
     */
    private MessagesAdapter mMessagesAdapter;

    private Handler mHandler;

    /**
     * Flipper to switch between a loading view, an error screen view, and the fragment default view
     *
     * @since 0.1.0
     */
    private ViewFlipper mFlipper;

    public RequestDetailsFragment() { /* required default constructor, do not delete or edit this */ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_request_details, container, false);

        // hides the keyboard by default
        getActivity()
                .getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mRequest = RideRequest.getStoredInstance(RequestDetailsActivity.EXTRA_REQUEST);
        mHandler = new Handler();

        initComponents(rootView);

        // if no request instance was passed to activity, fetches the ride request data before
        // init the screen
        if (mRequest == null) {
            fetchRequestData();
        } else {
            loadMessages();
        }

        setHasOptionsMenu(true);

        return rootView;
    }

    /**
     * Fetch ride request data if no request instance was passed to activity.
     *
     * @since 0.1.0
     */
    private void fetchRequestData() {
        mFlipper.setDisplayedChild(VIEW_LOADING);

        String requestId = getActivity().getIntent()
                .getStringExtra(RequestDetailsActivity.EXTRA_REQUEST_ID);

        ParseQuery<RideRequest> query = ParseQuery.getQuery(RideRequest.class);
        query.include(RideRequest.FIELD_REQUESTER);
        query.getInBackground(requestId).continueWith(new Continuation<RideRequest, Void>() {
            @Override
            public Void then(Task<RideRequest> task) throws Exception {
                if (!task.isCancelled() && !task.isFaulted()) {
                    mRequest = task.getResult();
                    mMessagesAdapter.setRequest(mRequest);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mFlipper.setDisplayedChild(VIEW_DEFAULT);
                            loadMessages();
                        }
                    });
                }

                return null;
            }
        });
    }

    /**
     * Load the messages sent to context ride request.
     *
     * @since 0.1.0
     */
    private void loadMessages() {
        mRequest.getMessagesAsync().continueWith(new Continuation<List<RequestMessage>, Void>() {
            @Override
            public Void then(Task<List<RequestMessage>> task) throws Exception {
                mMessagesAdapter.setDataset(task.getResult());

                return null;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handles action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
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

        return super.onOptionsItemSelected(item);
    }

    /**
     * Initializes screen components.
     *
     * @param rootView Inflated layout.
     * @since 0.1.0
     */
    private void initComponents(View rootView) {
        mFlipper = (ViewFlipper) rootView.findViewById(R.id.flipper);

        mMessagesLayoutManager = new LinearLayoutManager(getActivity());
        mMessagesAdapter = new MessagesAdapter(getActivity(), mRequest);

        mMessagesRecyclerView = (RecyclerView) rootView.findViewById(R.id.request_details_recyclerview);
        mMessagesRecyclerView.setAdapter(mMessagesAdapter);
        mMessagesRecyclerView.setLayoutManager(mMessagesLayoutManager);

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

        mSendMessageButton = (ImageButton) rootView.findViewById(R.id.send_message_button);
        // on sendMessageButton click, adds a new message to the recyclerview, and sends this
        // message to server, then it will be displayed on the ride request details screen.
        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // the message to add on the recyclerview
                RequestMessage message = new RequestMessage(
                    mMessageEditText.getText().toString(),
                    User.getCurrentUser(),
                    mRequest
                );

                // adds message to recyclerview
                final int position = mMessagesAdapter.addItem(message);
                mMessageEditText.setText("");
                mMessagesRecyclerView.scrollToPosition(mMessagesAdapter.getItemCount() - 1);

                // save message on cloud database
                message.saveInBackground().continueWith(new Continuation<Void, Void>() {
                    @Override
                    public Void then(Task<Void> task) throws Exception {
                        mMessagesAdapter.itemChanged(position);

                        return null;
                    }
                });
            }
        });
    }

    /**
     * Adapter for the RecyclerView on this screen.
     *
     * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
     * @since 0.1.0
     * @version 1.0.0
     */
    public static class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

        /**
         * Code for the viewType used on the first view, where the request details are displayed.
         *
         * @since 0.1.0
         */
        public static int VIEW_DETAILS = 0;

        /**
         * Code for the viewType of the all other views, where the messages are displayed.
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
             * Inflated TextView with the message date.
             *
             * @since 0.1.0
             */
            private TextView messageTimeTextView;

            public ViewHolder(View itemView, int viewType) {
                super(itemView);

                if (viewType == VIEW_DETAILS) {
                    requesterImage = (CircleImageView) itemView.findViewById(R.id.requester_picture);
                    requesterNameTextView = (TextView) itemView.findViewById(R.id.requester_name_text_view);
                    startingPointTextView = (TextView) itemView.findViewById(R.id.starting_point_text_view);
                    destinationTextView = (TextView) itemView.findViewById(R.id.destination_text_view);
                    datetimeTextView = (TextView) itemView.findViewById(R.id.datetime_text_view);
                    detailsTextView = (TextView) itemView.findViewById(R.id.details_text_view);
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
         *
         * @since 0.1.0
         */
        private Context mContext;

        /**
         * The request to display the details on the first item of the recyclerview.
         *
         * @since 0.1.0
         */
        private RideRequest mRequest;

        /**
         * A handler to run code on the main thread.
         *
         * @since 0.1.0
         */
        private Handler mHandler;

        /**
         * The dataset containing the messages sent to this request. These messages will be
         * displayed on the recyclerview.
         *
         * @since 0.1.0
         */
        private List<RequestMessage> mMessageDataset;

        /**
         * A class constructor to initialize some adapter fields.
         *
         * @param context The current context.
         * @param request The request to display the details.
         */
        public MessagesAdapter(Context context, RideRequest request) {
            mContext = context;
            mRequest = request;
            mHandler = new Handler();
        }

        /**
         * Set the data of the request to display the details.
         *
         * @param request The request instance.
         * @since 0.1.0
         */
        public void setRequest(RideRequest request) {
            mRequest = request;
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
         * @since 0.1.0
         */
        public void setDataset(List<RequestMessage> dataset) {
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
         * @since 0.1.0
         * @return The position where the item was inserted.
         */
        public int addItem(RequestMessage message) {
            mMessageDataset.add(message);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemInserted(mMessageDataset.size() + 1);
                }
            });

            return mMessageDataset.size();
        }

        public void itemChanged(final int position) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemChanged(position);
                }
            });
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
            View v;

            if (getItemViewType(position) == VIEW_DETAILS) {
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.request_details, viewGroup, false);
            } else {
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.recyclerview_request_details_messages, viewGroup, false);
            }

            return new ViewHolder(v, getItemViewType(position));
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            if (getItemViewType(position) == VIEW_DETAILS) {
                if (mRequest != null) {
                    viewHolder.requesterImage.setImageBitmap(mRequest.getRequester().getProfileImage());
                    viewHolder.requesterNameTextView.setText(mRequest.getRequester().getName());
                    viewHolder.startingPointTextView.setText(mRequest.getStartingPoint().getTitulo());
                    viewHolder.destinationTextView.setText(mRequest.getDestination().getTitulo());
                    viewHolder.datetimeTextView.setText(DateUtil.getFormattedDateTime(mContext, mRequest.getDatetime()));
                    viewHolder.detailsTextView.setText(mRequest.getDetails());
                }
            } else {
                // the message to display on this item
                RequestMessage message = mMessageDataset.get(position - 1);

                viewHolder.senderImageView.setImageBitmap(message.getSender().getProfileImage());
                viewHolder.senderNameTextView.setText(message.getSender().getName());
                viewHolder.messageTextView.setText(message.getMessage());
                viewHolder.messageTimeTextView.setText(getFormattedDate(message.getCreatedAt()));
            }
        }

        /**
         * Return the request date on a specific format for this feed.
         *
         * @param date The request date.
         * @return The formatted date.
         * @since 0.1.0
         */
        public String getFormattedDate(Date date) {
            if (date == null) {
                return mContext.getString(R.string.sending);
            }

            long diff = new Date().getTime() - date.getTime();
            long diffSeconds = (diff / 1000) % 60;
            long diffMinutes = (diff / (1000 * 60)) % 60;
            long diffHours = (diff / (1000 * 60 * 60)) % 60;

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
