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

package co.vamojunto.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import co.vamojunto.R;
import co.vamojunto.model.SeatRequest;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adapter for the RecyclerView on the {@link co.vamojunto.ui.fragments.SeatRequestsFragment}
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class SeatRequestRecyclerViewAdapter extends RecyclerView.Adapter<SeatRequestRecyclerViewAdapter.ViewHolder> {

    /**
     * The ViewHolder for the items on the {@link co.vamojunto.ui.fragments.SeatRequestsFragment}.
     * Each item should display the requester name, picture, and a message. And gives the user
     * the choice to approve or reject the request.
     *
     * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
     * @version 1.0.0
     * @since 0.1.0
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        /**
         * The View that holds the requester picture
         */
        public CircleImageView mUserImage;

        /**
         * The View that holds the requester name
         */
        public TextView mUserNameTextView;

        /**
         * The View that holds the message sent by the requester, on the requesting moment.
         */
        public TextView mMessageTextView;

        /**
         * Class constructor
         *
         * @param itemView The root layout for the ViewHolder
         */
        public ViewHolder(View itemView) {
            super(itemView);

            mUserNameTextView = (TextView) itemView.findViewById(R.id.user_name_text_view);
            mUserImage = (CircleImageView) itemView.findViewById(R.id.user_image);
            mMessageTextView = (TextView) itemView.findViewById(R.id.message_text_view);
        }

    }

    /**
     * Dataset containing the items to show on the RecyclerView
     */
    private List<SeatRequest> mDataset;

    /**
     * {@link android.os.Handler} used to run code on the main thread.
     */
    private Handler mHandler;

    /**
     * Class constructor
     *
     * @param context The context used to instantiate the {@link android.os.Handler}
     */
    public SeatRequestRecyclerViewAdapter(Context context) {
        mHandler = new Handler(context.getMainLooper());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // creates the rootView for the item
        View rootItemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_seat_request, parent, false);

        return new ViewHolder(rootItemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SeatRequest request = mDataset.get(position);

        holder.mMessageTextView.setText(request.getMessage());
        holder.mUserNameTextView.setText(request.getUser().getName());
        holder.mUserImage.setImageBitmap(request.getUser().getProfileImage());
    }

    @Override
    public int getItemCount() {
        if (mDataset == null)
            return 0;

        return mDataset.size();
    }

    /**
     * Changes the dataset and notifies all listeners
     *
     * @param lst The new list of items to be used as dataset.
     */
    public void setDataset(List<SeatRequest> lst) {
        this.mDataset = lst;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

}
