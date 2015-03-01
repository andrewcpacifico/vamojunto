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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Modelo de usuário do sitema. Atualmente é uma extensão da classe {@link com.parse.ParseUser},
 * apenas para adicionar algumas funcionalidades mais específicas.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class Usuario {

    private final static String FIELD_IMG_PERFIL = "img_perfil";
    private final static String FIELD_NOME = "nome";

    private ParseUser mParseUser;

    public Usuario(ParseUser pUser) {
        this.mParseUser = pUser;
    }

    public String getNome() {
        return mParseUser.get(FIELD_NOME).toString();
    }

    public Bitmap getImage() {
        ParseFile imgUsuarioPFile = mParseUser.getParseFile(FIELD_IMG_PERFIL);
        Bitmap imgUsuario = null;

        // Caso o usuário não possua imagem de perfil cadastrada
        if (imgUsuarioPFile != null) {
            try {
                imgUsuario = BitmapFactory.decodeByteArray(imgUsuarioPFile.getData(), 0, imgUsuarioPFile.getData().length);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return imgUsuario;
    }

    public static Usuario getCurrentUser() {
        return new Usuario(ParseUser.getCurrentUser());
    }

    public ParseObject toParseObject() {
        return mParseUser;
    }

}
