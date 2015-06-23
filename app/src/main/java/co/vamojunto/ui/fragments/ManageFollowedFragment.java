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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.Friendship;
import co.vamojunto.model.User;
import co.vamojunto.ui.activities.MainActivity;
import co.vamojunto.ui.adapters.FriendsRecyclerViewAdapter;
import co.vamojunto.util.Globals;
import co.vamojunto.util.NetworkUtil;
import co.vamojunto.util.UIUtil;

/**
 * A {@link android.support.v4.app.Fragment} where the user can manage the other users that he
 * follow.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 1.0.0
 */
public class ManageFollowedFragment extends Fragment {

    private static final String TAG = "ManageFriendsFragment";

    // the constants below are used to identify the views loaded by the ViewFlipper
    private static final int VIEW_PROGRESS = 0;
    private static final int VIEW_ERROR = 1;
    private static final int VIEW_DEFAULT = 2;

    /**
     * Adapter for the friends RecyclerView
     *
     * @since 0.1.0
     */
    private FriendsRecyclerViewAdapter mFriendsAdapter;

    /**
     * A {@link android.os.Handler} to run code on the main thread.
     *
     * @since 0.1.0
     */
    private Handler mHandler;

    /**
     * ViewFlipper used to alternate between the ProgressBar, that is displayed when the friends
     * are loading, the error screen displayed when any error occurs, and the main screen with
     * the friends list.
     *
     * @since 0.1.0
     */
    private ViewFlipper mViewFlipper;

    /**
     * TextView containing message on the error screen.
     *
     * @since 0.1.0
     */
    private TextView mErrorScreenMsgTextView;

    /**
     * Button displayed on error screen.
     *
     * @since 0.1.0
     */
    private Button mErrorScreenButton;

    /**
     * Icon displayed on error screen.
     *
     * @since 0.1.0
     */
    private ImageView mErrorScreenIcon;

    /**
     * Required default constructor
     *
     * @since 0.1.0
     */
    public ManageFollowedFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manage_followed, container, false);

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
        mFriendsAdapter = new FriendsRecyclerViewAdapter(
            getActivity(),
            true,
            getString(R.string.followed_friends),
            new FriendsRecyclerViewAdapter.OnItemClickListener() {
                @Override
                public void onClick(FriendsRecyclerViewAdapter.ViewHolder holder) {
                    mFriendsAdapter.toggleFollow(holder);
                }
            }
        );


        LinearLayoutManager friendsLayoutManager = new LinearLayoutManager(getActivity());

        // inflates the friendsRecyclerView and defines its layoutManager and adapter
        RecyclerView friendsRecyclerView = (RecyclerView) rootView.findViewById(R.id.friends_recycler_view);
        friendsRecyclerView.setLayoutManager(friendsLayoutManager);
        friendsRecyclerView.setAdapter(mFriendsAdapter);
        friendsRecyclerView.setHasFixedSize(true);

        mErrorScreenMsgTextView =
                (TextView) rootView.findViewById(R.id.error_screen_message_text_view);

        mErrorScreenIcon = (ImageView) rootView.findViewById(R.id.error_screen_message_icon);

        mErrorScreenButton = (Button) rootView.findViewById(R.id.error_screen_retry_button);
        mErrorScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFriends();
            }
        });

        mViewFlipper = (ViewFlipper) rootView.findViewById(R.id.flipper);

        Button saveButton = (Button) rootView.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            // on button click, persists the changes made by the current user to the cloud database.
            @Override
            public void onClick(View v) {
                List<User> removed = mFriendsAdapter.getUnfollowed();

                // save changes, only if there is any new friend to follow
                if (removed.size() > 0) {
                    UIUtil.startLoading(getActivity(), getString(R.string.loading));

                    Friendship.unfollow(User.getCurrentUser(), mFriendsAdapter.getUnfollowed())
                            .continueWith(new Continuation<Void, Void>() {
                                @Override
                                public Void then(Task<Void> task) throws Exception {
                                    UIUtil.stopLoading();

                                    // after the task conclusion, restarts the friends feed,
                                    // so the user can view the new friends on feed
                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                    intent.putExtra(MainActivity.EXTRA_INITIAL_VIEW, MainActivity.VIEW_FRIENDS_FEED);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                    startActivity(intent);
                                    getActivity().finish();

                                    return null;
                                }
                            });
                } else {
                    getActivity().finish();
                }
            }
        });
    }

    /**
     * Loads all users followed by the current user. This data are stored on the local datastore.
     *
     * @since 0.1.0
     */
    public void loadFriends() {
        mViewFlipper.setDisplayedChild(VIEW_PROGRESS);

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

        // if the user have to fetch friends from web, and is not connected to internet
        // just displays an error message
        if (! fetchedFromCloud && ! NetworkUtil.isConnected(getActivity())) {
            displayErrorScreen(getString(R.string.errormsg_no_internet_connection));
        } else {
            // after search for the user friends, sets the list of friends as the recyclerview dataset
            tskGetFollowed.continueWith(new Continuation<List<User>, Void>() {
                @Override
                public Void then(Task<List<User>> task) throws Exception {
                    // check if Fragment is attached to some Activity, before change the view
                    if (getActivity() != null && isAdded()) {
                        if (task.isCancelled() || task.isFaulted()) {
                            displayErrorScreen();
                            if (task.isFaulted()) {
                                Log.e(TAG, "Error on fetch user friends.", task.getError());
                            }
                        } else {
                            List<User> lst = task.getResult();
                            mFriendsAdapter.setDataset(lst);

                            if (lst.size() == 0) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        displayNoFriends();
                                    }
                                });
                            } else {
                                // if it is the first time that current user friends have been fetched
                                if (!fetchedFromCloud) {
                                    SharedPreferences.Editor editor = settings.edit();
                                    editor.putBoolean(Globals.FETCHED_FRIENDS_PREF_KEY, true);
                                    editor.apply();
                                }

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mViewFlipper.setDisplayedChild(VIEW_DEFAULT);
                                    }
                                });
                            }
                        }
                    } else {
                        Log.e(TAG, "Fragment detached from Activity");
                    }

                    return null;
                }
            });
        }
    }

    /**
     * Displays a message to user, when he have no following friends.
     *
     * @since 0.1.0
     */
    private void displayNoFriends() {
        displayErrorScreen(getString(R.string.no_following_message));
        mErrorScreenButton.setVisibility(View.GONE);
        mErrorScreenIcon.setImageResource(R.drawable.ic_sad);
    }

    /**
     * Switches the viewFlipper to display the error screen, with the default message.
     *
     * @since 0.1.0
     */
    private void displayErrorScreen() {
        displayErrorScreen(getString(R.string.errormsg_default));
    }

    /**
     * Switches the viewFlipper to display the error screen. and customizes the error message.
     *
     * @param errorMsg The message displayed on the screen.
     * @since 0.1.0
     */
    private void displayErrorScreen(String errorMsg) {
        mErrorScreenMsgTextView.setText(errorMsg);

        mViewFlipper.setDisplayedChild(VIEW_ERROR);
    }

}
