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

import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.Ride;
import co.vamojunto.model.User;
import co.vamojunto.util.DateUtil;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Activity where the user can view the details of a specific ride.
 *
 * @author Andrew C. Pacifico <andrewpcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class RideDetailsActivity extends ActionBarActivity {

    public static final String TAG = "RideDetailsActivity";

    // constants used to define the input extras names
    public static final String EXTRA_RIDE = "ride";

    /**
     * The Activity toolbar
     */
    private Toolbar mToolbarActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_details);

        PlaceholderFragment fragment = new PlaceholderFragment();
        fragment.setArguments(getIntent().getExtras());
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }

        setupAppBar();
    }

    /**
     * Initializes the activity's App Bar
     */
    private void setupAppBar() {
        mToolbarActionBar = (Toolbar) findViewById(R.id.tool_bar);

        setSupportActionBar(mToolbarActionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        /**
         * The ride to be displayed on the screen
         */
        private Ride mRide;

        /**
         * The edit ride menu item
         */
        private MenuItem mEditMenu;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_ride_details, container, false);

            Bundle arguments = getArguments();
            mRide = arguments.getParcelable(RideDetailsActivity.EXTRA_RIDE);

            mRide.getPassengers().continueWith(new Continuation<List<User>, Object>() {
                @Override
                public Object then(Task<List<User>> task) throws Exception {
                    List<User> lst = task.getResult();

                    return null;
                }
            });

            initComponents(rootView);

            setHasOptionsMenu(true);

            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.menu_ride_details, menu);

            // enables the edit menu only if the user is the driver of this ride
            mEditMenu = menu.findItem(R.id.action_edit);
            if (! mRide.getDriver().equals(User.getCurrentUser())) {
                mEditMenu.setEnabled(false);
                mEditMenu.setVisible(false);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // handles action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            if (id == android.R.id.home) {
                getActivity().finish();
            } else if (id == R.id.action_edit) {
                Toast.makeText(getActivity(), "Calma aí arrombado, chegou agora e já quer tirar onda?"
                    + "Deixa de ser folgado ... Se liga!", Toast.LENGTH_LONG).show();
            }

            return super.onOptionsItemSelected(item);
        }

        /**
         * Initializes the screen
         *
         * @param rootView The Fragment's inflated layout.
         */
        private void initComponents(View rootView) {
            CircleImageView driverImageView = (CircleImageView) rootView.findViewById(R.id.driver_picture);
            driverImageView.setImageBitmap(mRide.getDriver().getProfileImage());

            TextView driverNameTextView = (TextView) rootView.findViewById(R.id.driver_name_text_view);
            driverNameTextView.setText(mRide.getDriver().getName());

            TextView startingPointTextView = (TextView) rootView.findViewById(R.id.starting_point_text_view);
            startingPointTextView.setText(getString(R.string.from) + ": " +
                    mRide.getStartingPoint().getTitulo());

            TextView destinationTextView = (TextView) rootView.findViewById(R.id.destination_text_view);
            destinationTextView.setText(getString(R.string.to) + ": " +
                    mRide.getDestination().getTitulo());

            TextView datetimeTextView = (TextView) rootView.findViewById(R.id.datetime_text_view);
            datetimeTextView.setText(getString(R.string.when) + ": " +
                    DateUtil.getFormattedDateTime(getActivity(), mRide.getDatetime()));

            TextView detailsTextView = (TextView) rootView.findViewById(R.id.detais_text_view);
            detailsTextView.setText(getString(R.string.details) + ": " + mRide.getDetails());

            // enables the request a ride button, only if the user is not the driver of this ride
            Button askButton = (Button) rootView.findViewById(R.id.ask_button);
            if (mRide.getDriver().equals(User.getCurrentUser())) {
                askButton.setVisibility(View.GONE);
            }
        }
    }
}

