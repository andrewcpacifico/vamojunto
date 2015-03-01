/*
 * Copyright (c) 2015. Vamo Junto Ltda. Todos os direitos reservados.
 *
 * Este software contém informações confidenciais e de propriedade da Vamo Junto Ltda,
 * ("Informações Confidenciais"). Você não deve divulgar tais informações, e deve usá-las somente em
 * conformidade com os termos do contrato de licença estabelecido entre você e a Vamo Junto Ltda.
 *
 * See LICENSE.txt
 */

package co.vamojunto.dao;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.model.Carona;
import co.vamojunto.model.Place;
import co.vamojunto.util.Globals;

/**
 * Data Access Object para as caronas.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class CaronaDAO {

    private static final String TAG = "CaronaDAO";
    private static final String CLASS_NAME = "Carona";

    private static final String FIELD_DATA_HORA = "data_hora";
    private static final String FIELD_MOTORISTA = "motorista";
    private static final String FIELD_ORIGEM_LAT = "origem_lat";
    private static final String FIELD_ORIGEM_LNG = "origem_lng";
    private static final String FIELD_ORIGEM_TITULO = "origem_titulo";
    private static final String FIELD_DESTINO_LAT = "destino_lat";
    private static final String FIELD_DESTINO_LNG = "destino_lng";
    private static final String FIELD_DESTINO_TITULO = "destino_titulo";
    private static final String FIELD_NUM_LUGARES = "num_lugares";
    private static final String FIELD_DETALHES = "detalhes";

    /**
     * Salva um novo registro de carona no BD
     * @param c Instância da carona que deseja-se salvar.
     */
    public void novo(Carona c) {
        ParseObject pObj = new ParseObject(CLASS_NAME);

        pObj.put(FIELD_DATA_HORA, c.getDataHora().getTime());

        pObj.put(FIELD_MOTORISTA, c.getMotorista());

        pObj.put(FIELD_ORIGEM_LAT, c.getOrigem().getLatitude());
        pObj.put(FIELD_ORIGEM_LNG, c.getOrigem().getLongitude());
        pObj.put(FIELD_ORIGEM_TITULO, c.getOrigem().getTitulo());

        pObj.put(FIELD_DESTINO_LAT, c.getDestino().getLatitude());
        pObj.put(FIELD_DESTINO_LNG, c.getDestino().getLongitude());
        pObj.put(FIELD_DESTINO_TITULO, c.getDestino().getTitulo());

        pObj.put(FIELD_NUM_LUGARES, c.getNumLugares());

        pObj.put(FIELD_DETALHES, c.getDetalhes());

        pObj.saveInBackground();
    }

    /**
     * Recupera uma lista de caronas que têm como motorista um determinado usuário, ou seja,
     * recupera todas as ofertas de carona feitas por esse usuário.
     * @param u Usuário do qual deseja-se recuperar as ofertas de carona.
     * @return Uma {@link bolts.Task} que é finalizada após a busca pelos registros, caso tudo
     *         ocorra normalmente, a {@link bolts.Task} conterá a lista de caronas.
     */
    public Task<List<Carona>> buscaPorMotoristaAsync(final ParseUser u) {
        final Task<List<Carona>>.TaskCompletionSource tcs = Task.create();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(CLASS_NAME);
        query.whereEqualTo(FIELD_MOTORISTA, u);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if ( e == null ) {
                    List<Carona> lstRes = new ArrayList<Carona>();

                    for (ParseObject parseCarona : parseObjects) {
                        Calendar dataHora = Calendar.getInstance();
                        dataHora.setTime(parseCarona.getDate(FIELD_DATA_HORA));

                        Place origem = new Place(parseCarona.getDouble(FIELD_ORIGEM_LAT), parseCarona.getDouble(FIELD_ORIGEM_LNG));
                        Place destino = new Place(parseCarona.getDouble(FIELD_DESTINO_LAT), parseCarona.getDouble(FIELD_DESTINO_LNG));

                        lstRes.add(new Carona(parseCarona.getObjectId(), dataHora, u, parseCarona.getInt(FIELD_NUM_LUGARES),
                                parseCarona.getString(FIELD_DETALHES), origem, destino));
                    }

                    tcs.setResult(lstRes);
                } else {
                    tcs.setError(e);
                }
            }
        });

        return tcs.getTask();
    }

}