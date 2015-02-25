package co.vamojunto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.adapters.SearchPlaceAdapter;
import co.vamojunto.helpers.GooglePlacesHelper;
import co.vamojunto.model.Place;
import co.vamojunto.widgets.OnRecyclerViewItemClickListener;

/**
 * Tela onde o usuário pode buscar por um determinado local. A busca é feita utilizando a Places API
 * do Google, os resultados retornados são exibidos em uma lista, onde o usuário pode selecionar um local.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class SearchPlaceActivity extends ActionBarActivity {

    public static final int REQUEST_CODE = 1234;

    public static final String PACKAGE = "co.vamojunto.SearchPlaceActivity";
    public static final String PLACE = PACKAGE + ".place";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_place);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SearchPlaceFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class SearchPlaceFragment extends Fragment {
        private static final String TAG = "SearchPlaceFragment";

        private RecyclerView mRecyclerView;
        private SearchPlaceAdapter mAdapter;
        private RecyclerView.LayoutManager mLayoutManager;
        private GooglePlacesHelper mGooglePlacesHelper;

        private ImageButton mBtnClear;
        private EditText mLocalEditText;
        private ProgressBar mProgressBar;
        private ImageView mSearchIcon;

        /** Evita que sejam realizadas consultas em paralelo */
        private static boolean isSearching = false;

        /** Armazena o último valor buscado pelo usuário, é utilizado para ao final da consulta,
         * verificar se o valor na caixa de texto ainda é o mesmo, caso não seja, uma nova busca é
         * realizada. */
        private String mUltimaBusca;

        public SearchPlaceFragment() {
            mUltimaBusca = "";
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_search_place, container, false);

            initComponents(rootView);

            return rootView;
        }

        /**
         * Inicializa os componentes da tela.
         *
         * @param rootView View correspondente ao layout do fragment.
         */
        private void initComponents(View rootView) {
            // Exibe o teclado automaticamente.
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            mLayoutManager = new LinearLayoutManager(rootView.getContext());
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            mAdapter = new SearchPlaceAdapter(rootView.getContext(), null, new OnRecyclerViewItemClickListener() {
                @Override
                public void OnItemClicked(View v, int position) {
                    Place p = mAdapter.getItem(position);

                    if ( p != null ) {
                        Intent intent = new Intent();
                        intent.putExtra(PLACE, p);

                        getActivity().setResult(Activity.RESULT_OK, intent);
                        getActivity().finish();
                    }
                }
            });
            mRecyclerView.setAdapter(mAdapter);

            mGooglePlacesHelper = new GooglePlacesHelper(rootView.getContext());

            mLocalEditText = (EditText) rootView.findViewById(R.id.local_edit_text);
            mLocalEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(final CharSequence s, int start, int before, int count) {
                    localEditTextOnTextChanged(s);
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });

            mBtnClear = (ImageButton) rootView.findViewById(R.id.clear_button);
            mBtnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLocalEditText.setText("");
                }
            });

            mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
            mSearchIcon = (ImageView) rootView.findViewById(R.id.img_search);
        }

        /**
         * Evento executado quando o valor da caixa de pesquisa é alterado pelo usuário.
         *
         * @param s Texto atualizado.
         */
        private void localEditTextOnTextChanged(final CharSequence s) {
            Log.d(TAG, s.toString());

            // Oculta o botão para limpar o EditText
            if ( s.length() == 0)
                mBtnClear.setVisibility(View.GONE);
            else
                mBtnClear.setVisibility(View.VISIBLE);

            buscaLocal(s.toString());
        }

        /**
         * Chamado sempre que o usuário altera o conteúdo do campo de busca. Recebe como parâmetro
         * o conteúdo digitado pelo usuário, e repassa para o método que busca os locais utilizando
         * a API do Google Places.
         *
         * @param strBusca String digitada pelo usuário, representando a busca que ele deseja realizar.
         */
        private void buscaLocal(String strBusca) {
            // Faz a busca apenas se uma outra não estiver sendo realizada.
            if ( !isSearching ) {
                // Inicia a busca apenas após o usuário ter digitado pelo menos 3 caracteres.
                if (strBusca.length() > 2) {
                    // Bloqueia outras buscas, enquanto esta ainda não for finalizada.
                    isSearching = true;

                    // Caso a busca vá ser efetuada, armazena o valor que foi buscado para ser comparado
                    // com o valor do campo de busca no final
                    mUltimaBusca = strBusca;

                    // Exibe a ProgressBar no lugar do ícone de busca.
                    mSearchIcon.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);

                    // Faz uma chamada asíncrona à busca utilizando a Google Places API
                    mGooglePlacesHelper.autocompleteAsync(strBusca).
                            continueWith(new Continuation<List<Place>, Void>() {
                                @Override
                                public Void then(final Task<List<Place>> task) {
                                    // Desbloqueia a busca
                                    isSearching = false;

                                    // Utilizado para executar os trechos de código que devem roda ipreterivelmente na Thread principal
                                    Handler handler = new Handler(getActivity().getMainLooper());

                                    // Verifica se a tarefa foi executada com sucesso
                                    // TODO Exibir essa mensagem de erro na tela, e não apenas em um Toast
                                    if (task.isFaulted()) {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getActivity(), R.string.search_error, Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    } else if (!task.isCancelled()) {
                                        mAdapter.setDataset(task.getResult());

                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mAdapter.notifyDataSetChanged();
                                            }
                                        });
                                    }

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Restaura o ícone de busca
                                            mProgressBar.setVisibility(View.GONE);
                                            mSearchIcon.setVisibility(View.VISIBLE);

                                            // Verifica se o usuário alterou o valor do campo de busca enquanto
                                            // a última busca era processada. Caso tenha alterado, realiza
                                            // uma outra busca.
                                            if (!mLocalEditText.getText().toString().equals(mUltimaBusca)) {
                                                buscaLocal(mLocalEditText.getText().toString());
                                            }
                                        }
                                    });

                                    return null;
                                }
                            });
                }
            }
        }

    }
}
