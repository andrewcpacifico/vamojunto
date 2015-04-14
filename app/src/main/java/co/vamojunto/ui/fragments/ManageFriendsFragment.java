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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.helpers.FacebookHelper;
import co.vamojunto.model.Friendship;
import co.vamojunto.model.User;
import co.vamojunto.util.Globals;
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

    private static final String TAG = "ManageFriendsFragment";
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
     * On Fragment Stop, persists the changes made by the current user to the cloud database.
     *
     * @since 0.1.0
     */
    @Override
    public void onStop() {
        super.onStop();

        Friendship.unfollow(User.getCurrentUser(), mFriendsAdapter.getUnfollowed());
    }

    /**
     * Setups the screen components
     *
     * @param rootView The inflated layout view.
     * @since 0.1.0
     */
    public void initComponents(View rootView) {
        mFriendsAdapter = new FriendsRecyclerViewAdapter(
            getActivity(),
            new FriendsRecyclerViewAdapter.OnItemClickListener() {
                @Override
                public void onClick(FriendsRecyclerViewAdapter.ViewHolder holder) {
                    mFriendsAdapter.toggleFollow(holder);
                }
            }
        );

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
        FacebookHelper.getUserFriendsAsync(User.getCurrentUser());

        // get the default preferences for app
        final SharedPreferences settings = getActivity().getSharedPreferences(
                Globals.DEFAULT_PREF_NAME,
                Context.MODE_PRIVATE
        );

        // gets the preference that defines if the current user friends have been already fetched
        // from the cloud database
        final boolean fetchedFromCloud =
                settings.getBoolean(Globals.FETCHED_FRIENDS_PREF_KEY, false);

        // search for users that is being followed by current user, if the user's friends have
        // already been fetched, searches on the local database
        Task<List<User>> tskGetFollowed = (fetchedFromCloud)
                ? Friendship.getFollowedByUserFromLocal(User.getCurrentUser())
                : Friendship.getFollowedByUser(User.getCurrentUser());

        // after search for the user friends, sets the list of friends as the recyclerview dataset
        tskGetFollowed.continueWith(new Continuation<List<User>, Void>() {
            @Override
            public Void then(Task<List<User>> task) throws Exception {
                List<User> lst = task.getResult();
                mFriendsAdapter.setDataset(lst);

                // if it is the first time that current user friends have been fetched
                if (! fetchedFromCloud) {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(Globals.FETCHED_FRIENDS_PREF_KEY, true);
                    editor.commit();
                }

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
         * @param clickListener Listener for clicks on recyclerView items.
         * @since 0.1.0
         */
        public FriendsRecyclerViewAdapter(Context context, OnItemClickListener clickListener) {
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
                holder.mFollowCheckBox.setChecked(true);
            } else {
                if (position == 0)
                    holder.mHeaderTextView.setText(mContext.getString(R.string.followed_friends));
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
}
