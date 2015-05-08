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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import co.vamojunto.R;

/**
 * A simple {@link Fragment} subclass.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 1.0.0
 */
public class FilterFeedFragment extends Fragment {

    /**
     * Inflated EditText with the destination filter value.
     *
     * @since 0.1.0
     */
    private EditText mDestinationEditText;

    /**
     * Inflated EditText with the starting point filter value.
     *
     * @since 0.1.0
     */
    private EditText mStartingPointEditText;

    /**
     * The feed fragment to filter the results.
     *
     * @since 0.1.0
     */
    private FilterableFeedFragment mFeedFragment;

    public FilterFeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_filter_feed, container, false);

        initComponents(rootView);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // force to show the keyboard
        getActivity().getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void initComponents(View rootView) {
        TextView.OnEditorActionListener actionListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performFilter();
                    return true;
                }
                return false;
            }
        } ;

        mDestinationEditText = (EditText) rootView.findViewById(R.id.destination_edit_text);
        mDestinationEditText.setOnEditorActionListener(actionListener);

        mStartingPointEditText = (EditText) rootView.findViewById(R.id.starting_point_edit_text);
        mStartingPointEditText.setOnEditorActionListener(actionListener);

        ImageButton closeButton = (ImageButton) rootView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

        ImageButton okButton = (ImageButton) rootView.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performFilter();
            }
        });

        ImageButton changeButton = (ImageButton) rootView.findViewById(R.id.change_button);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buffer = String.valueOf(mDestinationEditText.getText());

                mDestinationEditText.setText(mStartingPointEditText.getText());
                mStartingPointEditText.setText(buffer);
            }
        });
    }

    /**
     * Perform the search
     *
     * @since 0.1.0
     */
    private void performFilter() {
        close();

        Bundle filterValues = new Bundle();
        filterValues.putString(
                FilterableFeedFragment.STARTING_POINT,
                mStartingPointEditText.getText().toString()
        );

        filterValues.putString(
                FilterableFeedFragment.DESTINATION,
                mDestinationEditText.getText().toString()
        );

        mFeedFragment.onFeedFilter(filterValues);
    }

    /**
     * Dismisses the fragment.
     *
     * @since 0.1.0
     */
    private void close() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mDestinationEditText.getWindowToken(), 0);

        FragmentManager manager = getFragmentManager();
        manager
            .beginTransaction()
            .remove(FilterFeedFragment.this)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit();
        manager.popBackStack();
    }

    /**
     * Commit a fragment transaction, to inflate this fragment on activity top.
     *
     * @param feedFragment The feed fragment to be filtered.
     * @since 0.1.0
     */
    public void show(FilterableFeedFragment feedFragment) {
        feedFragment.getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .add(R.id.master_container, this)
                .addToBackStack(null)
                .commit();

        mFeedFragment = feedFragment;
    }
}
