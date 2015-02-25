/*
 * Copyright (c) 2015. Vamo Junto Ltda. Todos os direitos reservados.
 *
 * Este software contém informações confidenciais e de propriedade da Vamo Junto Ltda,
 * ("Informações Confidenciais"). Você não deve divulgar tais informações, e deve usá-las somente em
 * conformidade com os termos do contrato de licença estabelecido entre você e a Vamo Junto Ltda.
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
        for ( int i = 1; i < numCasasDecimais; i++ ) {
            sbFormato.append("#");
        }

        DecimalFormat df = new DecimalFormat(sbFormato.toString());
        df.setRoundingMode(RoundingMode.DOWN);

        return Double.parseDouble(df.format(num));
    }

}
