/*
 * Copyright (c) 2015. Vamo Junto Ltda. Todos os direitos reservados.
 *
 * Este software contém informações confidenciais e de propriedade da Vamo Junto Ltda,
 * ("Informações Confidenciais"). Você não deve divulgar tais informações, e deve usá-las somente em
 * conformidade com os termos do contrato de licença estabelecido entre você e a Vamo Junto Ltda.
 *
 * See LICENSE.txt
 */

package co.vamojunto.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import co.vamojunto.MainActivity;
import co.vamojunto.R;


/**
 * Fragment contendo o formulário de registro de novo usuário.
 *
 * @author Andrew C. Pacifico (andrewcpacifico@gmail.com)
 * @since 0.1.0
 */
public class FormCadastroFragment extends Fragment implements Validator.ValidationListener {

    private static final String TAG = "FormCadastroFragment";

    @Required(order = 1, messageResId = R.string.error_required_field)
    private EditText mNomeEditText;

    @Required(order = 2, messageResId = R.string.error_required_field)
    @Email(order = 3, messageResId = R.string.error_invalid_email)
    private EditText mEmailEditText;

    @Regex(order = 4, pattern = "[A-Za-z0-9]{6,20}", messageResId = R.string.invalid_password_error)
    @Password(order = 5)
    private EditText mSenhaEditText;

    @ConfirmPassword(order = 6, messageResId = R.string.password_not_match)
    private EditText mConfirmSenhaEditText;

    /** Utilizado para validar os valores dos inputs do formulário */
    private Validator mValidator;

    /** Diálogo exibido durante o cadastro do usuário */
    private ProgressDialog mProDialog;

    /**
     * Instancia o Validator durante a criação do formulário.
     */
    public FormCadastroFragment() {
        mValidator = new Validator(this);
        mValidator.setValidationListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cadastro, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initComponents(view);
    }

    /**
     * Inicializa os componentes visuais da tela.
     *
     * @param v RootView contendo o layout do Fragment
     */
    private void initComponents(View v) {
        mNomeEditText = (EditText) v.findViewById(R.id.nome_edit_text);
        mEmailEditText = (EditText) v.findViewById(R.id.email_edit_text);
        mSenhaEditText = (EditText) v.findViewById(R.id.senha_edit_text);
        mConfirmSenhaEditText = (EditText) v.findViewById(R.id.confirm_senha_edit_text);

        Button cadastroButton = (Button) v.findViewById(R.id.cadastro_button);
        cadastroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mValidator.validate();
            }
        });
    }

    /**
     * Executado quando todos os inputs do formulários possuem valores válidos
     */
    @Override
    public void onValidationSucceeded() {
        startLoading();

        ParseUser u = new ParseUser();
        u.put("nome", mNomeEditText.getText().toString());
        u.setEmail(mEmailEditText.getText().toString());
        u.setUsername(mEmailEditText.getText().toString());
        u.setPassword(mSenhaEditText.getText().toString());

        u.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                stopLoading();
                // Caso nenhum erro tenha ocorrido no cadastro do usuário. A tela principal será
                // exibida
                if ( e == null ) {
                    Activity cadastroActivity = getActivity();

                    Intent intent = new Intent(cadastroActivity, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //cadastroActivity.finish();
                } else {
                    // O email informado pelo usuário não está disponível.
                    if ( e.getCode() == ParseException.EMAIL_TAKEN  ||
                            e.getCode() == ParseException.USERNAME_TAKEN) {
                        mEmailEditText.setError(getString(R.string.error_email_taken));
                        mEmailEditText.requestFocus();
                    } else if ( e.getCode() == ParseException.INVALID_EMAIL_ADDRESS ) {
                        mEmailEditText.setError(getString(R.string.error_invalid_email));
                        mEmailEditText.requestFocus();
                    }
                    else {
                        // Caso tenha ocorrido qualquer outro erro.
                        Toast.makeText(getActivity(), R.string.error_sign_up, Toast.LENGTH_LONG).show();
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Executado sempre que for detectado algum erro em algum input do formulário. Basicamente
     * exibe a mensagem de erro ao usuário, e muda o foco para o input inválido.
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
     * Exibe um diálogo indicando que a tela principal está sendo carregada.
     */
    private void startLoading() {
        mProDialog = new ProgressDialog(getActivity());
        mProDialog.setMessage(getString(R.string.signing_up));
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

}