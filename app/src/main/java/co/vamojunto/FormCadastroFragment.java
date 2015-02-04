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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Rules;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.mobsandgeeks.saripaar.annotation.Regex;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.mobsandgeeks.saripaar.annotation.TextRule;


/**
 * Fragment contendo o formulário de registro de novo usuário.
 *
 * @author Andrew C. Pacifico (andrewcpacifico@gmail.com)
 * @since 0.1.0
 */
public class FormCadastroFragment extends Fragment implements Validator.ValidationListener {

    @Required(order = 1, messageResId = R.string.required_field_error)
    private EditText mNomeEditText;

    @Required(order = 2, messageResId = R.string.required_field_error)
    @Email(order = 3, messageResId = R.string.invalid_email_error)
    private EditText mEmailEditText;

    @Regex(order = 4, pattern = "[A-Za-z0-9]{6,20}", messageResId = R.string.invalid_password_error)
    @Password(order = 5)
    private EditText mSenhaEditText;

    @ConfirmPassword(order = 6, messageResId = R.string.password_not_match)
    private EditText mConfirmSenhaEditText;

    /** Utilizado para validar os valores dos inputs do formulário */
    private Validator mValidator;

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
        Toast.makeText(getActivity(), "Tudo joia", Toast.LENGTH_LONG).show();
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

}