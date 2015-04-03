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

package co.vamojunto.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import co.vamojunto.R;
import co.vamojunto.ui.activities.MainActivity;
import co.vamojunto.ui.activities.RideDetailsActivity;

/**
 * Custom PushBroadcastReceiver for application. This class handles all push notification sent
 * to application.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class VamoJuntoPushBroadcastReceiver extends ParsePushBroadcastReceiver {

    private static final String TAG = "PushReceiver";

    /**
     * Code for notifications pushed when seat requests are received.
     */
    private static final int SEAT_REQ_RECEIVED = 1;

    /**
     * Code for notifications pushed when seat requests are confirmed.
     */
    private static final int SEAT_REQ_CONFIRMED = 2;

    /**
     * Data sent by ParsePush
     */
    private JSONObject mData;

    /**
     * The code for notification received
     */
    private int mCode;

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        Log.i(TAG, "Push received");

        mCode = -1;
        String data = intent.getExtras().getString("com.parse.Data");
        try {
            mData = new JSONObject(data);
            mCode = mData.getInt("code");
        } catch (JSONException e) {
            Log.d(TAG, "Error while processing push data json", e);
        }

        super.onPushReceive(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
        Log.d(TAG, "Push opened");
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = getIntent(context);

        if (resultIntent != null) {
            NotificationCompat.Builder mBuilder =
                    null;
            try {
                mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_notify)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(getMessage(context));
            } catch (JSONException e) {
                Log.e(TAG, "Error on parse json", e);
                return null;
            }

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            Intent i = new Intent(context, MainActivity.class);
            stackBuilder.addNextIntent(i);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setAutoCancel(true);

            return mBuilder.build();
        }

        return super.getNotification(context, intent);
    }

    /**
     * Returns a message to display on notification
     *
     * @return A message for notification
     */
    protected String getMessage(Context context) throws JSONException {
        switch (mCode) {
            case SEAT_REQ_RECEIVED:
                return context.getString(R.string.notifymsg_seat_requested);

            case SEAT_REQ_CONFIRMED:
                String driverName = mData.getString("driver_name");
                return context.getString(R.string.notifymsg_seatrequest_confirmed, driverName);
        }

        return "";
    }

    /**
     * Returns the intent specific for the pushed notification.
     *
     * @param context The context of notification.
     * @return The intent containing the information about the activity to be displayed when the
     *         user clicks on notification.
     */
    protected Intent getIntent(Context context) {
        Intent intent = null;

        switch (mCode) {
            case SEAT_REQ_RECEIVED:
                intent = getSeatReqReceivedIntent(context);
                break;

            case SEAT_REQ_CONFIRMED:
                intent = getSeatReqConfirmedIntent(context);
                break;
        }

        return intent;
    }

    /**
     * Returns the intent to respond for seat request confirmed notifications.
     *
     * @param context The context of notification.
     * @return The resultIntent for notification.
     */
    private Intent getSeatReqConfirmedIntent(Context context) {
        Intent intent = null;

        try {
            String rideId = mData.getString("ride_id");

            intent = new Intent(context, RideDetailsActivity.class);
            intent.putExtra(RideDetailsActivity.EXTRA_RIDE_ID, rideId);
        } catch (JSONException e) {
            Log.e(TAG, "Error on parse input json.", e);
        }

        return intent;
    }

    /**
     * Returns the intent specific to notifications sent when a seat is requested on some ride
     * offered by user.
     *
     * @param context The context of notification.
     * @return The intent for this type of notification.
     */
    private Intent getSeatReqReceivedIntent(Context context) {
        Intent intent = null;
        try {
            String rideId = mData.getString("ride_id");

            intent = new Intent(context, RideDetailsActivity.class);
            intent.putExtra(RideDetailsActivity.EXTRA_RIDE_ID, rideId);
        } catch (JSONException e) {
            Log.e(TAG, "Error on parse input json.", e);
        }

        return intent;
    }

}
