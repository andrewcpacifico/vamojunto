/*
 * Copyright (c) 2015. Vamo Junto Ltda. Todos os direitos reservados.
 *
 * Este software contém informações confidenciais e de propriedade da Vamo Junto Ltda,
 * ("Informações Confidenciais"). Você não deve divulgar tais informações, e deve usá-las somente em
 * conformidade com os termos do contrato de licença estabelecido entre você e a Vamo Junto Ltda.
 *
 * See LICENSE.txt
 */

package co.vamojunto.fragment;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.NovaOfertaCaronaActivity;
import co.vamojunto.R;
import co.vamojunto.adapters.ListaCaronasRecyclerViewAdapter;
import co.vamojunto.model.Carona;
import co.vamojunto.model.Usuario;
import co.vamojunto.util.Globals;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListaCaronasFragment extends Fragment {

    private RecyclerView mOfertasRecyclerView;
    private LinearLayoutManager mOfertasLayoutManager;
    private ListaCaronasRecyclerViewAdapter mOfertasAdapter;

    private Button mBtnOk;
    private ProgressDialog mProDialog;

    public ListaCaronasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_lista_caronas, container, false);

        initComponents(rootView);
        carregaMinhasCaronas();

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Globals.NOVA_CARONA_ACTIVITY_REQUEST_CODE) {
                Carona c = data.getParcelableExtra(NovaOfertaCaronaActivity.RES_CARONA);
            }
        }
    }

    /**
     * Exibe um diálogo indicando que a tela principal está sendo carregada.
     */
    private void startLoading() {
        mProDialog = new ProgressDialog(getActivity());
        mProDialog.setMessage(getString(R.string.loading));
        mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProDialog.setCancelable(false);
        mProDialog.show();
    }

    /**
     * Finaliza o diálogo do carregamento da tela principal.
     */
    private void stopLoading() {
        mProDialog.dismiss();
        mProDialog = null;
    }

    public void initComponents(View rootView) {
        mOfertasRecyclerView = (RecyclerView) rootView.findViewById(R.id.lista_caronas_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mOfertasRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mOfertasLayoutManager = new LinearLayoutManager(rootView.getContext());
        mOfertasRecyclerView.setLayoutManager(mOfertasLayoutManager);

        // specify an adapter (see also next example)
        mOfertasAdapter = new ListaCaronasRecyclerViewAdapter(getActivity(), new ArrayList<Carona>());
        mOfertasRecyclerView.setAdapter(mOfertasAdapter);

        Button btnOk = (Button) rootView.findViewById(R.id.btn_ok);
        btnOk.setText(getText(R.string.oferecer_carona));
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NovaOfertaCaronaActivity.class);
                getParentFragment().startActivityForResult(intent, Globals.NOVA_CARONA_ACTIVITY_REQUEST_CODE);
            }
        });
    }

    /**
     * Carrega as ofertas de caronas do usuário. Ao final do procedimento, a lista de ofertas de
     * caronas de usuário é definida como dataset do {@link android.support.v7.widget.RecyclerView}
     * da aba de ofertas de caronas.
     */
    public void carregaMinhasCaronas() {
        startLoading();

        Carona.buscaPorMotoristaAsync((Usuario) Usuario.getCurrentUser()).continueWith(new Continuation<List<Carona>, Void>() {
            @Override
            public Void then(Task<List<Carona>> task) throws Exception {
                stopLoading();

                if ( !task.isFaulted() && !task.isCancelled()) {
                    List<Carona> lstCaronas = task.getResult();
                    mOfertasAdapter.setDataset(lstCaronas);
                    mOfertasRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            mOfertasAdapter.notifyDataSetChanged();
                        }
                    });
                }

                return null;
            }
        });
    }

}
