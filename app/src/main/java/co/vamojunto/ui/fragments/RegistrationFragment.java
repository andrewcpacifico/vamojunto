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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.mobsandgeeks.saripaar.annotation.Regex;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.parse.ParseException;
import com.parse.SignUpCallback;

import co.vamojunto.model.User;
import co.vamojunto.ui.activities.MainActivity;
import co.vamojunto.R;


/**
 * {@link android.support.v4.app.Fragment} containing the UI where the user can create an account.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class RegistrationFragment extends Fragment implements Validator.ValidationListener {

    private static final String TAG = "RegistrationFragment";

    /**
     * The field containing the name of the user
     */
    @Required(order = 1, messageResId = R.string.errormsg_required_field)
    private EditText mNameEditText;

    /**
     * The field containing the email of the user
     */
    @Required(order = 2, messageResId = R.string.errormsg_required_field)
    @Email(order = 3, messageResId = R.string.error_invalid_email)
    private EditText mEmailEditText;

    /**
     * The field containing the password of the user
     */
    @Regex(order = 4, pattern = "[A-Za-z0-9]{6,20}", messageResId = R.string.invalid_password_error)
    @Password(order = 5)
    private EditText mPassEditText;

    /**
     * The field containing the password confirmation of the user
     */
    @ConfirmPassword(order = 6, messageResId = R.string.password_not_match)
    private EditText mPassConfirmEditText;

    /**
     * {@link com.mobsandgeeks.saripaar.Validator} for the input values
     */
    private Validator mValidator;

    /**
     * {@link android.app.ProgressDialog} displayed while the user account is being created
     */
    private ProgressDialog mProDialog;

    public RegistrationFragment() {
        // required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_registration, container, false);

        // instantiates the validator
        mValidator = new Validator(this);
        mValidator.setValidationListener(this);

        initComponents(rootView);

        return rootView;
    }

    /**
     * Initializes the visual components
     *
     * @param rootView The Fragment's inflated layout
     */
    private void initComponents(View rootView) {
        mNameEditText = (EditText) rootView.findViewById(R.id.name_edit_text);
        mEmailEditText = (EditText) rootView.findViewById(R.id.email_edit_text);
        mPassEditText = (EditText) rootView.findViewById(R.id.password_edit_text);
        mPassConfirmEditText = (EditText) rootView.findViewById(R.id.password_confirm_edit_text);

        // gets the button instance and register the event handler
        Button registerButton = (Button) rootView.findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mValidator.validate();
            }
        });
    }

    /**
     * Executed when all inputs have valid values
     */
    @Override
    public void onValidationSucceeded() {
        startLoading();

        User u = new User();
        u.setName(mNameEditText.getText().toString());
        u.setEmail(mEmailEditText.getText().toString());
        u.setUsername(mEmailEditText.getText().toString());
        u.setPassword(mPassEditText.getText().toString());

        u.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                stopLoading();
                // if there is no error on user registration, the main screen is displayed
                if ( e == null ) {
                    Activity cadastroActivity = getActivity();

                    Intent intent = new Intent(cadastroActivity, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    // the email given by the user is already taken
                    if ( e.getCode() == ParseException.EMAIL_TAKEN  ||
                            e.getCode() == ParseException.USERNAME_TAKEN) {
                        mEmailEditText.setError(getString(R.string.error_email_taken));
                        mEmailEditText.requestFocus();
                    // the email given by the user is invalid
                    } else if ( e.getCode() == ParseException.INVALID_EMAIL_ADDRESS ) {
                        mEmailEditText.setError(getString(R.string.error_invalid_email));
                        mEmailEditText.requestFocus();
                    }
                    else {
                        // any other error has ocurred
                        Toast.makeText(getActivity(), R.string.error_sign_up, Toast.LENGTH_LONG).show();
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Handles the errors on the input fields.
     *
     * @param failedView Input onde foi detectado o erro.
     * @param failedRule Regra onde o input não passou na validação.
     */
    @Override
    public void onValidationFailed(View failedView, Rule<?> failedRule) {
        if ( failedView instanceof TextView ) {
            ((TextView) failedView).setError(failedRule.getFailureMessage());
            failedView.requestFocus();
        }
    }

    /**
     * Displays a ProgressDialog to indicates that something is being loaded.
     */
    private void startLoading() {
        mProDialog = new ProgressDialog(getActivity());
        mProDialog.setMessage(getString(R.string.signing_up));
        mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProDialog.setCancelable(false);
        mProDialog.show();
    }

    /**
     * Finishes the ProgressDialog.
     */
    private void stopLoading() {
        mProDialog.dismiss();
        mProDialog = null;
    }

}