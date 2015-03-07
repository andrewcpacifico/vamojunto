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


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.ui.activities.NewRideActivity;
import co.vamojunto.R;
import co.vamojunto.ui.adapters.ListaCaronasRecyclerViewAdapter;
import co.vamojunto.model.Ride;
import co.vamojunto.model.User;
import co.vamojunto.util.Globals;
import co.vamojunto.util.NetworkUtil;

/**
 * {@link android.support.v4.app.Fragment} to list all the rides that a user is participating, as
 * a driver or passenger.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class ListaCaronasFragment extends Fragment {

    private static final String TAG = "ListaCaronasFragment";

    // As constantes abaixo são utilizadas para identificar os views a serem carregados pwlo ViewSwitcher
    private static final int VIEW_PROGRESS = 0;
    private static final int VIEW_ERRO = 1;
    private static final int VIEW_PADRAO = 2;

    /**
     * RecyclerView onde são exibidos os registros das caronas.
     */
    private RecyclerView mOfertasRecyclerView;

    /**
     * LayoutManager utilizado pelo mOfertasRecyclerView
     */
    private LinearLayoutManager mOfertasLayoutManager;

    /**
     * Adapter utilizado para gerenciar os dados do mOfertasRecyclerView
     */
    private ListaCaronasRecyclerViewAdapter mOfertasAdapter;

    /**
     * ViewSwitcher utilizado para alterar entre a ProgressBar que é exibida enquanto as caronas
     * são carregadas, e a tela principal.
     */
    private ViewFlipper mViewFlipper;

    /**
     * The {@link android.widget.TextView} that displays a error message, on the error screen View
     */
    private TextView mErrorScreenMsgTextView;

    /**
     * The {@link android.widget.Button} used to retry an action that failed on error screen.
     */
    private Button mErrorScreenRetryButton;

    /**
     * Required default constructor
     */
    public ListaCaronasFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_lista_caronas, container, false);

        initComponents(rootView);

        loadMyRides();

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Globals.NEW_RIDE_ACTIVITY_REQUEST_CODE) {
                final Ride c = data.getParcelableExtra(NewRideActivity.RES_RIDE);

                // foi necessário utilizar um delay para adicionar o item à tela, para que o
                // recyclerview pudesse mostrar a animação, e posicionar no item novo
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addItem(c);
                    }
                }, 1000);

                Toast.makeText(getActivity(), getString(R.string.carona_cadastrada), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Initializates the screen components
     *
     * @param rootView The Fragment's inflated layout.
     */
    public void initComponents(View rootView) {
        mOfertasRecyclerView = (RecyclerView) rootView.findViewById(R.id.lista_caronas_recycler_view);

        mOfertasRecyclerView.setHasFixedSize(true);

        mOfertasLayoutManager = new LinearLayoutManager(rootView.getContext());
        mOfertasRecyclerView.setLayoutManager(mOfertasLayoutManager);

        mOfertasAdapter = new ListaCaronasRecyclerViewAdapter(getActivity(), new ArrayList<Ride>());
        mOfertasRecyclerView.setAdapter(mOfertasAdapter);

        Button btnOk = (Button) rootView.findViewById(R.id.btn_ok);
        btnOk.setText(getText(R.string.oferecer_carona));
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NewRideActivity.class);
                getParentFragment().startActivityForResult(intent, Globals.NEW_RIDE_ACTIVITY_REQUEST_CODE);
            }
        });

        mViewFlipper = (ViewFlipper) rootView.findViewById(R.id.flipper);

        // Looks for the error screen views
        mErrorScreenMsgTextView = (TextView) rootView.findViewById(R.id.error_screen_message_text_view);
        mErrorScreenRetryButton = (Button) rootView.findViewById(R.id.error_screen_retry_button);
        mErrorScreenRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMyRides();
            }
        });
    }

    /**
     * Adiciona um registro de carona à tela, é chamado após o cadastro de uma nova carona, para que
     * não seja necessário recarregar todos os dados da nuvem.
     *
     * @param c Ride a ser adicionada à interface.
     */
    private void addItem(Ride c) {
        mOfertasAdapter.addItem(c);

        // Após a adição do item, rola o recyclerview para o início, para que o usuário possa visualizar
        // o registro inserido.
        if (mOfertasLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
            mOfertasLayoutManager.scrollToPosition(0);
        }
    }

    /**
     * Carrega as ofertas de caronas do usuário. Ao final do procedimento, a lista de ofertas de
     * caronas de usuário é definida como dataset do {@link android.support.v7.widget.RecyclerView}
     * da aba de ofertas de caronas.
     */
    public void loadMyRides() {
        mViewFlipper.setDisplayedChild(VIEW_PROGRESS);

        // Loads the rides from the cloud, only if the user is connected to the Internet
        if (NetworkUtil.isConnected(getActivity())) {
            Ride.getByDriverInBackground((User) User.getCurrentUser()).continueWith(new Continuation<List<Ride>, Void>() {
                @Override
                public Void then(Task<List<Ride>> task) throws Exception {
                    mViewFlipper.setDisplayedChild(VIEW_PADRAO);

                    if (!task.isFaulted() && !task.isCancelled()) {
                        List<Ride> lstRides = task.getResult();
                        Collections.sort(lstRides, new Comparator<Ride>() {
                            @Override
                            public int compare(Ride lhs, Ride rhs) {
                                return rhs.getCreatedAt().compareTo(lhs.getCreatedAt());
                            }
                        });

                        mOfertasAdapter.setDataset(lstRides);
                    } else {
                        Log.e(TAG, task.getError().getMessage());

                        displayErrorScreen();
                    }

                    return null;
                }
            });
        } else {
            displayErrorScreen(getString(R.string.erro_msg_no_internet_connection));
        }
    }

    /**
     * Switches the viewFlipper to display the error screen. and customizes the error message.
     *
     * @param errorMsg The message displayed on the screen, if the param value is null, the default
     *                 error message is used.
     */
    private void displayErrorScreen(String errorMsg) {
        if (errorMsg == null)
            mErrorScreenMsgTextView.setText(getString(R.string.default_error_screen_message));
        else
            mErrorScreenMsgTextView.setText(errorMsg);

        mViewFlipper.setDisplayedChild(VIEW_ERRO);
    }

    /**
     * Switches the viewFliper to display the error screen using the default error screen message.
     */
    private void displayErrorScreen() {
        mErrorScreenMsgTextView.setText(getString(R.string.default_error_screen_message));

        mViewFlipper.setDisplayedChild(VIEW_ERRO);
    }
}
