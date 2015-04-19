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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.w3c.dom.Text;

import co.vamojunto.R;
import co.vamojunto.model.RideRequest;
import co.vamojunto.ui.activities.MainActivity;
import co.vamojunto.ui.activities.RequestDetailsActivity;
import co.vamojunto.util.DateUtil;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A {@link android.support.v4.app.Fragment} where the user can view the details of a ride request
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class RequestDetailsFragment extends Fragment {

    /**
     * The request to display the details.
     *
     * @since 0.1.0
     */
    private RideRequest mRequest;

    /**
     * Inflated ImageView with the requester profile image
     *
     * @since 0.1.0
     */
    private CircleImageView mRequesterImage;

    /**
     * Inflated TextView with the requester name.
     *
     * @since 0.1.0
     */
    private TextView mRequesterNameTextView;

    /**
     * Inflated TextView with the request starting point.
     *
     * @since 0.1.0
     */
    private TextView mStartingPointTextView;

    /**
     * Inflated TextView with the request destination point.
     *
     * @since 0.1.0
     */
    private TextView mDestinationTextView;

    /**
     * Inflated TextView with the request date and time.
     *
     * @since 0.1.0
     */
    private TextView mDatetimeTextView;

    /**
     * Inflated TextView with the request details.
     *
     * @since 0.1.0
     */
    private TextView mDetailsTextView;

    /**
     * Inflated EditText with the message to send to requester.
     *
     * @since 0.1.0
     */
    private EditText mMessageEditText;

    /**
     * Inflated ImageButton used to send the message to requester.
     *
     * @since 0.1.0
     */
    private ImageButton mSendButton;

    public RequestDetailsFragment() { /* required default constructor, do not delete or edit this */ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_request_details, container, false);

        // hides the keyboard by default
        getActivity()
                .getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mRequest = RideRequest.getStoredInstance(RequestDetailsActivity.EXTRA_REQUEST);

        initComponents(rootView);

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handles action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            getActivity().finish();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Initializes screen components.
     *
     * @param rootView Inflated layout.
     * @since 0.1.0
     */
    private void initComponents(View rootView) {
        mRequesterImage = (CircleImageView) rootView.findViewById(R.id.requester_picture);
        mRequesterImage.setImageBitmap(mRequest.getRequester().getProfileImage());

        mRequesterNameTextView = (TextView) rootView.findViewById(R.id.requester_name_text_view);
        mRequesterNameTextView.setText(mRequest.getRequester().getName());

        mStartingPointTextView = (TextView) rootView.findViewById(R.id.starting_point_text_view);
        mStartingPointTextView.setText(mRequest.getStartingPoint().getTitulo());

        mDestinationTextView = (TextView) rootView.findViewById(R.id.destination_text_view);
        mDestinationTextView.setText(mRequest.getDestination().getTitulo());

        mDatetimeTextView = (TextView) rootView.findViewById(R.id.datetime_text_view);
        mDatetimeTextView.setText(DateUtil.getFormattedDateTime(getActivity(), mRequest.getDatetime()));

        mDetailsTextView = (TextView) rootView.findViewById(R.id.details_text_view);
        mDetailsTextView.setText(getString(R.string.details) + ": " + mRequest.getDetails());
    }
}
