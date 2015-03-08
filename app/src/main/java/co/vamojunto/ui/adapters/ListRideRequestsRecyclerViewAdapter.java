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
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import co.vamojunto.R;
import co.vamojunto.model.RideRequest;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Used to fill the {@link android.support.v7.widget.RecyclerView} of a ride requests list screen. These
 * lists are displayed on the user's ride requests administration screen, and in the groups walls.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class ListRideRequestsRecyclerViewAdapter extends RecyclerView.Adapter<ListRideRequestsRecyclerViewAdapter.ViewHolder> {

    /**
     * Stores the data that will be displayed on the screen
     */
    private List<RideRequest> mDataset;

    /**
     * {@link android.content.Context} of the RecyclerView for this adapter
     */
    private Context mContext;

    /**
     * Used to execute something on the main thread
     */
    private Handler mHandler;

    static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView mRequestorImage;
        public TextView mNameTextView;
        public TextView mStartingPointTextView;
        public TextView mDestinationTextView;
        public TextView mDateTextView;
        public TextView mTimeTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            mRequestorImage = (CircleImageView) itemView.findViewById(R.id.user_pic);
            mNameTextView = (TextView) itemView.findViewById(R.id.user_name_text_view);
            mStartingPointTextView = (TextView) itemView.findViewById(R.id.starting_point_text_view);
            mDestinationTextView = (TextView) itemView.findViewById(R.id.destination_text_view);
            mDateTextView = (TextView) itemView.findViewById(R.id.date_text_view);
            mTimeTextView = (TextView) itemView.findViewById(R.id.time_text_view);
        }
    }

    public ListRideRequestsRecyclerViewAdapter(Context context, List<RideRequest> dataset) {
        this.mDataset = dataset;
        this.mContext = context;
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Updates the RecyclereView dataset
     *
     * @param dataset The new dataset
     */
    public void setDataset(List<RideRequest> dataset) {
        this.mDataset = dataset;

        // notifies all the listeners that the dataset was changed. That action have to be executed
        // on the main thread to have immediate effect on the screen.
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    /**
     * Adds a new item to the dataset, then notifies all the listeners, so that the RecyclerView
     * is updated.
     *
     * @param r The {@link co.vamojunto.model.RideRequest} to be added to the dataset and consequently
     *          to the screen.
     */
    public void addItem(RideRequest r) {
        // the new item is added to the first position on the dataset,to be displayed at the top of the list
        this.mDataset.add(0, r);

        // same situation of the dataset changing, the code have to be executed on the main thread
        // to have an immediate effect on the screen
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyItemInserted(0);
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_ride_card, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RideRequest r = mDataset.get(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat(mContext.getString(R.string.date_format));
        SimpleDateFormat timeFormat = new SimpleDateFormat(mContext.getString(R.string.time_format));

        holder.mNameTextView.setText(r.getRequestor().getName());
        holder.mRequestorImage.setImageBitmap(r.getRequestor().getProfileImage());
        holder.mStartingPointTextView.setText(mContext.getString(R.string.de) + ": " + r.getStartingPoint().getTitulo());
        holder.mDestinationTextView.setText(mContext.getString(R.string.para) + ": " + r.getDestination().getTitulo());
        holder.mDateTextView.setText(dateFormat.format(r.getDatetime().getTime()));
        holder.mTimeTextView.setText(timeFormat.format(r.getDatetime().getTime()));
    }

    @Override
    public int getItemCount() {
        return this.mDataset.size();
    }
}
