/*
 * Copyright (c) 2015. Vamo Junto Ltda. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Vamo Junto Ltda,
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you entered into with Vamo Junto Ltda.
 *
 * See LICENSE.txt
 */

package co.vamojunto;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.util.Arrays;


/**
 * Activity de Login do aplicativo. Redireciona para a tela principal caso o usuário já esteja
 * autenticado no sistema.
 *
 * @author Andrew C. Pacifico (andrewcpacifico@gmail.com)
 * @since 0.1.0
 */
public class LoginActivity extends ActionBarActivity {

    /** Usado para identificação nos logs */
    private static final String TAG = "LoginActivity";

    /** Utilizado para gerenciamento da sessão do usuário. */
    private UiLifecycleHelper uiHelper;

    /** Ouvinte para as alterações no estado da sessão do usuário. */
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    /**
     * Controla as mudanças no estado da sessão do usuário.
     *
     * @param session
     * @param state
     * @param exception
     */
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        /* Caso o usuário esteja autenticado, exibe a tela principal do aplicativo.  */
        if (state.isOpened()) {
            Log.i(TAG, "Logged in...");

            /* Cadastra o usuário na base do Parse */
            ParseFacebookUtils.logIn(Arrays.asList("email"), this, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException err) {
                    if (user == null) {
                        Log.d(TAG, "Uh oh. The user cancelled the Facebook login.");
                    } else if (user.isNew()) {
                        Log.d(TAG, "User signed up and logged in through Facebook!");
                    } else {
                        Log.d(TAG, "User logged in through Facebook!");
                    }
                }
            });

            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
            this.finish();
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        LoginButton authButton = (LoginButton) findViewById(R.id.authButton);
        authButton.setReadPermissions(Arrays.asList("email"));
    }

    @Override
    public void onResume() {
        super.onResume();

        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }

        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

}
