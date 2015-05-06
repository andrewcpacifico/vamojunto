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


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

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
     * Inflated button to close this fragment.
     *
     * @since 0.1.0
     */
    private ImageButton mCloseButton;

    /**
     * Inflated button to confirm the feed filtering.
     *
     * @since 0.1.0
     */
    private ImageButton mOkButton;

    /**
     * The feed fragment to filter the results.
     *
     * @since 0.1.0
     */
    private AbstractListRidesFragment mFeedFragment;

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

    private void initComponents(View rootView) {
        mCloseButton = (ImageButton) rootView.findViewById(R.id.close_button);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

        mOkButton = (ImageButton) rootView.findViewById(R.id.ok_button);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();

                mFeedFragment.onFilterFeed();
            }
        });
    }

    private void close() {
        getFragmentManager()
                .beginTransaction()
                .remove(FilterFeedFragment.this)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    public void show(AbstractListRidesFragment listFragment) {
        listFragment.getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .add(R.id.master_container, this)
                .addToBackStack(null)
                .commit();

        mFeedFragment = listFragment;
    }
}
