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
import co.vamojunto.model.RideOffer;
import co.vamojunto.model.User;

/**
 * A {@link android.support.v4.app.Fragment} to display all rides that the current user is confirmed
 * as a passenger.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 1.0.0
 */
public class RidesAsPassengerFragment extends AbstractListRideOffersFragment {

    @Override
    protected Task<List<RideOffer>> filter(Map<String, String> filterValues) {
        return RideOffer.getFilteredRidesAsPassengerAsync(User.getCurrentUser(), filterValues);
    }

    @Override
    protected boolean isOfflineFeed() {
        return false;
    }

    /**
     * Returns a {@link bolts.Task} for the query to get all rides that the current user is
     * confirmed as a passenger.
     *
     * @return A {@link bolts.Task} containing the {@link java.util.List} of rides as result.
     * @since 0.1.0
     */
    @Override
    protected Task<List<RideOffer>> getRidesAsync() {
        return RideOffer.getRidesAsPassengerAsync(User.getCurrentUser());
    }

}
