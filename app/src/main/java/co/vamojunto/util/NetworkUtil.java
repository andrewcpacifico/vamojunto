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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Contém funções utilitárias que envolvem manipulação de rede
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.01
 */
public class NetworkUtil {

    /**
     * Verifica se o usuário está conectado à Internet
     *
     * @return <code>true</code> caso esteja conectado e <code>false</code> caso contrário.
     */
    public static boolean isConnected(Context c) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
