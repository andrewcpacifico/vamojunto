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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.Arrays;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.helpers.FacebookHelper;
import co.vamojunto.model.User;


/**
 * Activity de Login do aplicativo. Redireciona para a tela principal caso o usuário já esteja
 * autenticado no sistema.
 *
 * Atualmente utiliza a clase ParseUser para gerenciar os dados dos usuários, assim como gerenciar
 * a sessão de usuário.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.1
 * @since 0.1.0
 */
public class LoginActivity extends Activity implements Validator.ValidationListener {

    /** Usado para identificação nos logs */
    private static final String TAG = "LoginActivity";

    /** Janela de diálogo exibida durante o processo de login. */
    private ProgressDialog mProDialog;

    /** Utilizado para validar o formulário de login */
    private Validator validator;

    private Handler mHandler;

    /**
     * Facebook login callback manager.
     *
     * @since 0.1.1
     */
    private CallbackManager mCallbackManager;

    // Campos do formulário de login
    @Required(order = 1, messageResId = R.string.error_required_field)
    @Email(order = 2, messageResId = R.string.error_invalid_email)
    private EditText mEmailEditText;

    @Required(order = 3, messageResId = R.string.error_required_field)
    private EditText mSenhaEditText;
    // Campos do formulário de login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        validator = new Validator(this);
        validator.setValidationListener(this);

        mHandler = new Handler();
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        initComponents();
    }

    /**
     * Inicializa as propriedades relacionadas aos componentes da UI.
     */
    private void initComponents() {
        mEmailEditText = (EditText) findViewById(R.id.email_edit_text);
        mSenhaEditText = (EditText) findViewById(R.id.password_edit_text);

        Button fbAuthButton = (Button) findViewById(R.id.fb_auth_button);
        fbAuthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbAuthButtonClick();
            }
        });

//        Button cadastroButton = (Button) findViewById(R.id.register_button);
//        cadastroButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                cadastroButtonOnClick(v);
//            }
//        });
//
//        Button loginButton = (Button) findViewById(R.id.login_button);
//        loginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                loginButtonOnClick(v);
//            }
//        });
    }

    /**
     * Ao pressionar o botão de login, a UI do formulário é validada, só então a
     * tentativa de login é realizada.
     *
     * @param v View do botão que foi pressionado.
     */
    private void loginButtonOnClick(View v) {
        validator.validate();
    }

    /**
     * Ação executada ao clicar no botão de cadastro, é carregada e exibida a tela de cadastro na
     * aplicação.
     *
     * @param v View do botão que foi pressionado.
     */
    private void cadastroButtonOnClick(View v) {
        Intent intent = new Intent(this, CadastroActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the Facebook authentication button is pressed. The user is logged in with
     * your Facebook account. If the user was not registered on our database, a new user is created
     * on Parse.com, using the information given by Facebook API.
     *
     * @since 0.1.0
     */
    private void fbAuthButtonClick() {
        startLoading();

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                Log.i(TAG, "User successfully authenticated, logging in on parse.com ...");

                ParseFacebookUtils.logInInBackground(loginResult.getAccessToken())
                        .continueWith(new Continuation<ParseUser, Void>() {
                            @Override
                            public Void then(Task<ParseUser> task) throws Exception {
                                stopLoading();
                                AccessToken.setCurrentAccessToken(loginResult.getAccessToken());

                                // if no error happened on parse login
                                if (!task.isFaulted() && !task.isCancelled()) {
                                    final User user = (User) task.getResult();

                                    if (user == null) {
                                        Log.i(TAG, "User null after parse login, this error should not have happened...");
                                    } else {
                                        // if is the first time user logs in, fetch some data from
                                        // facebook account to finish the signup
                                        if (user.isNew()) {
                                            Log.i(TAG, "New user Registered with facebook login...");

                                            // associate the device with a user
                                            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                                            installation.put("user", user);
                                            installation.saveInBackground();

                                            mHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    cadastroFacebook(user);
                                                }
                                            });
                                        } else {
                                            Log.i(TAG, "Existent user logged in with facebook account...");

                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                } else {
                                    displayErrorMessage();
                                    Log.e(TAG, "Some error happened on Facebook login", task.getError());
                                }

                                return null;
                            }
                        });
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "Facebook authentication cancelled by the user");
                stopLoading();
            }

            @Override
            public void onError(FacebookException e) {
                Log.e(TAG, "Error on Facebook authentication", e);
                stopLoading();
                displayErrorMessage();
            }
        });

        Log.i(TAG, "Authenticating user with Facebook...");
        LoginManager.getInstance()
                .logInWithReadPermissions(
                        this,
                        Arrays.asList(
                            FacebookHelper.Permissions.USER_EMAIL,
                            FacebookHelper.Permissions.USER_FRIENDS
                        )
                );
    }

    /**
     * Após a autenticação com o facebook, o usuário é criado, mas apenas os dados essenciais são salvos.
     * Este método obtém os outros dados do usuário a partir da sua conta no Facebook, e salva
     * na base de dados no Parse.
     *
     * Atualmente os dados obtidos da conta do usuário no Facebook são:
     * <ul>
     *     <li>Nome</li>
     *     <li>Email</li>
     *     <li>Imagem de Perfil</li>
     * </ul>
     *
     * @param parseUser Instância do usuário já salvo na base do Parse.
     */
    protected void cadastroFacebook(ParseUser parseUser) {
        final User u = (User) parseUser;

        startLoading();

        // stores the current accessToken to fix the bug with ParseUser clearing the current accessToken
        // after saving, so the current access token is stored and redefined after user saving
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();

        // this request for facebook data, is necessary to get user email
        FacebookHelper.getFBUserAsync().continueWithTask(new Continuation<JSONObject, Task<Bitmap>>() {
            @Override
            public Task<Bitmap> then(Task<JSONObject> task) throws Exception {
                JSONObject userJson = task.getResult();

                u.setName(userJson.getString(FacebookHelper.USER_NAME));
                u.setFacebookId(userJson.getString(FacebookHelper.USER_ID));

                // check if user gave permission to read his email address
                if (AccessToken.getCurrentAccessToken()
                        .getPermissions()
                        .contains(FacebookHelper.Permissions.USER_EMAIL)
                ) {
                    u.setEmail(userJson.getString(FacebookHelper.USER_EMAIL));
                    u.setUsername(userJson.getString(FacebookHelper.USER_EMAIL));
                }

                return FacebookHelper.getProfilePictureAsync(userJson.getString(FacebookHelper.USER_ID));
            }
        }).continueWithTask(new Continuation<Bitmap, Task<Void>>() {

            // Obtém a imagem de perfil do usuário, é gerado um objeto do tipo ParseFile para salvar
            // a imagem na tabela de usuários na nuvem. Em seguida salva os dados do usuário obtidos
            // a partir do facebook na tabela de usuários.
            @Override
            public Task<Void> then(Task<Bitmap> task) throws Exception {
                Bitmap img = task.getResult();

                u.setImgPerfil(img);

                return u.saveInBackground();
            }
        }).continueWith(new Continuation<Void, Void>() {

            // Após salvar todos os dados do usuário, finalmente exibe a tela principal do sistema.
            // TODO Encontrar uma forma de exibir a tela principal enquanto estes dados ainda são carregados, para um login mais rápido.
            @Override
            public Void then(Task<Void> task) throws Exception {
                // redefine the current access token to fix ParseUser bug
                AccessToken.setCurrentAccessToken(accessToken);

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                stopLoading();
                finish();

                return null;
            }
        });
    }

    /**
     * Exibe um diálogo indicando que a tela principal está sendo carregada.
     */
    private void startLoading() {
        mProDialog = new ProgressDialog(this);
        mProDialog.setMessage(getString(R.string.loading));
        mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProDialog.setCancelable(false);
        mProDialog.show();
    }

    /**
     * Finaliza o diálogo do carregamento da tela principal.
     *
     * @since 0.1.0
     */
    private void stopLoading() {
        mProDialog.dismiss();
        mProDialog = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Callback executado quando os dados fornecidos para o formulário são válidos. Tenta executar
     * o login utilizando um objeto ParseUser.
     */
    @Override
    public void onValidationSucceeded() {
        startLoading();

        // Autentica o usuário utilizando o ParseUser
        ParseUser.logInInBackground(mEmailEditText.getText().toString(),
                mSenhaEditText.getText().toString(), new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        // Login aconteceu normalmente
                        if (parseUser != null ){
                            // Exibe a tela principal do sistema e finaliza a Activity de login.
                            //Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            String errorMsg = "";

                            switch (e.getCode()) {
                                case ParseException.INVALID_EMAIL_ADDRESS:
                                    errorMsg = getString(R.string.error_invalid_email);
                                    break;

                                case ParseException.OBJECT_NOT_FOUND:
                                    errorMsg = getString(R.string.error_wrong_email_pass);
                                    break;

                                default:
                                    errorMsg = getString(R.string.error_login);
                            }

                            Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }

                        stopLoading();
                    }
                });
    }

    /**
     * Callback executado quando os dados fornecidos para o formulário são inválidos.
     *
     * @param failedView View onde aconteceu o erro.
     * @param failedRule Regra de validação que foi quebrada.
     */
    @Override
    public void onValidationFailed(View failedView, Rule<?> failedRule) {
        if ( failedView instanceof TextView) {
            ((TextView) failedView).setError(failedRule.getFailureMessage());
            failedView.requestFocus();
        }
    }

    /**
     * Method to encapsulate a default error message displaying.
     *
     * @since 0.1.1
     */
    public void displayErrorMessage() {
        displayErrorMessage(getString(R.string.errormsg_default));
    }

    /**
     * Method to encapsulate error message displaying.
     *
     * @param errorMsg The messag eto display.
     * @since 0.1.1
     */
    public void displayErrorMessage(final String errorMsg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this,
                        errorMsg,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
