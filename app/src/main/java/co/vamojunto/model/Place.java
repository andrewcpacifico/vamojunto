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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.security.Policy;

import co.vamojunto.util.NumberUtil;

/**
 * Modelo de um local geográfico.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class Place implements Parcelable {
    public double NOT_SET_LAT = -9999;
    public double NOT_SET_LNG = -9999;

    private String mGooglePlaceId;
    private String mTitulo;
    private String mEndereco;
    private double mLatitude;
    private double mLongitude;

    public Place(double lat, double lng) {
        this.mTitulo = "";
        this.mEndereco = "";
        this.mGooglePlaceId = null;
        this.mLatitude = lat;
        this.mLongitude = lng;
    }

    public Place(String titulo, String endereco, String placeId) {
        this.mTitulo = titulo;
        this.mEndereco = endereco;
        this.mGooglePlaceId = placeId;
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

    public String getGooglePlaceId() {
        return mGooglePlaceId;
    }

    public void setGooglePlaceId(String googlePlaceId) {
        this.mGooglePlaceId = googlePlaceId;
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

    public boolean isGooglePlace() {
        return this.mGooglePlaceId != null;
    }

    public boolean hasCoord() {
        return this.mLatitude != NOT_SET_LAT && this.mLongitude != NOT_SET_LNG;
    }

    @Override
    public String toString() {
        return mTitulo + ", " + mEndereco;
    }


    @Override
    public boolean equals(Object o) {
        if ( o == null )
            return false;

        if ( o.getClass() == Place.class ) {
            Place p = (Place) o;

            if (this.isGooglePlace() && p.isGooglePlace()) {
                return this.mGooglePlaceId.equals(p.mGooglePlaceId);
            } else {
                // Trunca as coordenadas para apenas 5 casas decimais para poder fazer a comparação com as
                // coordenadas do local retornado pela Google Places API
                double lat = NumberUtil.truncate(mLatitude, 5);
                double lng = NumberUtil.truncate(mLongitude, 5);

                double pLat = NumberUtil.truncate(p.mLatitude, 5);
                double pLng = NumberUtil.truncate(p.mLongitude, 5);

                return lat == pLat && lng == pLng;
            }
        }

        return false;
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
        dest.writeString(mGooglePlaceId);
        dest.writeString(mTitulo);
        dest.writeString(mEndereco);
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
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
        mGooglePlaceId = in.readString();
        mTitulo = in.readString();
        mEndereco = in.readString();
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
    }
}