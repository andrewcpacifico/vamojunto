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
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.NovaOfertaCaronaActivity;
import co.vamojunto.R;
import co.vamojunto.adapters.ListaCaronasRecyclerViewAdapter;
import co.vamojunto.model.Carona;
import co.vamojunto.model.Usuario;
import co.vamojunto.util.Globals;
import co.vamojunto.util.NetworkUtil;

/**
 * Fragment para listagem das caronas em que um determinado usuário participa, como motorista ou
 * passageiro.
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
     * Construtor padrão obrigatório
     */
    public ListaCaronasFragment() { }

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
                final Carona c = data.getParcelableExtra(NovaOfertaCaronaActivity.RES_CARONA);

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
     * Inicializa os componentes da tela.
     *
     * @param rootView Layout inflado do fragment.
     */
    public void initComponents(View rootView) {
        mOfertasRecyclerView = (RecyclerView) rootView.findViewById(R.id.lista_caronas_recycler_view);

        mOfertasRecyclerView.setHasFixedSize(true);

        mOfertasLayoutManager = new LinearLayoutManager(rootView.getContext());
        mOfertasRecyclerView.setLayoutManager(mOfertasLayoutManager);

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

        Button btnRetry = (Button) rootView.findViewById(R.id.btn_retry);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carregaMinhasCaronas();
            }
        });

        mViewFlipper = (ViewFlipper) rootView.findViewById(R.id.switcher);
    }

    /**
     * Adiciona um registro de carona à tela, é chamado após o cadastro de uma nova carona, para que
     * não seja necessário recarregar todos os dados da nuvem.
     *
     * @param c Carona a ser adicionada à interface.
     */
    private void addItem(Carona c) {
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
    public void carregaMinhasCaronas() {
        mViewFlipper.setDisplayedChild(VIEW_PROGRESS);

        // Tenta carregar as caronas da nuvem, apenas se o usuário estiver conectado à Internet
        if (NetworkUtil.isConnected(getActivity())) {
            Carona.buscaPorMotoristaAsync((Usuario) Usuario.getCurrentUser()).continueWith(new Continuation<List<Carona>, Void>() {
                @Override
                public Void then(Task<List<Carona>> task) throws Exception {
                    mViewFlipper.setDisplayedChild(VIEW_PADRAO);

                    if (!task.isFaulted() && !task.isCancelled()) {
                        List<Carona> lstCaronas = task.getResult();
                        Collections.sort(lstCaronas, new Comparator<Carona>() {
                            @Override
                            public int compare(Carona lhs, Carona rhs) {
                                return rhs.getCreatedAt().compareTo(lhs.getCreatedAt());
                            }
                        });

                        mOfertasAdapter.setDataset(lstCaronas);
                    } else {
                        Log.e(TAG, task.getError().getMessage());

                        mViewFlipper.setDisplayedChild(VIEW_ERRO);
                    }

                    return null;
                }
            });
        } else {
            mViewFlipper.setDisplayedChild(VIEW_ERRO);
        }
    }

}
