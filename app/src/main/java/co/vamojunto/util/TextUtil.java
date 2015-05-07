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

import java.text.Normalizer;

/**
 * An utility class to work with text.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 1.0.0
 */
public class TextUtil {

    /**
     * Private Constructor to avoid class instancing.
     *
     * @since 0.1.0
     */
    private TextUtil() { /* empty constructor. DO NOT EDIT THIS CODE */ }

    /**
     * Normalizes an input string. The output string have no special characters, and spaces are
     * change by "-" character.
     *
     * @param input The input string.
     * @return The normalized output.
     * @since 0.1.0
     */
    public static String normalize(String input) {
        return Normalizer
                .normalize(input, Normalizer.Form.NFD)
                .trim()
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[^ a-zA-Z0-9]+","")
                .replaceAll("[\\s]+","-")
                .toLowerCase();
    }
}
