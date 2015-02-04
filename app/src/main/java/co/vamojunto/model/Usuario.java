/*
 * Copyright (c) 2015. Vamo Junto Ltda. Todos os direitos reservados.
 *
 * Este software contém informações confidenciais e de propriedade da Vamo Junto Ltda,
 * ("Informações Confidenciais"). Você não deve divulgar tais informações, e deve usá-las somente em
 * conformidade com os termos do contrato de licença estabelecido entre você e a Vamo Junto Ltda.
 *
 * See LICENSE.txt
 */

package co.vamojunto.model;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Classe utilizada como modelo de um usuário na aplicação. Além de servir como interface entre
 * o sistema e a classe ParseUser, para facilitar caso seja necessário deixar de usa-la no futuro.
 *
 * @author Andrew C. Pacifico (andrewcpacifico@gmail.com)
 * @since 0.1.0
 */
public class Usuario {

    private static final String TAG = "Usuario";

    private String mNome;
    private String mEmail;
    private String mSenha;

    /** Utilizado para comunicar com os dados do usuário na base do Parse */
    private ParseUser mParseUser;

    /**
     * Inicializa os atributos da classe.
     *
     * @param nome Nome do usuário.
     * @param email Email do usuário.
     * @param senha Senha do usuário.
     */
    public Usuario(String nome, String email, String senha) {
        this.mNome = nome;
        this.mEmail = email;
        this.mSenha = senha;

        mParseUser = new ParseUser();
        mParseUser.setEmail(mEmail);
        mParseUser.setPassword(mSenha);
        mParseUser.put("nome", mNome);

        // Temporariamente a aplicação não exige um nome de usuário
        // Como é obrigatório fornecer um para o cadastro com o Parse, passo o email como valor.
        mParseUser.setUsername(mEmail);
    }

    public String getNome() {
        return mNome;
    }

    public void setNome(String nome) {
        this.mNome = nome;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }

    public String getSenha() {
        return mSenha;
    }

    public void setSenha(String senha) {
        this.mSenha = senha;
    }

    public void cadastra() {
        mParseUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if ( e != null ) {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }

}
