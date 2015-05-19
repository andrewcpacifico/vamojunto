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
import android.provider.ContactsContract;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;
import co.vamojunto.helpers.FacebookHelper;

/**
 * System's User Model. Currently is just an extension of {@link com.parse.ParseUser} class, with
 * some specific methods and fields.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
@ParseClassName("_User")
public class User extends ParseUser {

    private static final String TAG = "User";

    public final static String FIELD_ID = "objectId";
    public final static String FIELD_NAME = "name";
    public final static String FIELD_USERNAME = "username";
    public final static String FIELD_EMAIL = "email";
    public final static String FIELD_PROFILE_IMG = "profile_img";
    public final static String FIELD_FB_AUTH_DATA = "authData";
    public final static String FIELD_FB_ID = "fbId";

    private Bitmap profileImage;

    private static Map<String, User> instances = new HashMap<String, User>();

    public static void storeInstance(String key, User value) {
        instances.put(key, value);
    }

    public static User getStoredInstance(String key) {
        User u = instances.get(key);
        instances.remove(key);

        return u;
    }

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

    /**
     * Returns the facebook id of the user.
     *
     * @return The facebook id of the user, or <code>null</code> if some error occurs.
     * @since 0.1.0
     */
    public String parseFacebookIdFromAuthData() {
        JSONObject fbAuthData = getJSONObject(FIELD_FB_AUTH_DATA);
        String fbId = null;
        try {
            fbId = fbAuthData.getJSONObject("facebook").getString("id");
        } catch (JSONException e) {
            Log.e(TAG, "[parseFacebookIdFromAuthData] Error on parsing JSON: ", e);
        }

        return fbId;
    }

    /**
     * Return the facebook access token, associated to this User instance.
     *
     * @return The current facebook access token for this.
     * @since 0.1.1
     */
    public String getFacebookAccessToken() {
        JSONObject fbAuthData = getJSONObject(FIELD_FB_AUTH_DATA);
        String fbAccessToken = null;
        try {
            fbAccessToken = fbAuthData.getJSONObject("facebook").getString("access_token");
        } catch (JSONException e) {
            Log.e(TAG, "[getFacebookAccessToken] Error on parsing JSON: ", e);
        }

        return fbAccessToken;
    }

    public String getFacebookId() {
        return getString(FIELD_FB_ID);
    }

    public void setFacebookId(String fbId) {
        put(FIELD_FB_ID, fbId);
    }

    /**
     * Get the list of user's Facebook friends.
     *
     * @return A {@link bolts.Task} containing the result list.
     * @since 0.1.0
     */
    public Task<List<User>> getFacebookFriends() {
        final Task<List<User>>.TaskCompletionSource tcs = Task.create();
        final Capture<Map<String, User>> friendsMap = new Capture<>(null);

        FacebookHelper.getMyFriendsAsync().continueWithTask(
                new Continuation<List<String>, Task<List<User>>>() {
                    @Override
                    public Task<List<User>> then(Task<List<String>> task) throws Exception {
                        if (task.isCancelled()) {
                            return Task.cancelled();
                        } else if (task.isFaulted()) {
                            return Task.forError(task.getError());
                        } else {
                            List<String> friendsFbId = task.getResult();

                            ParseQuery<User> query = ParseQuery.getQuery(User.class);
                            query.whereContainedIn(FIELD_FB_ID, friendsFbId);

                            return query.findInBackground();
                        }
                    }
                }
        ).continueWithTask(new Continuation<List<User>, Task<List<User>>>() {
            @Override
            public Task<List<User>> then(Task<List<User>> task) throws Exception {
                if (task.isCancelled()) {
                    return Task.cancelled();
                } else if (task.isFaulted()) {
                    return Task.forError(task.getError());
                } else {
                    // list with all users that are friends of this user on Facebook
                    List<User> fbFriends = task.getResult();

                    // creates a map with Facebook friends, to speed up the searching on the next
                    // block
                    Map<String, User> fbFriendsMap = new HashMap<String, User>();
                    for (User user: fbFriends) {
                        fbFriendsMap.put(user.getId(), user);
                    }
                    friendsMap.set(fbFriendsMap);

                    return Friendship.getFollowedByUser(User.this);
                }
            }
        }).continueWith(new Continuation<List<User>, Void>() {
            @Override
            public Void then(Task<List<User>> task) throws Exception {
                if (task.isCancelled()) {
                    tcs.setCancelled();
                } else if (task.isFaulted()) {
                    tcs.setError(task.getError());
                } else {
                    List<User> friendsFollowed = task.getResult();
                    Map<String, User> fbFriendsMap = friendsMap.get();

                    // removes all facebook friends already followed
                    for (User user: friendsFollowed) {
                        fbFriendsMap.remove(user.getId());
                    }

                    // the remaining users are set as task result
                    tcs.setResult(new ArrayList<User>(fbFriendsMap.values()));
                }

                return null;
            }
        });

        return tcs.getTask();
    }

    public static User getCurrentUser() {
        return (User) ParseUser.getCurrentUser();
    }

    public Task<List<Company>> getUserCompanies() {
        final Task<List<Company>>.TaskCompletionSource tcs = Task.create();

        ParseQuery<UserCompany> userCompanyQuery = ParseQuery.getQuery(UserCompany.class);
        userCompanyQuery.whereEqualTo(UserCompany.FIELD_USER, this);
        userCompanyQuery.include(UserCompany.FIELD_COMPANY);
        userCompanyQuery.findInBackground().continueWith(new Continuation<List<UserCompany>, Void>() {
            @Override
            public Void then(Task<List<UserCompany>> task) throws Exception {
                if (task.isCancelled()) {
                    tcs.setCancelled();
                } else if (task.isFaulted()) {
                    tcs.setError(task.getError());
                } else {
                    List<UserCompany> relation = task.getResult();
                    List<Company> lstResult = new ArrayList<Company>();

                    for (UserCompany pair : relation) {
                        lstResult.add(pair.getCompany());
                    }

                    tcs.setResult(lstResult);
                }

                return null;
            }
        });

        return tcs.getTask();
    }

    /**
     * Compares two users, using their id as the comparison criteria.
     *
     * @param o The user to compare.
     * @return <code>true</code> if the users are the same, and <code>false</code> if not.
     * @since 0.1.0
     */
    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;

        if (o.getClass() == User.class) {
            User u = (User) o;

            return u.getId().equals(this.getId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "User: {Name: " + getName() + ", Email: " + getEmail() + "}";
    }

}
