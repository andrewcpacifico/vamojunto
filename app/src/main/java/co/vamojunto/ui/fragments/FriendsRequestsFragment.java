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

import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.RideRequest;
import co.vamojunto.model.User;

/**
 * A {@link android.support.v4.app.Fragment} where the user can view all requests made
 * by his friends.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 1.0.0
 */
public class FriendsRequestsFragment extends DefaultListRequestsFragment {

    @Override
    protected boolean isOfflineFeed() {
        return false;
    }

    @Override
    protected Task<List<RideRequest>> getRequestsAsync() {
        return RideRequest.getByRequesterAsync(User.getCurrentUser());
    }

    @Override
    protected String getNoRequestMessage() {
        return getString(R.string.no_friends_requests);
    }

}
