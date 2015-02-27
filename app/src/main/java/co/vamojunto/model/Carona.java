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

import com.parse.ParseUser;

import java.util.Calendar;

/**
 * Modelo de uma carona no sistema.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class Carona {

    private String mId;
    private Calendar mData;
    private Calendar mHora;
    private ParseUser mMotorista;
    private int mNumLugares;
    private String mDetalhes;
    private Place mOrigem;
    private Place mDestino;

    public Carona(Calendar data, Calendar hora, ParseUser motorista, int numLugares,
                  String detalhes, Place origem, Place destino) {
        this.mData = data;
        this.mHora = hora;
        this.mMotorista = motorista;
        this.mNumLugares = numLugares;
        this.mDetalhes = detalhes;
        this.mOrigem = origem;
        this.mDestino = destino;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public Calendar getData() {
        return mData;
    }

    public void setData(Calendar data) {
        this.mData = data;
    }

    public Calendar getHora() {
        return mHora;
    }

    public void setHora(Calendar hora) {
        this.mHora = hora;
    }

    public ParseUser getMotorista() {
        return mMotorista;
    }

    public void setMotorista(ParseUser motorista) {
        this.mMotorista = motorista;
    }

    public int getNumLugares() {
        return mNumLugares;
    }

    public void setNumLugares(int numLugares) {
        this.mNumLugares = numLugares;
    }

    public String getDetalhes() {
        return mDetalhes;
    }

    public void setDetalhes(String detalhes) {
        this.mDetalhes = detalhes;
    }

    public Place getOrigem() {
        return mOrigem;
    }

    public void setOrigem(Place origem) {
        this.mOrigem = origem;
    }

    public Place getDestino() {
        return mDestino;
    }

    public void setDestino(Place destino) {
        this.mDestino = destino;
    }
}