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

package co.vamojunto.ui.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import co.vamojunto.R;

/**
 * Created by Andrew C. Pacifico <andrewcpacifico@gmail.com> on 16/06/15.
 */
public class ViewLocationActivity extends GetLocationActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.search_box).setVisibility(View.GONE);
        findViewById(R.id.pin_layout).setVisibility(View.GONE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        if (mPlace != null && mPlace.hasCoord()) {
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(mPlace.getLatitude(), mPlace.getLongitude()))
                    .title(mPlace.getTitulo()))
                    .showInfoWindow();
        }

        findViewById(R.id.ok_button).setVisibility(View.GONE);
    }

    @Override
    protected void initAppBar(Toolbar appBar) {
        super.initAppBar(appBar);

        if (mPlace != null) {
            appBar.setTitle(mPlace.getTitulo());
        } else {
            appBar.setTitle("");
        }

    }
}
