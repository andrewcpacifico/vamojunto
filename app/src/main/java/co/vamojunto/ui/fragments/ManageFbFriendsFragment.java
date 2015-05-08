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


import android.app.ProgressDialog;
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
import co.vamojunto.util.NetworkUtil;

/**
 * A simple {@link Fragment}, where the user can manage his Facebook friends, and choose to follow
 * them.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 1.0.0
 */
public class ManageFbFriendsFragment extends Fragment {


    private static final String TAG = "ManageFbFriendsFragment";

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
     * ViewFlipper used to alternate between the ProgressBar, that is displayed when the friends
     * are loading, the error screen displayed when any error occurs, and the main screen with
     * the friends list.
     *
     * @since 0.1.0
     */
    private ViewFlipper mViewFlipper;

    /**
     * A {@link android.os.Handler} to run code on the main thread.
     *
     * @since 0.1.0
     */
    private Handler mHandler;

    /**
     * TextView containing message on the error screen.
     *
     * @since 0.1.0
     */
    private TextView mErrorScreenMsgTextView;

    /**
     * Inflated button on the error screen.
     *
     * @since 0.1.0
     */
    private Button mErrorScreenButton;

    /**
     * Inflated icon on the error screen.
     *
     * @since 0.1.0
     */
    private ImageView mErrorScreenIcon;

    /**
     * A progress dialog, displayed when any data is being loaded.
     *
     * @since 0.1.0
     */
    private ProgressDialog mProDialog;

    /**
     * Required default constructor
     *
     * @since 0.1.0
     */
    public ManageFbFriendsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manage_fb_friends, container, false);

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
            false,
            getString(R.string.follow_fb_friends),
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

        // inflate the error screen widgets
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
                List<User> added = mFriendsAdapter.getAdded();

                // save changes, only if there is any new friend to follow
                if (added.size() > 0) {
                    startLoading();

                    Friendship.follow(User.getCurrentUser(), added)
                            .continueWith(new Continuation<Void, Void>() {
                                @Override
                                public Void then(Task<Void> task) throws Exception {
                                    stopLoading();

                                    // code to navigate up to MainActivity
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
     * Loads the user's Facebook friends, to display on the screen.
     *
     * @since 0.1.0
     */
    public void loadFriends() {
        if (! NetworkUtil.isConnected(getActivity())) {
            displayErrorScreen(getString(R.string.errormsg_no_internet_connection));
        } else {
            // after search for the user friends, sets the list of friends as the recyclerview dataset
            User.getCurrentUser().getFacebookFriends()
                    .continueWith(new Continuation<List<User>, Void>() {
                        @Override
                        public Void then(Task<List<User>> task) throws Exception {
                            // prevents error, on case that the user closes the fragment before the
                            // task finished
                            if (getActivity() != null && isAdded()) {
                                if (task.isFaulted() || task.isCancelled()) {
                                    displayErrorScreen();
                                } else {
                                    List<User> lst = task.getResult();

                                    // checks if there is at least one user to display
                                    if (lst.size() >= 1) {
                                        mFriendsAdapter.setDataset(lst);

                                        // after the loading, switches the viewflipper to display the list to user
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mViewFlipper.setDisplayedChild(VIEW_DEFAULT);
                                            }
                                        });
                                    } else {
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mErrorScreenMsgTextView
                                                        .setText(getString(R.string.no_fb_friends));
                                                mErrorScreenButton.setVisibility(View.GONE);
                                                mErrorScreenIcon.setImageResource(R.drawable.ic_sad);

                                                mViewFlipper.setDisplayedChild(VIEW_ERROR);
                                            }
                                        });
                                    }
                                }
                            }

                            return null;
                        }
                    });
        }
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

    /**
     * Shows a dialog indicating that the main screen is bein loaded.
     *
     * @since 0.1.0
     */
    private void startLoading() {
        mProDialog = new ProgressDialog(getActivity());
        mProDialog.setMessage(getString(R.string.saving));
        mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProDialog.setCancelable(false);
        mProDialog.show();
    }

    /**
     * Finishes the loading dialog;
     *
     * @since 0.1.0
     */
    private void stopLoading() {
        mProDialog.dismiss();
        mProDialog = null;
    }

}
