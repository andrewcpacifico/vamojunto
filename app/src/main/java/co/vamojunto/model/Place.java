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

package co.vamojunto.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.security.Policy;
import java.util.HashMap;
import java.util.Map;

import co.vamojunto.util.NumberUtil;

/**
 * Modelo de um local geográfico.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class Place {
    public double NOT_SET_LAT = -9999;
    public double NOT_SET_LNG = -9999;

    private String mGooglePlaceId;
    private String mTitulo;
    private String mEndereco;
    private double mLatitude;
    private double mLongitude;

    private static Map<String, Place> instances = new HashMap<String, Place>();

    public static void storeInstance(String key, Place value) {
        instances.put(key, value);
    }

    public static Place getStoredInstance(String key) {
        Place p = instances.get(key);
        instances.remove(key);

        return p;
    }

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

}