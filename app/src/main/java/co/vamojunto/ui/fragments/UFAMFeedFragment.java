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
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseCloud;

import java.util.HashMap;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.Company;
import co.vamojunto.model.User;
import co.vamojunto.model.UserCompany;
import co.vamojunto.util.UIUtil;

/**
 * A simple {@link Fragment} subclass.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.3.0
 * @version 1.0.0
 */
public class UFAMFeedFragment extends AbstractFeedFragment {

    private static final String TAG = "UFAMFeedFragment";

    public static final String COMPANY_CODE = "ufam";

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

        final Company ufamCompany = new Company();
        ufamCompany.setCode(COMPANY_CODE);

        // first look for user companies on the local datastore, if the ufam company was found
        // cancel the next task, if the company
        User.getCurrentUser().getCachedUserCompanies().continueWithTask(
                new Continuation<List<UserCompany>, Task<List<UserCompany>>>() {
                    @Override
                    public Task<List<UserCompany>> then(Task<List<UserCompany>> task) throws Exception {
                        if (task.isCancelled()) {
                            return task;
                        } else if (task.isFaulted()) {
                            return task;
                        } else {
                            List<UserCompany> userCompanies = task.getResult();

                            // iterate over user companies, to check if user have approved to view
                            // the ufam feed
                            for (UserCompany pair : userCompanies) {
                                if (pair.getCompany().equals(ufamCompany)
                                        && pair.getStatus() == UserCompany.Status.APPROVED) {
                                    tcs.setResult(pair.getStatus());

                                    // cancel the searching on cloud, if the user has been
                                    // already approved
                                    return Task.cancelled();
                                }
                            }
                        }

                        return User.getCurrentUser().getUserCompanies();
                    }
                }).continueWith(new Continuation<List<UserCompany>, Void>() {
            @Override
            public Void then(Task<List<UserCompany>> task) throws Exception {
                if (task.isCancelled()) {
                    tcs.setCancelled();
                } else if (task.isFaulted()) {
                    tcs.setError(task.getError());
                } else {
                    List<UserCompany> userCompanies = task.getResult();
                    UserCompany.Status approvationStatus = UserCompany.Status.REJECTED;

                    for (UserCompany pair : userCompanies) {
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
        final EditText matriculaEditText = (EditText) rootView.findViewById(R.id.matricula_edittext);

        final EditText cursoEditText = (EditText) rootView.findViewById(R.id.curso_edittext);

        final EditText codAuthEditText = (EditText) rootView.findViewById(R.id.cod_aut_edittext);

        Button btn = (Button) rootView.findViewById(R.id.send_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (matriculaEditText.getText().toString().equals("")) {
                    matriculaEditText.setError(getString(R.string.errormsg_required_field));
                    matriculaEditText.requestFocus();

                    return;
                } else if (cursoEditText.toString().equals("")){
                    cursoEditText.setError(getString(R.string.errormsg_required_field));
                    cursoEditText.requestFocus();

                    return;
                }

                // define the options to send to cloud function
                HashMap<String, Object> params = new HashMap<>();
                params.put("user_id", User.getCurrentUser().getId());
                params.put("company_code", COMPANY_CODE);

                params.put("curso", cursoEditText.getText().toString());
                params.put("matricula", matriculaEditText.getText().toString());
                params.put("cod_aut", codAuthEditText.getText().toString());

                UIUtil.startLoading(getActivity(), "Carregando...");
                // call cloud function to confirm the seat
                ParseCloud.callFunctionInBackground("askPermission", params).continueWith(new Continuation<Object, Object>() {
                    @Override
                    public Object then(Task<Object> task) throws Exception {
                        UIUtil.stopLoading();

                        getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                changeContent(SCREEN_DEFAULT);
                                displayErrorScreen(getString(R.string.ufam_feed_waiting_msg) , false);
                            }
                        });

                        return null;
                    }
                });
            }
        });
    }

    @Override
    protected int getNotAuthorizedLayoutRes() {
        return R.layout.layout_ufam_registration;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.screentitle_ufam_feed);
    }

    @Override
    public AbstractListRideOffersFragment getListOffersFragment() {
        return ListUFAMOffersFragment.newInstance();
    }

    @Override
    public AbstractListRideRequestsFragment getListRequestsFragment() {
        return ListUFAMRequestsFragment.newInstance();
    }

}
