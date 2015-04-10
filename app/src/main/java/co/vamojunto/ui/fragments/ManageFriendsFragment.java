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
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.Friendship;
import co.vamojunto.model.User;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A {@link android.support.v4.app.Fragment} where the user can manage the other users that he
 * follow.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 1.0.0
 */
public class ManageFriendsFragment extends Fragment {

    /**
     * RecyclerView to list the users followed by current user.
     *
     * @since 0.1.0
     */
    private RecyclerView mFriendsRecyclerView;

    /**
     * Adapter for the friends RecyclerView
     *
     * @since 0.1.0
     */
    private FriendsRecyclerViewAdapter mFriendsAdapter;

    /**
     * LayoutManager for the friends RecyclerView
     *
     * @since 0.1.0
     */
    private LinearLayoutManager mFriendsLayoutManager;

    /**
     * A {@link android.os.Handler} to run code on the main thread.
     *
     * @since 0.1.0
     */
    private Handler mHandler;

    /**
     * Required default constructor
     *
     * @since 0.1.0
     */
    public ManageFriendsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manage_friends, container, false);

        mHandler = new Handler();

        // setups the screen
        initComponents(rootView);

        // loads the currentUser's friends
        loadFriends();

        return rootView;
    }

    /**
     * Setups the screen components
     *
     * @param rootView The inflated layout view.
     * @since 0.1.0
     */
    public void initComponents(View rootView) {
        mFriendsAdapter = new FriendsRecyclerViewAdapter(getActivity());

        mFriendsLayoutManager = new LinearLayoutManager(getActivity());

        // inflates the friendsRecyclerView and defines its layoutManager and adapter
        mFriendsRecyclerView = (RecyclerView) rootView.findViewById(R.id.friends_recycler_view);
        mFriendsRecyclerView.setLayoutManager(mFriendsLayoutManager);
        mFriendsRecyclerView.setAdapter(mFriendsAdapter);
        mFriendsRecyclerView.setHasFixedSize(true);
    }

    /**
     * Loads all users followed by the current user. This data are stored on the local datastore.
     *
     * @since 0.1.0
     */
    public void loadFriends() {
        // search for users that is being followed by current user, and defines this list of users
        // as the recyclerView dataset
        Friendship.getFollowedByUserFromLocal(User.getCurrentUser())
                .continueWith(new Continuation<List<User>, Void>() {
                    @Override
                    public Void then(Task<List<User>> task) throws Exception {
                        List<User> lst = task.getResult();
                        mFriendsAdapter.setDataset(lst);

                        return null;
                    }
                });
    }

    /**
     * Adapter for the RecyclerViews where the user can see its friends. On this RecyclerView there
     * is two different item layouts, the default layout displays a row with a user's friend data,
     * the other layout is used as a header for the two list sections:
     * <ul>
     *     <li>The users already followed by the current user</li>
     *     <li>The Facebook friends of the user, that aren't being followed yet.</li>
     * </ul>
     *
     * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
     * @version 1.0.0
     * @since 0.1.0
     */
    public static class FriendsRecyclerViewAdapter
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

            // views for view_header layout
            public TextView mHeaderTextView;

            /**
             * Constructor, inflates the holder views based on a given viewType to define which
             * layout was used to inflate the holder.
             *
             * @param itemView The holder's inflated layout.
             * @param viewType Defines the type of the holder.
             * @since 0.1.0
             */
            public ViewHolder(View itemView, int viewType) {
                super(itemView);

                holderType = viewType;

                // if the holder is a view_friend, i.e a recyclerview.manage_friends_item layout
                if (viewType == VIEW_FRIEND) {
                    mFriendName = (TextView) itemView.findViewById(R.id.friend_name);
                    mFriendPicture = (CircleImageView) itemView.findViewById(R.id.friend_picture);

                // if the holder is a view_header, i.e a recyclerview.manage_friends_header layout
                } else {
                    mHeaderTextView = (TextView) itemView.findViewById(R.id.header_text);
                }
            }
        }

        public FriendsRecyclerViewAdapter(Context context) {
            mHandler = new Handler();
            mContext = context;
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

            holder = new ViewHolder(v, viewType);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (holder.holderType == VIEW_FRIEND) {
                User u = mDataset.get(position - 1);

                holder.mFriendName.setText(u.getName());
                holder.mFriendPicture.setImageBitmap(u.getProfileImage());
            } else {
                if (position == 0)
                    holder.mHeaderTextView.setText(mContext.getString(R.string.followed_friends));
                else
                    holder.mHeaderTextView.setText(mContext.getString(R.string.facebook_friends));
            }
        }

        @Override
        public int getItemCount() {
            if (mDataset == null)
                return 1;

            return mDataset.size() + 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0 || (mDataset != null && position == (mDataset.size() + 1)))
                return VIEW_HEADER;
            else
                return VIEW_FRIEND;
        }
    }
}
