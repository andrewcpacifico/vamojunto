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

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import co.vamojunto.R;

/**
 * Utility class with date related methods
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class DateUtil {

    /**
     * Formats a datetime in the default format.
     *
     * @param c The Context where this method is needed.
     * @param d The datetime to format.
     * @return A String with formatted datetime using the application's default format.
     */
    public static String getFormattedDateTime(Context c, Calendar d) {
        String format = c.getString(R.string.default_datetime_format);
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);

        return dateFormat.format(d.getTime());
    }

}
