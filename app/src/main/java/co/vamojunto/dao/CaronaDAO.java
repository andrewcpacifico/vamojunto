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

import com.parse.ParseObject;

import java.util.Calendar;

import co.vamojunto.model.Carona;
import co.vamojunto.util.Globals;

/**
 * Data Access Object para as caronas.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class CaronaDAO {

    private static final String TAG = Globals.PACKAGE + "CaronaDAO";
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

        Calendar data_hora = c.getData();
        data_hora.set(Calendar.HOUR_OF_DAY, c.getHora().get(Calendar.HOUR_OF_DAY));
        data_hora.set(Calendar.MINUTE, c.getHora().get(Calendar.MINUTE));
        pObj.put(FIELD_DATA_HORA, data_hora.getTime());

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

}
