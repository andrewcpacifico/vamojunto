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

package co.vamojunto.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import co.vamojunto.R;

/**
 * The main {@link android.support.v4.app.Fragment} for the {@link co.vamojunto.ui.activities.NewRideRequestActivity}
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class NewRideRequestFragment extends Fragment {

    /** Flag utilizada para indicar se o campo origem está sendo editado. A flag é utilizada para
     * identificar para onde irá o resultado após a escolha de uma coordenada pelo usuário. */

    /**
     * Flag that indicates whether the starting point field is being edited.
     */
    private boolean mEditandoOrigem;

    /** Flag utilizada para indicar se o campo destino está sendo editado. A flag é utilizada para
     * identificar para onde irá o resultado após a escolha de uma coordenada pelo usuário. */
    private boolean mEditandoDestino;


    public NewRideRequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_ride_request, container, false);
        return rootView;
    }

}
