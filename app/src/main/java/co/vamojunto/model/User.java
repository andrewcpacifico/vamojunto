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
 * System's User Model. Currently is just an extension of {@link com.parse.ParseUser} class, with
 * some specific methods and fields.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
@ParseClassName("_User")
public class User extends ParseUser implements Parcelable {

    private final static String FIELD_ID = "objectId";
    private final static String FIELD_NAME = "nome";
    private final static String FIELD_USERNAME = "username";
    private final static String FIELD_EMAIL = "email";
    private final static String FIELD_PROFILE_IMG = "img_perfil";

    private Bitmap profileImage;

    public User() {
        profileImage = null;
    }

    public String getId() {
        return getObjectId();
    }

    public String getName() {
        return getString(FIELD_NAME);
    }

    public void setName(String name) {
        put(FIELD_NAME, name);
    }

    public Bitmap getProfileImage() {
        if (profileImage == null) {
            ParseFile imgUsuarioPFile = getParseFile(FIELD_PROFILE_IMG);
            // checks if the user have a profile image before convert it to an Bitmap
            if (imgUsuarioPFile != null) {
                try {
                    profileImage = BitmapFactory.decodeByteArray(imgUsuarioPFile.getData(), 0, imgUsuarioPFile.getData().length);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        return profileImage;
    }

    public void setImgPerfil(Bitmap imgPerfil) {
        this.profileImage = imgPerfil;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        this.profileImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        ParseFile pFile = new ParseFile("img_perfil.jpg", stream.toByteArray());
        put(FIELD_PROFILE_IMG, pFile);
    }

    public static User getCurrentUser() {
        return (User) ParseUser.getCurrentUser();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;

        if (o.getClass() == User.class) {
            User u = (User) o;

            return u.getId() == this.getId();
        }

        return false;
    }

    /***************************************************************************************************
 *
 * Turning into a Parcelable object
 *
 ***************************************************************************************************/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        this.getProfileImage().compress(Bitmap.CompressFormat.JPEG, 100, stream);

        dest.writeString(getId());
        dest.writeString(getName());
        dest.writeString(getUsername());
        dest.writeString(getEmail());
        dest.writeInt(stream.toByteArray().length);
        dest.writeByteArray(stream.toByteArray());
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            User u = ParseObject.createWithoutData(User.class, in.readString());
            u.setName(in.readString());
            u.setUsername(in.readString());
            u.setEmail(in.readString());

            byte[] imgBytes = new byte[in.readInt()];
            in.readByteArray(imgBytes);
            u.setImgPerfil(BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length));

            return u;
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

}
