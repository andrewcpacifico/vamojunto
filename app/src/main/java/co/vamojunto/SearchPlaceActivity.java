package co.vamojunto;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

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

        public SearchPlaceFragment() {
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

            // Faz a busca apenas se uma outra não estiver sendo realizada.
            if ( !isSearching ) {
                // Inicia a busca apenas após o usuário ter digitado pelo menos 3 caracteres.
                if (s.length() > 2) {
                    isSearching = true;

                    mSearchIcon.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);

                    mGooglePlacesHelper.autocompleteAsync(s.toString()).
                            continueWith(new Continuation<List<Place>, Void>() {
                                @Override
                                public Void then(final Task<List<Place>> task) {
                                    mAdapter.setDataset(task.getResult());

                                    mRecyclerView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mAdapter.notifyDataSetChanged();

                                            mProgressBar.setVisibility(View.GONE);
                                            mSearchIcon.setVisibility(View.VISIBLE);
                                        }
                                    });

                                    isSearching = false;
                                    return null;
                                }
                            });
                }
            }
        }

    }
}
