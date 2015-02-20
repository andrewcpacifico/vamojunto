/*
 * Copyright (c) 2015. Vamo Junto Ltda. Todos os direitos reservados.
 *
 * Este software contém informações confidenciais e de propriedade da Vamo Junto Ltda,
 * ("Informações Confidenciais"). Você não deve divulgar tais informações, e deve usá-las somente em
 * conformidade com os termos do contrato de licença estabelecido entre você e a Vamo Junto Ltda.
 *
 * See LICENSE.txt
 */

package co.vamojunto.helpers;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/**
 * Classe contendo funções para auxiliar no processo de conversão de endereços para coordenadas
 * e vice-versa.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class GeocodingHelper {
    public static final String TAG = "GeocodingHelper";

    public static Task<Address> getEnderecoAsync(Context context, final LatLng latLng) {
        final Task<Address>.TaskCompletionSource tcs = Task.create();

        final Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        Task.callInBackground(new Callable<List<Address>>() {
            @Override
            public List<Address> call() throws IOException {
                return geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            }
        }).continueWith(new Continuation<List<Address>, Void>() {
            @Override
            public Void then(Task<List<Address>> task) throws IOException {
                List<Address> lstEndereco = task.getResult();

                if ( lstEndereco != null ) {
                    tcs.setResult(lstEndereco.get(0));
                }

                return null;
            }
        });

        return tcs.getTask();
    }
}
