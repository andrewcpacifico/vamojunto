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


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.adapters.ListaCaronasRecyclerViewAdapter;
import co.vamojunto.dao.CaronaDAO;
import co.vamojunto.model.Carona;
import co.vamojunto.model.Usuario;
import co.vamojunto.view.SlidingTabLayout;

/**
 * Tela de visualização das caronas cadastradas pelo usuário, tanto os pedidos, quanto as ofertas.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class MinhasCaronasFragment extends Fragment {

    private static final String TAG = "MinhasCaronasFragment";
    private ProgressDialog mProDialog;

    public MinhasCaronasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_minhas_caronas, container, false);

        // Assigning ViewPager View and setting the adapter
        ViewPager pager = (ViewPager) rootView.findViewById(R.id.pager);
        pager.setAdapter(new MyPagerAdapter());

        // Assiging the Sliding Tab Layout View
        SlidingTabLayout tabs = (SlidingTabLayout) rootView.findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.ColorPrimaryDark);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        return rootView;
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

    /**
     * Adapter utilizado para preencher as páginas exibidas em cada uma das abas do Fragment.
     * São exibidas duas páginas, uma contendo as ofertas de caronas do usuário, e outra contendo
     * os pedidos.
     *
     * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
     * @since 0.1.0
     */
    public class MyPagerAdapter extends PagerAdapter {
        private RecyclerView mOfertasRecyclerView;
        private LinearLayoutManager mOfertasLayoutManager;
        private ListaCaronasRecyclerViewAdapter mOfertasAdapter;

        private RecyclerView mPedidosRecyclerView;
        private LinearLayoutManager mPedidosLayoutManager;
        private ListaCaronasRecyclerViewAdapter mPedidosAdapter;

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if ( position == 0 )
                return "Motorista";

            return "Passageiro";
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if ( position == 0 )
                return getPageOfertas(container);

            return getPagePedidos(container);
        }

        /**
         * Carrega a view da aba de ofertas de caronas do usuário.
         */
        public View getPageOfertas(ViewGroup container) {
            // Inflate a new layout from our resources
            View rootView = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.layout_lista_caronas, container, false);
            // Add the newly created View to the ViewPager
            container.addView(rootView);

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

            carregaMinhasCaronas();

            // Return the View
            return rootView;
        }

        /**
         * Carrega a view da aba de pedidos de caronas do usuário.
         */
        public View getPagePedidos(ViewGroup container) {
            // Inflate a new layout from our resources
            View rootView = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.layout_lista_caronas, container, false);
            // Add the newly created View to the ViewPager
            container.addView(rootView);

            mPedidosRecyclerView = (RecyclerView) rootView.findViewById(R.id.lista_caronas_recycler_view);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mPedidosRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            mPedidosLayoutManager = new LinearLayoutManager(rootView.getContext());
            mPedidosRecyclerView.setLayoutManager(mPedidosLayoutManager);

            // specify an adapter (see also next example)
            mPedidosAdapter = new ListaCaronasRecyclerViewAdapter(getActivity(), new ArrayList<Carona>());
            mPedidosRecyclerView.setAdapter(mPedidosAdapter);

            // Return the View
            return rootView;
        }

        /**
         * Carrega as ofertas de caronas do usuário. Ao final do procedimento, a lista de ofertas de
         * caronas de usuário é definida como dataset do {@link android.support.v7.widget.RecyclerView}
         * da aba de ofertas de caronas.
         */
        public void carregaMinhasCaronas() {
            startLoading();

            CaronaDAO dao = new CaronaDAO();
            dao.buscaPorMotoristaAsync(Usuario.getCurrentUser()).continueWith(new Continuation<List<Carona>, Void>() {
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
}
