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
import android.os.Parcel;
import android.os.Parcelable;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;

/**
 * Modelo de usuário do sitema. Atualmente é uma extensão da classe {@link com.parse.ParseUser},
 * apenas para adicionar algumas funcionalidades mais específicas.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
@ParseClassName("_User")
public class Usuario extends ParseUser implements Parcelable {

    private final static String FIELD_ID = "objectId";
    private final static String FIELD_NOME = "nome";
    private final static String FIELD_USERNAME = "username";
    private final static String FIELD_EMAIL = "email";
    private final static String FIELD_IMG_PERFIL = "img_perfil";

    private Bitmap mImgPerfil;

    public Usuario() {
        mImgPerfil = null;
    }

    public String getId() {
        return getObjectId();
    }

    public String getNome() {
        return getString(FIELD_NOME);
    }

    public void setNome(String nome) {
        put(FIELD_NOME, nome);
    }

    public Bitmap getImgPerfil() {
        if (mImgPerfil == null) {
            ParseFile imgUsuarioPFile = getParseFile(FIELD_IMG_PERFIL);
            Bitmap imgUsuario = null;
            // Caso o usuário não possua imagem de perfil cadastrada
            if (imgUsuarioPFile != null) {
                try {
                    mImgPerfil = BitmapFactory.decodeByteArray(imgUsuarioPFile.getData(), 0, imgUsuarioPFile.getData().length);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        return mImgPerfil;
    }

    public void setImgPerfil(Bitmap imgPerfil) {
        this.mImgPerfil = imgPerfil;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        this.mImgPerfil.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        ParseFile pFile = new ParseFile("img_perfil.jpg", stream.toByteArray());
        put(FIELD_IMG_PERFIL, pFile);
    }

/***************************************************************************************************
 *
 * Transformando em um Parcelable object
 *
 ***************************************************************************************************/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        this.mImgPerfil.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        dest.writeString(getId());
        dest.writeString(getNome());
        dest.writeString(getUsername());
        dest.writeString(getEmail());
        dest.writeInt(stream.toByteArray().length);
        dest.writeByteArray(stream.toByteArray());
    }

    public static final Parcelable.Creator<Usuario> CREATOR = new Parcelable.Creator<Usuario>() {
        public Usuario createFromParcel(Parcel in) {
            Usuario u = ParseObject.createWithoutData(Usuario.class, in.readString());
            u.setNome(in.readString());
            u.setUsername(in.readString());
            u.setEmail(in.readString());

            byte[] imgBytes = new byte[in.readInt()];
            in.readByteArray(imgBytes);
            u.setImgPerfil(BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length));

            return u;
        }

        public Usuario[] newArray(int size) {
            return new Usuario[size];
        }
    };

}
