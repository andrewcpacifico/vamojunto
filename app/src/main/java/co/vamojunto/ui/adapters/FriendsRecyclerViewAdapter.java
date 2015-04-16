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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.vamojunto.R;
import co.vamojunto.model.User;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adapter for the RecyclerViews where the user can see its friends and manage (follow/unfollow)
 * them. This Adapter is used by the {@link android.support.v7.widget.RecyclerView} on the
 * {@link co.vamojunto.ui.fragments.ManageFollowedFragment}
 * and {@link co.vamojunto.ui.fragments.ManageFbFriendsFragment}.
 *
 * On the ManageFollowedFragment the adapter displays all users checked, so the current user can
 * uncheck all users that he wants to unfollow. On the ManageFbFriendsFragment, the adapter displays
 * all users unchecked, so the current user can check all the users that he wants to follow.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class FriendsRecyclerViewAdapter
        extends RecyclerView.Adapter<FriendsRecyclerViewAdapter.ViewHolder> {

    /**
     * Code indicating a viewType for a header row
     *
     * @since 0.1.0
     */
    private static final int VIEW_HEADER = 0;

    /**
     * Code indicating a viewType for a default row, displaying a friend.
     *
     * @since 0.1.0
     */
    private static final int VIEW_FRIEND = 1;

    /**
     * Dataset containing the users to display on the recyclerView.
     *
     * @since 0.1.0
     */
    private List<User> mDataset;

    /**
     * A list containing all friends unfollowed by user.
     *
     * @since 0.1.0
     */
    private List<User> mRemovedFriends;

    /**
     * A {@link android.os.Handler} to run code on the main thread.
     *
     * @since 0.1.0
     */
    private Handler mHandler;

    /**
     * Current context, will be used to access resources.
     *
     * @since 0.1.0
     */
    private Context mContext;

    /**
     * Listener for clicks on recyclerView items
     *
     * @since 0.1.0
     */
    private OnItemClickListener mClickListener;

    /**
     * Defines if the checkboxes have to be checked or not.
     *
     * @since 0.1.0
     */
    private boolean mChecked;

    /**
     * The header title
     *
     * @since 0.1.0
     */
    private String mHeaderTitle;

    /**
     * ViewHolder for items on the ManageFriends RecyclerView.
     *
     * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
     * @version 1.0.0
     * @since 0.1.0
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        /**
         * The type of the holder, used to define which views that have to be inflated.
         *
         * @since 0.1.0
         */
        public int holderType;

        // views for view_friend layout
        public TextView mFriendName;
        public CircleImageView mFriendPicture;
        public CheckBox mFollowCheckBox;

        // views for view_header layout
        public TextView mHeaderTextView;

        /**
         * Constructor, inflates the holder views based on a given viewType to define which
         * layout was used to inflate the holder.
         *
         * @param itemView The holder's inflated layout.
         * @param viewType Defines the type of the holder.
         * @param clickListener Listener for clicks on item, if the viewType is VIEW_FRIEND,
         *                      or <code>null</code> if there is no action for this item.
         * @since 0.1.0
         */
        public ViewHolder(final View itemView, int viewType, final OnItemClickListener clickListener) {
            super(itemView);

            holderType = viewType;

            // if the holder is a view_friend, i.e a recyclerview.manage_friends_item layout
            if (viewType == VIEW_FRIEND) {
                mFriendName = (TextView) itemView.findViewById(R.id.friend_name);
                mFriendPicture = (CircleImageView) itemView.findViewById(R.id.friend_picture);
                mFollowCheckBox = (CheckBox) itemView.findViewById(R.id.follow_checkbox);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (clickListener != null)
                            clickListener.onClick(ViewHolder.this);
                    }
                });

                mFollowCheckBox.setClickable(false);

                // if the holder is a view_header, i.e a recyclerview.manage_friends_header layout
            } else {
                mHeaderTextView = (TextView) itemView.findViewById(R.id.header_text);
            }
        }
    }

    /**
     * Interface to handle the clicks on recyclerView items
     *
     * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
     * @since 0.1.0
     */
    public static interface OnItemClickListener {
        public void onClick(ViewHolder holder);
    }

    /**
     * Adapter constructor to initialize some fields.
     *
     * @param context Current context.
     * @param checked Defines the statuses of the checkboxes.
     * @param headerTitle The header title.
     * @param clickListener Listener for clicks on recyclerView items.
     * @since 0.1.0
     */
    public FriendsRecyclerViewAdapter(
        Context context,
        boolean checked,
        String headerTitle,
        OnItemClickListener clickListener
    ) {
        mHeaderTitle = headerTitle;
        mChecked = checked;
        mHandler = new Handler();
        mContext = context;
        mClickListener = clickListener;
        mRemovedFriends = new ArrayList<>();
    }

    /**
     * Switches the following status of the user on a given position, i.e. if the user on that
     * position is being followed, he won't be anymore, and if he is not being followed, he
     * will starts to be.
     *
     * @param holder The ViewHolder clicked.
     * @since 0.1.0
     */
    public void toggleFollow(ViewHolder holder) {
        // if the user is removing a friend
        if (holder.mFollowCheckBox.isChecked()) {
            // adds the clicked user to unfollowed list, the position on dataset is the position
            // of the clicked holder minus one, because of the header
            mRemovedFriends.add(mDataset.get(holder.getPosition() - 1));
        }

        holder.mFollowCheckBox.setChecked(!holder.mFollowCheckBox.isChecked());
    }

    /**
     * Returns the list of the users unfollowed by the user.
     *
     * @return An {@link java.util.ArrayList} containing all users that current user wants to
     *         unfollow.
     * @since 0.1.0
     */
    public List<User> getUnfollowed() {
        return mRemovedFriends;
    }

    /**
     * Changes the recyclerView dataset, and notifies all listeners;
     *
     * @param dataset The new dataset
     * @since 0.1.0
     */
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = null;
        View v = null;

        // inflates the layout for view_friend viewType
        if (viewType == VIEW_FRIEND) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_manage_friends_item, parent, false);

            // inflates the layout for view_header viewType
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_manage_friends_header, parent, false);
        }

        holder = new ViewHolder(v, viewType, mClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder.holderType == VIEW_FRIEND) {
            User u = mDataset.get(position - 1);

            holder.mFriendName.setText(u.getName());
            holder.mFriendPicture.setImageBitmap(u.getProfileImage());
            holder.mFollowCheckBox.setChecked(mChecked);
        } else {
            if (position == 0)
                holder.mHeaderTextView.setText(mHeaderTitle);
        }
    }

    @Override
    public int getItemCount() {
        if (mDataset == null)
            return 1;

        return mDataset.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VIEW_HEADER;
        else
            return VIEW_FRIEND;
    }
}
