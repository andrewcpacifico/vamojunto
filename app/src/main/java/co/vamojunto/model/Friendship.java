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

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bolts.Continuation;
import bolts.Task;

/**
 * Friendship model class
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
@ParseClassName("Friendship")
public class Friendship extends ParseObject {

    private static final String TAG = "Friendship";

    public static final String FIELD_FOLLOWING = "following";
    public static final String FIELD_FOLLOWER = "follower";

    private static Map<String, Friendship> instances = new HashMap<String, Friendship>();

    public static void storeInstance(String key, Friendship value) {
        instances.put(key, value);
    }

    public static Friendship getStoredInstance(String key) {
        Friendship r = instances.get(key);
        instances.remove(key);

        return r;
    }

    /**
     * Required default constructor
     */
    public Friendship() { }

    /**
     * A constructor that initializes the fields.
     *
     * @since 0.1.0
     */
    public Friendship(User follower, User following) {
        setFollower(follower);
        setFollowing(following);
    }

    public User getFollowing() {
        return (User) getParseUser(FIELD_FOLLOWING);
    }

    public void setFollowing(User u) {
        put(FIELD_FOLLOWING, u);
    }

    public User getFollower() {
        return (User) getParseUser(FIELD_FOLLOWER);
    }

    public void setFollower(User u) {
        put(FIELD_FOLLOWER, u);
    }

    /**
     * Gets a list of users who is followed by another user.
     *
     * @param u The user to get the list of followings.
     * @return The list of users followed by u.
     */
    public static Task<List<User>> getFollowedByUser(User u) {
        final Task<List<User>>.TaskCompletionSource tcs = Task.create();

        // selects all friendships where the user is the follower
        ParseQuery<Friendship> qFriendship = ParseQuery.getQuery(Friendship.class);
        qFriendship.whereEqualTo(Friendship.FIELD_FOLLOWER, u);
        qFriendship.include(Friendship.FIELD_FOLLOWING);

        qFriendship.findInBackground().continueWith(new Continuation<List<Friendship>, Void>() {
            @Override
            public Void then(Task<List<Friendship>> task) throws Exception {
                if (task.isFaulted()) {
                    tcs.setError(task.getError());
                } else if (task.isCancelled()) {
                    tcs.setCancelled();
                } else {
                    List<Friendship> lstFriendship = task.getResult();
                    List<User> lstUsers = new ArrayList<User>();

                    // iterates over the friendship list, to build the user list to return
                    for (Friendship f: lstFriendship) {
                        lstUsers.add(f.getFollowing());
                    }

                    // stores the users on the local datastore
                    ParseObject.pinAllInBackground("myFriendships", lstFriendship);
                    tcs.setResult(lstUsers);
                }

                return null;
            }
        });

        return tcs.getTask();
    }

    /**
     * Gets a list of users who is followed by another user. Fetches the data from local datastore.
     *
     * @param u The user to get the list of followings.
     * @return The list of users followed by u.
     * @since 0.1.0
     */
    public static Task<List<User>> getFollowedByUserFromLocal(User u) {
        final Task<List<User>>.TaskCompletionSource tcs = Task.create();

        ParseQuery<Friendship> query = ParseQuery.getQuery(Friendship.class);
        query.fromPin("myFriendships");
        query.include(Friendship.FIELD_FOLLOWING);
        query.findInBackground().continueWith(new Continuation<List<Friendship>, Void>() {
            @Override
            public Void then(Task<List<Friendship>> task) throws Exception {
                if (task.isFaulted()) {
                    tcs.setError(task.getError());
                } else if (task.isCancelled()) {
                    tcs.setCancelled();
                } else {
                    List<Friendship> lstFriendship = task.getResult();
                    List<User> lstUsers = new ArrayList<User>();

                    // iterates over the friendship list, to build the user list to return
                    for (Friendship f: lstFriendship) {
                        lstUsers.add(f.getFollowing());
                    }

                    tcs.setResult(lstUsers);
                }

                return null;
            }
        });

        return tcs.getTask();
    }

    /**
     * Deletes the friendships between a given user, and a list of another users
     *
     * @param user The user to remove the friendships, usually this parameter have the current user.
     * @param unfollowed The list of users that the current user wants to unfollow.
     * @since 0.1.0
     */
    public static void unfollow(User user, List<User> unfollowed) {
        ParseQuery<Friendship> query = ParseQuery.getQuery(Friendship.class);
        query.whereEqualTo(FIELD_FOLLOWER, user);
        query.whereContainedIn(FIELD_FOLLOWING, unfollowed);
        query.fromPin("myFriendships");
        try {
            final List<Friendship> myFriendships = query.find();

            Friendship.deleteAllInBackground(myFriendships).continueWith(new Continuation<Void, Object>() {
                @Override
                public Object then(Task<Void> task) throws Exception {
                    Friendship.unpinAll("myFriendships", myFriendships);
                    Log.i(TAG, "Friends removed");

                    return null;
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a list of friendships between a given user, and a list of another users
     *
     * @param user The user to add the friendships, usually this parameter have the current user.
     * @param followed The list of users that the current user wants to follow.
     * @since 0.1.0
     */
    public static void follow(User user, List<User> followed) {
        final List<Friendship> lst = new ArrayList<>();

        for (User friend: followed) {
            lst.add(new Friendship(user, friend));
        }

        Friendship.saveAllInBackground(lst).continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                Friendship.pinAll("myFriendships", lst);
                Log.i(TAG, "Friends added");

                return null;
            }
        });
    }
}
