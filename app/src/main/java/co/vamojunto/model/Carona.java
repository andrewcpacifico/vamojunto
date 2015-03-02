/*
 * Copyright (c) 2015. Vamo Junto Ltda. Todos os direitos reservados.
 *
 * Este software contém informações confidenciais e de propriedade da Vamo Junto Ltda,
 * ("Informações Confidenciais"). Você não deve divulgar tais informações, e deve usá-las somente em
 * conformidade com os termos do contrato de licença estabelecido entre você e a Vamo Junto Ltda.
 *
 * See LICENSE.txt
 */

package co.vamojunto.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import bolts.Task;

/**
 * Modelo de uma carona no sistema.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
@ParseClassName("Carona")
public class Carona extends ParseObject implements Parcelable {

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

    public Carona() {}

    public Carona(Calendar dataHora, Usuario motorista, int numLugares,
                  String detalhes, Place origem, Place destino) {
        setDataHora(dataHora);
        setMotorista(motorista);
        setNumLugares(numLugares);
        setDetalhes(detalhes);
        setOrigem(origem);
        setDestino(destino);
    }

    public String getId() {
        return getObjectId();
    }

    public Calendar getDataHora() {
        Calendar c = Calendar.getInstance();
        c.setTime(getDate(FIELD_DATA_HORA));

        return c;
    }

    public void setDataHora(Calendar dataHora) {
        put(FIELD_DATA_HORA, dataHora.getTime());
    }

    public Usuario getMotorista() {
        return (Usuario) getParseUser(FIELD_MOTORISTA);
    }

    public void setMotorista(Usuario motorista) {
        put(FIELD_MOTORISTA, motorista);
    }

    public int getNumLugares() {
        return getInt(FIELD_NUM_LUGARES);
    }

    public void setNumLugares(int numLugares) {
        put(FIELD_NUM_LUGARES, numLugares);
    }

    public String getDetalhes() {
        return getString(FIELD_DETALHES);
    }

    public void setDetalhes(String detalhes) {
        put(FIELD_DETALHES, detalhes);
    }

    public Place getOrigem() {
        Place p = new Place(getDouble(FIELD_ORIGEM_LAT), getDouble(FIELD_ORIGEM_LNG));
        p.setTitulo(getString(FIELD_ORIGEM_TITULO));

        return p;
    }

    public void setOrigem(Place origem) {
        put(FIELD_ORIGEM_LAT, origem.getLatitude());
        put(FIELD_ORIGEM_LNG, origem.getLongitude());
        put(FIELD_ORIGEM_TITULO, origem.getTitulo());
    }

    public Place getDestino() {
        Place p = new Place(getDouble(FIELD_DESTINO_LAT), getDouble(FIELD_DESTINO_LNG));
        p.setTitulo(getString(FIELD_DESTINO_TITULO));

        return p;
    }

    public void setDestino(Place destino) {
        put(FIELD_DESTINO_LAT, destino.getLatitude());
        put(FIELD_DESTINO_LNG, destino.getLongitude());
        put(FIELD_DESTINO_TITULO, destino.getTitulo());
    }


    /**
     * Recupera uma lista de caronas que têm como motorista um determinado usuário, ou seja,
     * recupera todas as ofertas de carona feitas por esse usuário.
     * @param u Usuário do qual deseja-se recuperar as ofertas de carona.
     * @return Uma {@link bolts.Task} que é finalizada após a busca pelos registros, caso tudo
     *         ocorra normalmente, a {@link bolts.Task} conterá a lista de caronas.
     */
    public static Task<List<Carona>> buscaPorMotoristaAsync(final Usuario u) {
        final Task<List<Carona>>.TaskCompletionSource tcs = Task.create();

        ParseQuery<Carona> query = ParseQuery.getQuery(Carona.class);
        query.whereEqualTo(FIELD_MOTORISTA, u);

        query.findInBackground(new FindCallback<Carona>() {
            @Override
            public void done(List<Carona> caronas, ParseException e) {
                if ( e == null ) {
                    tcs.setResult(caronas);
                } else {
                    tcs.setError(e);
                }
            }
        });

        return tcs.getTask();
    }

/***************************************************************************************************
 *
 * Transformando em um Parcelable object
 *
 ***************************************************************************************************/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getId());
        dest.writeSerializable(getDataHora());
        dest.writeParcelable(getDestino(), flags);
        dest.writeString(getDetalhes());
        dest.writeParcelable(getMotorista(), flags);
        dest.writeInt(getNumLugares());
        dest.writeParcelable(getOrigem(), flags);
    }

    public static final Parcelable.Creator<Carona> CREATOR = new Parcelable.Creator<Carona>() {
        public Carona createFromParcel(Parcel in) {
            Carona c = ParseObject.createWithoutData(Carona.class, in.readString());

            c.setDataHora((Calendar) in.readSerializable());
            c.setDestino((Place) in.readParcelable(Place.class.getClassLoader()));
            c.setDetalhes(in.readString());
            c.setMotorista((Usuario) in.readParcelable(Usuario.class.getClassLoader()));
            c.setNumLugares(in.readInt());
            c.setOrigem((Place) in.readParcelable(Place.class.getClassLoader()));

            return c;
        }

        public Carona[] newArray(int size) {
            return new Carona[size];
        }
    };

    private Carona(Parcel in) {

    }
}
