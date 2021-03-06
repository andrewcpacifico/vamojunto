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


import java.util.List;
import java.util.Map;

import bolts.Task;
import co.vamojunto.model.RideRequest;

/**
 * A Fragment to display the list of ride requests made on the UFAM Feed.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.3.0
 * @version 1.0.0
 */
public class ListUFAMRequestsFragment extends AbstractListRideRequestsFragment {

    public ListUFAMRequestsFragment() {
        // Required empty public constructor
    }

    public static ListUFAMRequestsFragment newInstance() {
        return new ListUFAMRequestsFragment();
    }

    @Override
    protected Task<List<RideRequest>> filter(Map<String, String> filterValues) {
        return RideRequest.getRequestsByCompany(UFAMFeedFragment.COMPANY_CODE, filterValues);
    }

    @Override
    protected boolean isOfflineFeed() {
        return false;
    }

    @Override
    protected Task<List<RideRequest>> getRideRequestsAsync() {
        return RideRequest.getRequestsByCompany(UFAMFeedFragment.COMPANY_CODE);
    }

}
