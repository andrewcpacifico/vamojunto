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
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import co.vamojunto.R;
import co.vamojunto.ui.activities.ManageFriendsActivity;
import co.vamojunto.util.Globals;
import co.vamojunto.util.UIUtil;

/**
 * Fragment to display the feed from user friends. All data posted by the user friends have to be
 * displayed, the rides offered and requested.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 2.0
 */
public class FriendsFeedFragment extends AbstractFeedFragment implements MenuItem.OnMenuItemClickListener {

    private static final String TAG = "MyFriendsFeedFragment";

    public FriendsFeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided
     * parameters. Avoid to use the class constructor.
     *
     * @return A new instance of fragment FriendsFeedFragment.
     * @since 0.6.0
     */
    public static FriendsFeedFragment newInstance() {
        return new FriendsFeedFragment();
    }

    @Override
    protected void setupAppBar() {
        super.setupAppBar();

        Toolbar appBar = getAppBar();
        appBar.setTitle(getString(R.string.my_friends_feed));

        appBar.inflateMenu(R.menu.menu_my_friends_feed);
        appBar.getMenu().findItem(R.id.action_manage_friends).setOnMenuItemClickListener(this);

        // get the default preferences for app
        final SharedPreferences settings = getActivity().getSharedPreferences(
            Globals.DEFAULT_PREF_NAME,
            Context.MODE_PRIVATE
        );

        // check if it is the first exhibition of this feed, if it is true, display the screen
        // tooltips for the user
        boolean firstTime = settings.getBoolean(TAG + "firstTime", true);
        if (firstTime) {
            UIUtil.showCase(
                getActivity(),
                getString(R.string.tooltiptitle_manage_friends),
                getString(R.string.tooltipmsg_manage_friends),
                appBar.findViewById(R.id.action_manage_friends)
            );

            // defines an editor to preferences
            final SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(TAG + "firstTime", false);
            editor.apply();
        }
    }

    @Override
    protected AbstractListRideOffersFragment getListOffersFragment() {
        return FriendsOffersFragment.newInstance();
    }

    @Override
    protected AbstractListRideRequestsFragment getListRequestsFragment() {
        return FriendsRequestsFragment.newInstance();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_manage_friends) {
            Intent intent = new Intent(getActivity(), ManageFriendsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onMenuItemClick(item);
    }
}
