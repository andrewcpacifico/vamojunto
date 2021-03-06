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

package co.vamojunto.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;

import java.util.Arrays;
import java.util.List;

import co.vamojunto.R;
import co.vamojunto.helpers.FacebookHelper;
import co.vamojunto.ui.fragments.ManageFollowedFragment;
import co.vamojunto.ui.fragments.ManageFriendsFragment;

/**
 * Activity where the user can manage his friends on the app.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 */
public class ManageFriendsActivity extends VamoJuntoActivity {

    public static final String TAG = "ManageFriendsActivity";
    public static final String EXTRA_INITIAL_TAB = "initial_tab";

    public static final int TAB_FOLLOW = 0;
    public static final int TAB_FACEBOOK = 1;

    @Override
    protected void onCreated(Bundle savedInstanceState) {
        super.onCreated(savedInstanceState);

        ManageFriendsFragment fragment = new ManageFriendsFragment();
        fragment.setArguments(getIntent().getExtras());
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }

        setupAppBar();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_manage_friends;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult");
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null) {
            for (Fragment fragment: fragmentList) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    /**
     * Initializes the activity's App Bar
     */
    private void setupAppBar() {
        // remove elevation from app bar because of view pager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAppBar.setElevation(0);
        }

        mAppBar.setTitle(R.string.title_activity_manage_friends);
        mAppBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateBack();
            }
        });
    }

    /**
     * Do the back navigation.
     */
    private void navigateBack() {
        if (isTaskRoot()) {
            // code to navigate up to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_INITIAL_VIEW, MainActivity.VIEW_FRIENDS_FEED);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
            finish();
        } else {
            finish();
        }
    }

    /**
     * Overriding the onBackPressed method, to make an "up" navigation when this activity is
     * opened from a notification.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        navigateBack();
    }

}
