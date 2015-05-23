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

import android.app.Fragment;

import java.util.List;
import java.util.Map;

import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.Ride;
import co.vamojunto.model.User;

/**
 * A simple {@link Fragment} subclass.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.3.0
 * @version 1.0.0
 */
public class ListUFAMOffersFragment extends AbstractListRideOffersFragment {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ListUFAMOffersFragment.
     * @since 0.3.0
     */
    public static ListUFAMOffersFragment newInstance() {
        return new ListUFAMOffersFragment();
    }

    public ListUFAMOffersFragment() {
        // Required empty public constructor
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_list_rides;
    }

    @Override
    protected boolean isOfflineFeed() {
        return false;
    }

    @Override
    protected Task<List<Ride>> getRidesAsync() {
        return Ride.getOffersByCompany(UFAMFeedFragment.COMPANY_CODE);
    }

    @Override
    protected Task<List<Ride>> filter(Map<String, String> filterValues) {
        return Ride.getOffersByCompany(UFAMFeedFragment.COMPANY_CODE, filterValues);
    }

}
