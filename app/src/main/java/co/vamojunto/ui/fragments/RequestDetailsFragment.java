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
import android.os.Bundle;
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

import co.vamojunto.R;
import co.vamojunto.model.RideRequest;
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
    private ImageButton mSendButton;

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

        initComponents(rootView);

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handles action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            getActivity().finish();
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
        mMessagesLayoutManager = new LinearLayoutManager(getActivity());
        mMessagesAdapter = new MessagesAdapter(getActivity(), mRequest);

        mMessagesRecyclerView = (RecyclerView) rootView.findViewById(R.id.request_details_recyclerview);
        mMessagesRecyclerView.setAdapter(mMessagesAdapter);
        mMessagesRecyclerView.setLayoutManager(mMessagesLayoutManager);

    }

    public static class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

        public static int VIEW_DETAILS = 0;
        public static int VIEW_MESSAGE = 1;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            /*
             * Widgets for details view.
             * */
            /**
             * Inflated ImageView with the requester profile image
             *
             * @since 0.1.0
             */
            private CircleImageView mRequesterImage;

            /**
             * Inflated TextView with the requester name.
             *
             * @since 0.1.0
             */
            private TextView mRequesterNameTextView;

            /**
             * Inflated TextView with the request starting point.
             *
             * @since 0.1.0
             */
            private TextView mStartingPointTextView;

            /**
             * Inflated TextView with the request destination point.
             *
             * @since 0.1.0
             */
            private TextView mDestinationTextView;

            /**
             * Inflated TextView with the request date and time.
             *
             * @since 0.1.0
             */
            private TextView mDatetimeTextView;

            /**
             * Inflated TextView with the request details.
             *
             * @since 0.1.0
             */
            private TextView mDetailsTextView;

            public ViewHolder(View itemView, int viewType) {
                super(itemView);

                if (viewType == VIEW_DETAILS) {
                    initDetailsComponents(itemView);
                }
            }

            private void initDetailsComponents(View itemView) {
                mRequesterImage = (CircleImageView) itemView.findViewById(R.id.requester_picture);

                mRequesterNameTextView = (TextView) itemView.findViewById(R.id.requester_name_text_view);

                mStartingPointTextView = (TextView) itemView.findViewById(R.id.starting_point_text_view);

                mDestinationTextView = (TextView) itemView.findViewById(R.id.destination_text_view);

                mDatetimeTextView = (TextView) itemView.findViewById(R.id.datetime_text_view);

                mDetailsTextView = (TextView) itemView.findViewById(R.id.details_text_view);
            }

        }

        private Context mContext;

        private RideRequest mRequest;

        public MessagesAdapter(Context context, RideRequest request) {
            mContext = context;
            mRequest = request;
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
                viewHolder.mRequesterImage.setImageBitmap(mRequest.getRequester().getProfileImage());
                viewHolder.mRequesterNameTextView.setText(mRequest.getRequester().getName());
                viewHolder.mStartingPointTextView.setText(mRequest.getStartingPoint().getTitulo());
                viewHolder.mDestinationTextView.setText(mRequest.getDestination().getTitulo());
                viewHolder.mDatetimeTextView.setText(DateUtil.getFormattedDateTime(mContext, mRequest.getDatetime()));
                viewHolder.mDetailsTextView.setText(mRequest.getDetails());
            }
        }

        @Override
        public int getItemCount() {
            return 300;
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
