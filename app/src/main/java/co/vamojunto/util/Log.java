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

/**
 * Created by Andrew C. Pacifico <andrewcpacifico@gmail.com> on 21/06/15.
 */
public class Log {

    private static boolean production = false;

    public static void d(String tag, String msg) {
        if (! production)
            android.util.Log.d(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (! production)
            android.util.Log.i(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (! production)
            android.util.Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (! production)
            android.util.Log.e(tag, msg, tr);
    }

}
