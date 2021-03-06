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

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Classe utilitária com funções relacionadas a números
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class NumberUtil {

    public static double truncate(double num, int numCasasDecimais) {
        StringBuilder sbFormato = new StringBuilder("##.#");
        for (int i = 1; i < numCasasDecimais; i++) {
            sbFormato.append("#");
        }

        DecimalFormat df = new DecimalFormat(sbFormato.toString());
        df.setRoundingMode(RoundingMode.DOWN);

        return Double.parseDouble(df.format(num).replace(",", ""));
    }

}
