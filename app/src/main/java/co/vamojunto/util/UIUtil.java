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

package co.vamojunto.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.PointTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import bolts.Task;
import co.vamojunto.R;

/**
 * Util class with User Interface functions.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 4.0
 */
public class UIUtil {

    private static ProgressDialog mProDialog;

    /**
     * Display a progress dialog on the screen with a default loading, message.
     *
     * @since 0.8.0
     */
    public static void startLoading(Context context) {
        startLoading(context, context.getString(R.string.loading));
    }

    /**
     * Display a progress dialog on the screen.
     *
     * @since 0.1.0
     */
    public static void startLoading(Context context, String msg) {
        mProDialog = new ProgressDialog(context);
        mProDialog.setMessage(msg);
        mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProDialog.setCancelable(false);
        mProDialog.show();
    }

    /**
     * Dismisses the previously displayed progressDialog
     *
     * @since 0.1.0
     */
    public static void stopLoading() {
        mProDialog.dismiss();
        mProDialog = null;
    }

    /**
     * Force the soft keyboard to hide.
     *
     * @param activity The context activity.
     * @since 0.5.0
     */
    public static void hideKeyboard(Activity activity) {
        ((InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

//        if (imm.isActive()){
//            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0); // hide
//        } else {
//            imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY); // show
//        }
    }

    /**
     * Display a tooltip for a given View.
     *
     * @param activity
     * @param title
     * @param message
     * @param target
     * @return
     * @since 0.6.0
     */
    public static Task<Void> showCase(Activity activity, String title, String message, View target) {
        return showCase(activity, title, message, target, false);
    }

    /**
     * Display a tooltip for a given View.
     *
     * @param activity
     * @param title
     * @param message
     * @param target
     * @return
     * @since 0.6.0
     */
    public static Task<Void> showCase(
            Activity activity,
            String title,
            String message,
            View target,
            boolean buttonOnLeft
    ) {
        final Task<Void>.TaskCompletionSource tcs = Task.create();

        ShowcaseView showcaseView = new ShowcaseView.Builder(activity)
                .setTarget(new ViewTarget(target))
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(R.style.ShowcaseTheme)
                .hideOnTouchOutside()
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        tcs.setResult(null);
                    }

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {
                    }
                })
                .build();

        // fix problem of button under navigation bar on Lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Resources resources = activity.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            int height = (resourceId > 0)
                ? resources.getDimensionPixelSize(resourceId)
                : 0;
            showcaseView.setPadding(0, 0, 0, height);
        }

        if (buttonOnLeft) {
            Resources r = activity.getResources();
            int margin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    16,
                    r.getDisplayMetrics()
            );
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.setMargins(margin, 0, 0, margin);

            showcaseView.setButtonPosition(layoutParams);
        }

        return tcs.getTask();
    }

    /**
     *
     * @param activity
     * @param title
     * @param message
     * @param target
     * @return
     * @since 0.6.0
     */
    public static Task<Void> showCase(Activity activity, String title, String message, Point target) {
        final Task<Void>.TaskCompletionSource tcs = Task.create();

        new ShowcaseView.Builder(activity)
                .setTarget(new PointTarget(target))
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(R.style.ShowcaseTheme)
                .hideOnTouchOutside()
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) { }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        tcs.setResult(null);
                    }

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) { }
                })
                .build();

        return tcs.getTask();
    }

}
