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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import co.vamojunto.R;
import co.vamojunto.model.RideOffer;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Used to fill the {@link android.support.v7.widget.RecyclerView} of a ride list screen. These
 * lists are displayed on the user's rides administration screen, and in the groups walls.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 *
 * @version 1.0.0 First Version
 * @version 1.1.0 Display a cancelled stamp, if the rid was cancelled.
 *
 * @since 0.1.0
 */
public class RidesRecyclerViewAdapter extends RecyclerView.Adapter<RidesRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "ListRidesRecyclerViewAdapter";

    /**
     * The data to be displayed on the RecyclerView
     */
    private List<RideOffer> mDataset;

    /**
     * The RecyclerView Context
     */
    private Context mContext;

    /**
     * Used to execute stuff on the main Thread
     */
    private Handler mHandler;

    /**
     * Handles the item clicks
     */
    private OnItemClickListener mItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView mDriverImageView;
        public TextView mDriverNameTextView;
        public TextView mStartingPointTextView;
        public TextView mDestinationTextView;
        public TextView mDateTextView;
        public TextView mTimeTextView;
        public TextView mCancelledStampTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            mDriverImageView = (CircleImageView) itemView.findViewById(R.id.user_pic);
            mDriverNameTextView = (TextView) itemView.findViewById(R.id.user_name_text_view);
            mStartingPointTextView = (TextView) itemView.findViewById(R.id.starting_point_text_view);
            mDestinationTextView = (TextView) itemView.findViewById(R.id.destination_text_view);
            mDateTextView = (TextView) itemView.findViewById(R.id.date_text_view);
            mTimeTextView = (TextView) itemView.findViewById(R.id.time_text_view);
            mCancelledStampTextView = (TextView) itemView.findViewById(R.id.cancelled_stamp);
        }
    }

    public interface OnItemClickListener {
        public void OnItemClick(int position);
    }

    public RidesRecyclerViewAdapter(Context context, List<RideOffer> dataset, OnItemClickListener itemClickListener) {
        this.mDataset = dataset;
        this.mContext = context;
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mItemClickListener = itemClickListener;
    }

    /**
     * Updates the dataset
     *
     * @param dataset O novo dataset.
     */
    public void setDataset(List<RideOffer> dataset) {
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

    public List<RideOffer> getDataset() {
        return this.mDataset;
    }

    /**
     * Adds a new item to the dataset, then notifies all the listeners, so that the RecyclerView
     * is updated.
     *
     * @param r The {@link co.vamojunto.model.RideOffer} to be added to the dataset and consequently
     *          to the screen.
     */
    public void addItem(RideOffer r) {
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

    /**
     * Returns a {@link co.vamojunto.model.RideOffer} in a specific position of the RecyclerView
     *
     * @param position The position of the Ride wanted.
     * @return A {@link co.vamojunto.model.RideOffer} stored in a position of the dataset, or <code>null</code> if
     *         the position is invalid, or dataset is <code>null</code>
     */
    public RideOffer getItem(int position) {
        if (this.mDataset == null)
            return null;

        RideOffer r = null;
        try {
            r = this.mDataset.get(position);
        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "[getItem] Erro ao obter local do dataset " + e);
        }

        return r;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_ride_card, parent, false);
        final ViewHolder vh = new ViewHolder(v);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.OnItemClick(vh.getPosition());
            }
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RideOffer offer = mDataset.get(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat(mContext.getString(R.string.date_format));
        SimpleDateFormat timeFormat = new SimpleDateFormat(mContext.getString(R.string.time_format));

        holder.mDriverNameTextView.setText(offer.getDriver().getName());
        holder.mDriverImageView.setImageBitmap(offer.getDriver().getProfileImage());
        holder.mStartingPointTextView.setText(mContext.getString(R.string.de) + ": " + offer.getStartingPoint().getTitulo());
        holder.mDestinationTextView.setText(mContext.getString(R.string.para) + ": " + offer.getDestination().getTitulo());
        holder.mDateTextView.setText(dateFormat.format(offer.getDatetime().getTime()));
        holder.mTimeTextView.setText(timeFormat.format(offer.getDatetime().getTime()));

        if (! offer.isActive()) {
            holder.mCancelledStampTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if (mDataset == null)
            return 0;

        return this.mDataset.size();
    }
}
