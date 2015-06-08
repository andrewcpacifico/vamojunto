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
import co.vamojunto.model.User;

/**
 * A {@link android.support.v4.app.Fragment} where the user can view all requests made
 * by his friends.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 2.0
 */
public class FriendsRequestsFragment extends AbstractListRideRequestsFragment {

    /**
     * Use this factory method to create a new instance of this fragment using the provided
     * parameters. Avoid to use the class constructor.
     *
     * @return A new instance of fragment FriendsRequestsFragment.
     * @since 0.6.0
     */
    public static FriendsRequestsFragment newInstance() {
        return new FriendsRequestsFragment();
    }

    @Override
    protected boolean isOfflineFeed() {
        return false;
    }

    @Override
    protected Task<List<RideRequest>> getRideRequestsAsync() {
        return RideRequest.getFriendsRequestsAsync(User.getCurrentUser());
    }

    @Override
    protected Task<List<RideRequest>> filter(Map<String, String> filterValues) {
        return RideRequest.getFilteredFriendsRequestsAsync(User.getCurrentUser(), filterValues);
    }

}
