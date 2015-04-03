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

import com.facebook.model.GraphUser;
import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

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
 * @author Andrew C. Pacifico (andrewcpacifico@gmail.com)
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
                fbAuthButtonClick(v);
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
     */
    private void fbAuthButtonClick(View v) {
        startLoading();

        ParseFacebookUtils.logIn(Arrays.asList(ParseFacebookUtils.Permissions.User.EMAIL),
                this, new LogInCallback() {
                    @Override
                    public void done(final ParseUser user, ParseException err) {
                        // if any error happen on facebook login
                        if (err != null) {
                            stopLoading();

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this,
                                            getString(R.string.errormsg_default),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            if (user == null) {
                                stopLoading();
                                Log.i(TAG, "Login com Facebook cancelado pelo usuário.");
                            } else {
                                if (user.isNew()) {
                                    Log.i(TAG, "Um usuário novo se autenticou com o Facebook.");

                                    cadastroFacebook(user);
                                } else {
                                    Log.i(TAG, "Um usuário já existente se autenticou com o Facebook.");

                                    stopLoading();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }
                    }
                });
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

        // As tarefas abaixo são executadas de forma asíncrona por exigência do SDK, porém,
        // como é necessário finalizar estas tarefas para que o aplicativo possa executar normalmente,
        // as tarefas são executadas em cadeia, o final de cada tarefa dispara o início da segunda,
        // e só ao final de todas as tarefas a tela inicial do aplicativo é exibida.
        FacebookHelper.getGraphUserAsync().continueWithTask(new Continuation<GraphUser, Task<Bitmap>>() {

            // Obtém um objeto GraphUser com os dados da conta do Facebook do usuário. O objeto é
            // necessário para que possamos obter o nome e email do usuário, que são dados em nossa
            // base na nuvem, além do id do usuário no Facebook, que é utilizado para realizar a
            // requsição para obter a imagem de perfil do usuário.
            @Override
            public Task<Bitmap> then(Task<GraphUser> task) throws Exception {
                GraphUser user = task.getResult();

                u.setName(user.getName());
                u.setEmail((String) user.getProperty("email"));
                u.setUsername((String) user.getProperty("email"));

                return FacebookHelper.getProfilePictureAsync(user.getId());
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
     */
    private void stopLoading() {
        mProDialog.dismiss();
        mProDialog = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
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
}
