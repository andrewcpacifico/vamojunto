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


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Collections;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.helpers.FacebookHelper;
import co.vamojunto.model.Friendship;
import co.vamojunto.model.User;
import co.vamojunto.ui.activities.MainActivity;
import co.vamojunto.ui.adapters.FriendsRecyclerViewAdapter;
import co.vamojunto.util.NetworkUtil;
import co.vamojunto.util.UIUtil;

/**
 * A simple {@link Fragment}, where the user can manage his Facebook friends, and choose to follow
 * them.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 2.0
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
     * Define if fragment has loaded items to display once.
     *
     * @since 0.1.1
     */
    private boolean mHasLoaded;

    /**
     * Callback for Facebook login.
     *
     * @since 0.1.1
     */
    private CallbackManager mCallbackManager;

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
        mHasLoaded = false;

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        // setups the screen
        initComponents(rootView);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Method overridden to load friends just when the fragment is being displayed for the first
     * time.
     *
     * @param isVisibleToUser <code>true</code> if this fragment's UI is currently visible
     *                        to the user (default), <code>false</code> if it is not.
     * @since 0.1.1
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (this.isVisible() && isVisibleToUser && ! mHasLoaded) {
            // loads the currentUser's friends
            loadFriends();
        }
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
            @Override
            public void onClick(View v) {
                saveButtonOnclick();
            }
        });
    }

    /**
     * Called when the user clicks on the save button. The method persists the changes made by
     * the current user to the cloud database.
     *
     * @since 0.8.0
     */
    private void saveButtonOnclick() {
        List<User> added = mFriendsAdapter.getAdded();

        // save changes, only if there is any new friend to follow
        if (added.size() == 0) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.no_changing)
                    .content(R.string.no_changing_to_persist)
                    .build().show();

            return;
        }

        UIUtil.startLoading(getActivity());
        Friendship.follow(User.getCurrentUser(), added).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        UIUtil.stopLoading();

                        ((ManageFriendsFragment) getParentFragment()).reloadFriends();

                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.invite_fb_friends)
                                .content(R.string.fb_friends_saved)
                                .positiveText(R.string.invite_friends)
                                .negativeText(R.string.cancel)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);

                                        inviteFriendsButtonClick();
                                    }
                                }).build().show();
                    }
                });

                return null;
            }
        });
    }

    /**
     * Loads the user's Facebook friends, to display on the screen.
     *
     * @since 0.1.0
     */
    public void loadFriends() {
        mViewFlipper.setDisplayedChild(VIEW_PROGRESS);

        // no network connection, just display the error screen
        if (! NetworkUtil.isConnected(getActivity())) {
            displayErrorScreen(R.string.errormsg_no_internet_connection);
        } else if (
            ! AccessToken
                .getCurrentAccessToken()
                .getPermissions()
                .contains(FacebookHelper.Permissions.USER_FRIENDS)
        ) {
            LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    loadFriends();
                }

                @Override
                public void onCancel() {
                    displayErrorScreen(R.string.errormsg_cant_load_fb_friends);
                }

                @Override
                public void onError(FacebookException e) {
                    displayErrorScreen();
                }
            });

            // if user had not give permissions to read his friends o Facebook, asks for this permission
            LoginManager.getInstance()
                .logInWithReadPermissions(
                        getActivity(),
                        Collections.singletonList(FacebookHelper.Permissions.USER_FRIENDS)
                );
        } else {
            // after search for the user friends, sets the list of friends as the recyclerview dataset
            User.getCurrentUser().getFacebookFriends().continueWith(new Continuation<List<User>, Void>() {
                @Override
                public Void then(Task<List<User>> task) throws Exception {
                    // prevents error, on case that the user closes the fragment before the
                    // task finished
                    if (getActivity() != null && isAdded()) {
                        if (task.isFaulted() || task.isCancelled()) {
                            displayErrorScreen();
                        } else {
                            final List<User> lst = task.getResult();

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    displayFriendsList(lst);
                                    mHasLoaded = true;
                                }
                            });
                        }
                    }

                    return null;
                }
            });
        }
    }

    /**
     * Change the screen status, to display the list of facebook friends of current user. If there
     * is more than one user to display, a list of users are displayed, if not, display a message
     * and the option to user can invite his Facebook friends to try the app.
     *
     * @param userList The list of users to display on the screen.
     * @since 0.8.0
     */
    private void displayFriendsList(List<User> userList) {
        // checks if there is at least one user to display
        if (userList.size() >= 1) {
            mFriendsAdapter.setDataset(userList);

            // after the loading, switches the viewflipper to display the list to user
            mViewFlipper.setDisplayedChild(VIEW_DEFAULT);
        } else {
            displayErrorScreen(
                R.string.no_fb_friends,
                R.string.invite_fb_friends,
                R.drawable.ic_sad,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        inviteFriendsButtonClick();
                    }
                }
            );
        }
    }

    /**
     * Called when the user clicks on the invite Facebook friends button.
     *
     * @see FacebookHelper#inviteFriends(Activity)
     * @since 0.8.0
     */
    private void inviteFriendsButtonClick() {
        UIUtil.startLoading(getActivity(), getString(R.string.loading));

        FacebookHelper.inviteFriends(getActivity()).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                UIUtil.stopLoading();

                if (task.isCancelled() || task.isFaulted()) {
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.errormsg_invite_fb_friends),
                            Toast.LENGTH_LONG
                    ).show();
                }

                return null;
            }
        });
    }

    /**
     * Switches the viewFlipper to display the error screen, with the default message.
     *
     * @since 0.1.0
     */
    private void displayErrorScreen() {
        displayErrorScreen(R.string.errormsg_default);
    }

    /**
     * Switches the viewFlipper to display the error screen. and customizes the error message.
     *
     * @param errorMsgRes A string resource for the error message.
     * @since 0.1.0
     */
    private void displayErrorScreen(@StringRes int errorMsgRes) {
        mErrorScreenMsgTextView.setText(getString(errorMsgRes));

        mViewFlipper.setDisplayedChild(VIEW_ERROR);
    }

    private void displayErrorScreen(
            @StringRes int errorMsgRes,
            @StringRes int buttonMsgRes,
            @DrawableRes int iconRes,
            View.OnClickListener btnClickListener
    ) {
        mErrorScreenMsgTextView.setText(getString(errorMsgRes));
        mErrorScreenButton.setText(getString(buttonMsgRes));
        mErrorScreenButton.setOnClickListener(btnClickListener);
        mErrorScreenIcon.setImageResource(iconRes);

        mViewFlipper.setDisplayedChild(VIEW_ERROR);
    }

}
