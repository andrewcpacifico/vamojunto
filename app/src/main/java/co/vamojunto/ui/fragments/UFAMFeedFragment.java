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

import android.support.v4.app.Fragment;
import android.view.View;

import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.Company;
import co.vamojunto.model.Ride;
import co.vamojunto.model.User;
import co.vamojunto.model.UserCompany;

/**
 * A simple {@link Fragment} subclass.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.3.0
 * @version 1.0.0
 */
public class UFAMFeedFragment extends AbstractFeedFragment {

    private static final String COMPANY_CODE = "ufam";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UFAMFeedFragment.
     */
    public static UFAMFeedFragment newInstance() {
        return new UFAMFeedFragment();
    }

    public UFAMFeedFragment() {
        // Required empty public constructor
    }

    @Override
    protected Task<UserCompany.Status> isAuthorized() {
        final Task<UserCompany.Status>.TaskCompletionSource tcs = Task.create();

        User.getCurrentUser().getUserCompanies().continueWith(new Continuation<List<UserCompany>, Void>() {
            @Override
            public Void then(Task<List<UserCompany>> task) throws Exception {
                if (task.isCancelled()) {
                    tcs.setCancelled();
                } else if (task.isFaulted()) {
                    tcs.setError(task.getError());
                } else {
                    List<UserCompany> userCompanies = task.getResult();
                    UserCompany.Status approvationStatus = UserCompany.Status.REJECTED;

                    Company ufamCompany = new Company();
                    ufamCompany.setCode(COMPANY_CODE);

                    for (UserCompany pair: userCompanies) {
                        if (pair.getCompany().equals(ufamCompany)) {
                            approvationStatus = pair.getStatus();
                        }
                    }

                    tcs.setResult(approvationStatus);
                }

                return null;
            }
        });

        return tcs.getTask();
    }

    @Override
    protected void initNotAuthorizedComponents(View rootView) {
//        Button btn = (Button) rootView.findViewById(R.id.button);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getActivity(), "Teste", Toast.LENGTH_LONG).show();
//            }
//        });
    }

    @Override
    protected int getNotAuthorizedLayoutRes() {
        return R.layout.layout_ufam_registration;
    }

    @Override
    public AbstractListRidesFragment<Ride> getListOffersFragment() {
        return ListUFAMOffersFragment.newInstance();
    }

//    @Override
//    public ListUFAMOffersFragment getListRequestsFragment() {
//        return ListUFAMOffersFragment.newInstance();
//    }

}
