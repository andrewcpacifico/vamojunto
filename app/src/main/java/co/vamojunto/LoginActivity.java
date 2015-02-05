/*
 * Copyright (c) 2015. Vamo Junto Ltda. Todos os direitos reservados.
 *
 * Este software contém informações confidenciais e de propriedade da Vamo Junto Ltda,
 * ("Informações Confidenciais"). Você não deve divulgar tais informações, e deve usá-las somente em
 * conformidade com os termos do contrato de licença estabelecido entre você e a Vamo Junto Ltda.
 *
 * See LICENSE.txt
 */

package co.vamojunto;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
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
public class LoginActivity extends Activity {

    /** Usado para identificação nos logs */
    private static final String TAG = "LoginActivity";

    /** Janela de diálogo exibida durante o processo de login. */
    private ProgressDialog mProDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initButtons();
    }

    /**
     * Inicializa as propriedades de todos os botões da interface
     */
    private void initButtons() {
        Button fbAuthButton = (Button) findViewById(R.id.fb_auth_button);
        fbAuthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbAuthButtonClick(v);
            }
        });

        final Button cadastroButton = (Button) findViewById(R.id.cadastro_button);
        cadastroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cadastroButtonOnClick(v);
            }
        });
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
     * Ação executada ao pressionar o botão de autenticação com Facebook. O usuário é autenticado
     * com a sua conta do FB, caso o usuário ainda não esteja cadastrado em nossa base de dados,
     * é criado um novo usuário, com as informações retiradas da conta do Facebook.
     *
     * @param v View do botão que foi pressionado.
     */
    private void fbAuthButtonClick(View v) {
        startLoading();

        ParseFacebookUtils.logIn(Arrays.asList(ParseFacebookUtils.Permissions.User.EMAIL),
                this, new LogInCallback() {
            @Override
            public void done(final ParseUser user, ParseException err) {
                if (user == null) {
                    Log.i(TAG, "Login com Facebook cancelado pelo usuário.");
                } else {
                    if (user.isNew()) {
                        Log.i(TAG, "Um usuário novo se autenticou com o Facebook.");

                        // Requisita os dados adicionais do usuário para serem inseridos no ParseUser
                        Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
                            @Override
                            public void onCompleted(GraphUser graphUser, Response response) {
                                Log.d(TAG, "Request finalizada");

                                if (graphUser != null) {
                                    String userEmail = (String) graphUser.getProperty("email");

                                    user.setEmail(userEmail);
                                    user.saveInBackground();
                                }
                            }
                        }).executeAsync();
                    } else {
                        Log.i(TAG, "Um usuário já existente se autenticou com o Facebook.");
                    }

                    stopLoading();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
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
}
