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

/**
 * Modelo de um local geográfico.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class Place implements Parcelable {
    public double NOT_SET_LAT = -9999;
    public double NOT_SET_LNG = -9999;

    private String mPlaceId;
    private String mTitulo;
    private String mEndereco;
    private double mLatitude;
    private double mLongitude;

    public Place(String titulo, String endereco, String placeId) {
        this.mTitulo = titulo;
        this.mEndereco = endereco;
        this.mPlaceId = placeId;
        this.mLatitude = NOT_SET_LAT;
        this.mLongitude = NOT_SET_LNG;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        this.mLongitude = longitude;
    }

    public String getPlaceId() {
        return mPlaceId;
    }

    public String getTitulo() {
        return mTitulo;
    }

    public void setTitulo(String titulo) {
        this.mTitulo = titulo;
    }

    public String getEndereco() {
        return mEndereco;
    }

    public void setEndereco(String mEndereco) {
        this.mEndereco = mEndereco;
    }

    @Override
    public String toString() {
        return mTitulo + ", " + mEndereco;
    }

/***************************************************************************************************
 *
 * Transformando em um Parcelable object
 *
 **************************************************************************************************/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPlaceId);
        dest.writeString(mTitulo);
        dest.writeString(mEndereco);
    }

    public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    private Place(Parcel in) {
        mPlaceId = in.readString();
        mTitulo = in.readString();
        mEndereco = in.readString();
    }
}