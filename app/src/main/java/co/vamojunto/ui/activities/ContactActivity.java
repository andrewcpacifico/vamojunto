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

package co.vamojunto.ui.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.HashMap;
import java.util.Map;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.User;
import co.vamojunto.util.NetworkUtil;
import co.vamojunto.util.UIUtil;

/**
 * Activity where the user can send a message to VamoJunto Team.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.5.0
 * @version 1.0
 */
public class ContactActivity extends AppCompatActivity {

    private static final String TAG = "ContactActivity";

    private static final String CONTACT_URL = "http://api.vamojuntoapp.com/contact-message";

    /**
     * Inflated EditText where the user put his name.
     *
     * @since 0.5.0
     */
    private EditText mNameEditText;

    /**
     * Inflated EditText where the user put his email.
     *
     * @since 0.5.0
     */
    private EditText mEmailEditText;

    /**
     * Inflated EditText where the user put the message subject.
     *
     * @since 0.5.0
     */
    private EditText mSubjectEditText;

    /**
     * A handler to run code on the main thread.
     *
     * @since 0.5.0
     */
    private Handler mHandler;

    private EditText mMessageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        mHandler = new Handler();
        initComponents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send) {
            sendMessage();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Send the message to our team.
     *
     * @since 0.5.0
     */
    private void sendMessage() {
        if (! validForm()) {
            return;
        }

        UIUtil.startLoading(this, getString(R.string.sending_message));

        String name = mNameEditText.getText().toString();
        String email = mEmailEditText.getText().toString();
        String msgType = (String) ((Spinner) findViewById(R.id.message_type_spinner)).getSelectedItem();
        String subject = mSubjectEditText.getText().toString();
        String message = mMessageEditText.getText().toString();

        Map<String, String> requestParams = new HashMap<>(5);
        requestParams.put("name", name);
        requestParams.put("email", email);
        requestParams.put("msgType", msgType);
        requestParams.put("subject", subject);
        requestParams.put("message", message);

        NetworkUtil.postData(requestParams, CONTACT_URL).continueWith(new Continuation<String, Void>() {
            @Override
            public Void then(final Task<String> task) throws Exception {
                UIUtil.stopLoading();

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (! task.isFaulted() && ! task.isCancelled()) {
                            new MaterialDialog.Builder(ContactActivity.this)
                                    .title(R.string.message_sent)
                                    .content(R.string.contact_message_sent)
                                    .positiveText("Ok")
                                    .dismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            finish();
                                        }
                                    }).show();
                        } else {
                            Toast.makeText(
                                    ContactActivity.this,
                                    R.string.errormsg_default,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                });

                return null;
            }
        });
    }

    /**
     * Valid the form input data.
     *
     * @return <code>true</code> if all fields have valid data, and <code>false</code> if not.
     * @since 0.5.0
     */
    private boolean validForm() {
        // clear all error icons
        mNameEditText.setError(null);
        mEmailEditText.setError(null);
        mSubjectEditText.setError(null);
        mMessageEditText.setError(null);

        if (mNameEditText.getText().toString().equals("")) {
            mNameEditText.setError(getString(R.string.errormsg_required_field));
            mNameEditText.requestFocus();

            return false;
        } else if (mEmailEditText.getText().toString().equals("")) {
            mEmailEditText.setError(getString(R.string.errormsg_required_field));
            mEmailEditText.requestFocus();

            return false;
        } else if (mSubjectEditText.getText().toString().equals("")) {
            mSubjectEditText.setError(getString(R.string.errormsg_required_field));
            mSubjectEditText.requestFocus();

            return false;
        } else if (mMessageEditText.getText().toString().equals("")) {
            mMessageEditText.setError(getString(R.string.errormsg_required_field));
            mMessageEditText.requestFocus();

            return false;
        }

        return true;
    }

    /**
     * Init screen components.
     *
     * @since 0.5.0
     */
    private void initComponents() {
        setupAppBar();

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.contact_types,
                android.R.layout.simple_spinner_dropdown_item
        );
        Spinner msgTypeSpinner = (Spinner) findViewById(R.id.message_type_spinner);
        msgTypeSpinner.setAdapter(spinnerAdapter);

        mNameEditText = (EditText) findViewById(R.id.name_edit_text);
        mNameEditText.setText(User.getCurrentUser().getName());

        mEmailEditText = (EditText) findViewById(R.id.email_edit_text);
        mEmailEditText.setText(User.getCurrentUser().getEmail());

        mSubjectEditText = (EditText) findViewById(R.id.subject_edit_text);
        mSubjectEditText.requestFocus();

        mMessageEditText = (EditText) findViewById(R.id.message_edit_text);
    }

    /**
     * Setup the Application Bar.
     *
     * @since 0.5.0
     */
    private void setupAppBar() {
        Toolbar appBar = (Toolbar) findViewById(R.id.tool_bar);

        appBar.setNavigationIcon(R.drawable.ic_clear_white_24dp);

        setSupportActionBar(appBar);
        appBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
