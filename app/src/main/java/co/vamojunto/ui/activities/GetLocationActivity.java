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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.helpers.GooglePlacesHelper;
import co.vamojunto.model.Place;
import co.vamojunto.ui.adapters.SearchPlaceAdapter;
import co.vamojunto.util.Globals;
import co.vamojunto.util.UIUtil;

/**
 * Screen where the user can search for a location, the main UI is a google maps Fragment, and if
 * the user clicks on the search box, another fragment is displayed, so the user can do a google
 * places search.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 * @version 2.0
 */
public class GetLocationActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback {

    private static final String TAG = "GetLocationActivity";

    public static final String RES_PLACE = "res_place";
    public static final String INITIAL_PLACE = "place";
    public static final String TITLE = "title";
    public static final String PIN_RES_ID = "pinId";
    public static final String BUTTON_MSG = "buttonMsg";

    public static final int GET_LOCATION_REQUEST_CODE = 3127;

    private SearchPlaceAdapter mAdapter;
    private GooglePlacesHelper mGooglePlacesHelper;

    private ImageButton mBtnClear;
    private ProgressBar mProgressBar;
    private ImageView mSearchIcon;

    /**
     * A {@link Place} instance with UFAM data. This place is used when the user clicks on the
     * fixed item on the search place screen.
     *
     * @since 0.5.0
     */
    private Place mUfamPlace;

    /**
     * Flag used to lock search triggered when the search box text is changed. This flag is used
     * when the user selects an item on the search results, that action sets the search box text
     * to selected place title, so there is no need for other search. This flag possibles that
     * behaviour.
     *
     * @since 0.5.0
     */
    private boolean mLockSearch = false;

    /**
     * Avoid multiple searches in parallel
     *
     * @since 0.5.0
     */
    private boolean isSearching = false;

    /**
     * Stores the last values searched, this value is compared to the text on the search box
     * when the search finishes, if the values are different, a new search are made.
     *
     * @since 0.5.0
     */
     private String mLastSearch;

    /**
     * Indicate which view is being displayed, the map view, or the search place view.
     *
     * @since 0.5.0
     */
    private boolean mMapVisible;

    /**
     * The instance of displayed map
     *
     * @since 0.1.0
     */
    protected GoogleMap mMap;

    /** Instância da Google Play Services API Client, utilizada para manipular o Location API */
    private GoogleApiClient mGoogleApiClient;

    /**
     * The user's last location.
     *
     * @since 0.1.0
     */
    private Location mLastLocation;

    /** Guarda uma instância de um local, caso o usuário tenha feito uma busca. */
    protected Place mPlace;

    /**
     * Handler to run code on main thread, inside callbacks.
     */
    private Handler mHandler;

    private ProgressDialog mProDialog;
    private EditText mSearchEditText;

    protected SupportMapFragment mMapFragment;
    private Toolbar mAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);

        mUfamPlace = new Place(-3.0904041362806165, -59.96392708271741);
        mUfamPlace.setEndereco("Av. Rodrigo Otávio, Coroado - Manaus, AM");
        mUfamPlace.setTitulo("UFAM - Univ. Federal do Amazonas");

        mHandler = new Handler();
        mPlace = null;
        mMapVisible = true;
        mLastSearch = "";
        initComponents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Salvando as configurações da posição do mapa.
        CameraPosition position = mMap.getCameraPosition();
        double lat = position.target.latitude;
        double lng = position.target.longitude;
        float zoom = position.zoom;

        SharedPreferences settings = getSharedPreferences(Globals.DEFAULT_PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(Globals.LAT_PREF_KEY, Double.doubleToRawLongBits(lat));
        editor.putLong(Globals.LNG_PREF_KEY, Double.doubleToRawLongBits(lng));
        editor.putFloat(Globals.ZOOM_PREF_KEY, zoom);
        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (! mMapVisible) {
            switchView();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Switch the screen default view between the map view, and the search for a place view.
     *
     * @since 0.5.0
     */
    private void switchView() {
        if (mMapVisible) {
            findViewById(R.id.list_places).setVisibility(View.VISIBLE);
            findViewById(R.id.pin_layout).setVisibility(View.GONE);
            findViewById(R.id.ok_button).setVisibility(View.GONE);
        } else {
            findViewById(R.id.list_places).setVisibility(View.GONE);
            findViewById(R.id.pin_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.ok_button).setVisibility(View.VISIBLE);
        }

        mMapVisible = ! mMapVisible;
    }

    /**
     * Positions the map on a given {@link Place}.
     *
     * @param p The place where the map have to be positioned.
     */
    private void changeMapPosition(Place p) {
        // helper to find the place coordinates
        GooglePlacesHelper placesHelper = new GooglePlacesHelper(this);

        // Utilizado para gerenciar a execução da tarefa de busca pelas coordenadas, e posicionamento
        // do mapa. Criando um TaskCompletionSource, é possível fornecer ao usuário uma opção para
        // cancelar a operação caso a conexão esteja muito lenta, evitando do app ficar travado
        // com a ProgressDialog.
        final Task<LatLng>.TaskCompletionSource tcs = Task.create();

        // show the loading dialog to user
        startLoading(getString(R.string.loadingmsg_positioning_map), tcs);

        if (! p.hasCoord()) {
            // gets the place coordinates
            placesHelper.getLocationAsync(p).continueWith(new Continuation<LatLng, Void>() {
                @Override
                public Void then(final Task<LatLng> task) {
                    if (task.isFaulted()) {
                        tcs.setError(task.getError());
                    } else if (task.isCancelled()) {
                        tcs.setCancelled();
                    } else {
                        tcs.setResult(task.getResult());
                    }

                    return null;
                }
            });
        } else {
            tcs.setResult(new LatLng(p.getLatitude(), p.getLongitude()));
        }

        // procedure called when the place coordinates are retrieved
        tcs.getTask().continueWith(new Continuation<LatLng, Void>() {
            @Override
            public Void then(final Task<LatLng> task) throws Exception {
                // dismiss the loading dialog
                stopLoading();

                // handles the specific actions in the case of the task was cancelled, triggered
                // an error, or finished successfully
                if (task.isCancelled()) {
                    // on task cancellation just log that this happened
                    Log.e(TAG, "Task for positioning the map on a place was cancelled.");
                } else if (!task.isFaulted()) {
                    // on task successfully finishing, positions the map on the place coordinates
                    if (task.getResult() != null && mMap != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mMap.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                                task.getResult(),
                                                Globals.DEFAULT_ZOOM_LEVEL
                                        )
                                );

                                // changes the mPlace coordinates for the activity result
                                mPlace.setLatitude(task.getResult().latitude);
                                mPlace.setLongitude(task.getResult().longitude);
                            }
                        });
                    }
                } else {
                    // on task fault, displays a default error message to the user, and logs th error
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "Error on search for place coordinates.", task.getError());

                            Toast.makeText(GetLocationActivity.this,
                                    getString(R.string.errormsg_default),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

                return null;
            }
        });
    }

    /**
     * Initialize the screen components
     *
     * @since 0.1.0
     */
    private void initComponents() {
        // initialize google api client settings
        buildGoogleApiClient();

        // initialize the google maps fragment
        initMap();

        // setup the app bar
        mAppBar = (Toolbar) findViewById(R.id.tool_bar);
        initAppBar(mAppBar);

        Button btnOk = (Button) findViewById(R.id.ok_button);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOkOnClick();
            }
        });

        // check if some extra with button message was sent, and customize it
        if ( getIntent().hasExtra(BUTTON_MSG) ) {
            btnOk.setText(getIntent().getStringExtra(BUTTON_MSG));
        }

        // check if some extra with pin resource id was sent, and customize it
        if ( getIntent().hasExtra(PIN_RES_ID) ) {
            ImageView pinImg = (ImageView) findViewById(R.id.img_pin);
            int resId = getIntent().getIntExtra(PIN_RES_ID, -1);

            if( resId != -1 )
                pinImg.setImageDrawable(getResources().getDrawable(resId));
        }

        // inflate the search box edit text and define its event listeners
        mSearchEditText = (EditText) findViewById(R.id.search_edit_text);
        mSearchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.list_places).setVisibility(View.VISIBLE);
                findViewById(R.id.pin_layout).setVisibility(View.GONE);
                findViewById(R.id.ok_button).setVisibility(View.GONE);
                mMapVisible = false;
            }
        });
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                searchEditTextOnTextChanged(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        initSearchPlaceComponents();
    }

    /**
     * Handler for the text changed event on the screen search box.
     *
     * @param s The search box value.
     * @since 0.5.0
     */
    private void searchEditTextOnTextChanged(CharSequence s) {
        // hide the clear button if the search box is empty, and display it again if not
        if ( s.length() == 0) {
            mBtnClear.setVisibility(View.GONE);
        } else {
            mBtnClear.setVisibility(View.VISIBLE);
        }

        // see the mLockSearch documentation for a better understanding of this condition
        if (! mLockSearch) {
            placeSearch(s.toString());
        } else {
            mLockSearch = false;
        }
    }

    /**
     * Initialize the components displayed when the user is searching for a place.
     *
     * @since 0.5.0
     */
    private void initSearchPlaceComponents() {
        RecyclerView placesRecyclerView = (RecyclerView) findViewById(R.id.places_recyclerview);
        placesRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        placesRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new SearchPlaceAdapter(this, null, new SearchPlaceAdapter.OnItemClickListener() {
            @Override
            public void OnItemClicked(View v, int position) {
                Place p = mAdapter.getItem(position);

                UIUtil.hideKeyboard(GetLocationActivity.this);

                // lock the automatic search triggered by the search box text changing
                mLockSearch = true;
                mSearchEditText.setText(p.getTitulo());

                mPlace = p;
                switchView();
                changeMapPosition(p);
            }
        });
        placesRecyclerView.setAdapter(mAdapter);

        mGooglePlacesHelper = new GooglePlacesHelper(this);

        mBtnClear = (ImageButton) findViewById(R.id.clear_button);
        mBtnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchEditText.setText("");
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mSearchIcon = (ImageView) findViewById(R.id.ic_search);

        // a fixed item is displayed to the user can find the UFAM more quickly
        findViewById(R.id.ufam_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtil.hideKeyboard(GetLocationActivity.this);

                // lock the automatic search triggered by the search box text changing
                mLockSearch = true;
                mSearchEditText.setText(mUfamPlace.getTitulo());

                mPlace = mUfamPlace;
                switchView();
                changeMapPosition(mUfamPlace);
            }
        });
    }

    /**
     * Chamado sempre que o usuário altera o conteúdo do campo de busca. Recebe como parâmetro
     * o conteúdo digitado pelo usuário, e repassa para o método que busca os locais utilizando
     * a API do Google Places.
     *
     * @param strBusca String digitada pelo usuário, representando a busca que ele deseja realizar.
     */
    private void placeSearch(String searchedTerm) {
        // searches only if no other search is being processed
        if ( !isSearching ) {
            Log.d(TAG, "Starting a new place search, searched term: " + searchedTerm);

            // starts a new search only after the user has been typed at least 3 characters
            if (searchedTerm.length() > 2) {
                // blocks other searches, until this one has been finished
                isSearching = true;

                // stores the searched value, to compare with the text on search box, when the
                // searching completes
                mLastSearch = searchedTerm;

                // display the ProgressBar instead of the search icon
                mSearchIcon.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);

                mGooglePlacesHelper.autocompleteAsync(searchedTerm).
                        continueWith(new Continuation<List<Place>, Void>() {
                            @Override
                            public Void then(final Task<List<Place>> task) {
                                // unblock new searches
                                isSearching = false;

                                // Verifica se a tarefa foi executada com sucesso
                                // TODO Exibir essa mensagem de erro na tela, e não apenas em um Toast
                                if (task.isFaulted()) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(
                                                    GetLocationActivity.this,
                                                    R.string.search_error,
                                                    Toast.LENGTH_LONG
                                            ).show();
                                        }
                                    });

                                    Log.e(TAG, "Error on searching for a place", task.getError());
                                } else if (!task.isCancelled()) {
                                    mAdapter.setDataset(task.getResult());

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // restore the search icon
                                        mProgressBar.setVisibility(View.INVISIBLE);
                                        mSearchIcon.setVisibility(View.VISIBLE);

                                        // check if user changed the search box value while a search
                                        // was being executed, if that so, do another search with
                                        // current value
                                        if (!mSearchEditText.getText().toString().equals(mLastSearch)) {
                                            placeSearch(mSearchEditText.getText().toString());
                                        }
                                    }
                                });

                                return null;
                            }
                        });
            }
        } else {
            Log.d(TAG, "[buscaLocal] Uma consulta já está sendo realizada, a consulta foi cancelada.");
        }
    }

    /**
     * Inicializa as configurações do mapa. Instancia o objeto GoogleMap a partir do Fragment,
     */
    protected void initMap() {
        if (mMap == null) {
            double lat = Globals.MANAUS_LAT;
            double lng = Globals.MANAUS_LNG;
            float zoom = Globals.DEFAULT_ZOOM_LEVEL;

            // Verifica se foi passada uma localização inicial
            mPlace = Place.getStoredInstance(INITIAL_PLACE);
            if (mPlace != null) {
                if (mPlace.hasCoord()) {
                    lat = mPlace.getLatitude();
                    lng = mPlace.getLongitude();
                }
            } else {
                // Obtém as últimas configurações do mapa do usuário.
                SharedPreferences settings = getSharedPreferences(Globals.DEFAULT_PREF_NAME, MODE_PRIVATE);
                if (settings.contains(Globals.LAT_PREF_KEY))
                    lat = Double.longBitsToDouble(settings.getLong(Globals.LAT_PREF_KEY, 0));

                if (settings.contains(Globals.LNG_PREF_KEY))
                    lng = Double.longBitsToDouble(settings.getLong(Globals.LNG_PREF_KEY, 0));

                zoom = settings.getFloat(Globals.ZOOM_PREF_KEY, Globals.DEFAULT_ZOOM_LEVEL);
            }

            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(lat, lng))
                    .zoom(zoom)
                    .build();

            GoogleMapOptions mapOptions = new GoogleMapOptions()
                    .camera(position);

            mMapFragment = SupportMapFragment.newInstance(mapOptions);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.container, mMapFragment);
            transaction.commit();

            // Obtém a instância do GoogleMap
            mMapFragment.getMapAsync(this);
        }
    }

    /**
     * Exibe um diálogo de alerta para o usuário, indicando que ele deve ativar os serviços de
     * localização no aparelho. O diálogo redireciona para a tela de configuração de localização
     * caso o usuário pressione "Sim", ou não faz nada se o usuário pressionar o botão "Não".
     */
    private  void buildAlertMessageNoGps() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.no_gps_dialog_text))
                .setTitle(getString(R.string.no_gps_dialog_title));

        builder.setPositiveButton(getString(R.string.no_gps_dialog_button_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });

        builder.setNegativeButton(getString(R.string.no_gps_dialog_button_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Inicializa a localização e o zoom do mapa. Posiciona na localização atual do usuário.
     */
    protected void initMapLocation() {
        // Posiciona o mapa na posição atual do usuário, e dá um zoom de 17.0
        if (mLastLocation != null) {
            if (mPlace == null) {
                LatLng coord = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                if (mMap != null)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 17.0f));
            }
        } else {
            Log.e(TAG, "mLastLocation ainda não foi inicializado.");
        }
    }

    /**
     * Inicializa o campo que armazenará a instância da GoogleApiClient
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Inicializa as propriedades da Application Bar, o título da barra pode ser personalizado
     * enviando um Extra contendo uma string, pela Activity que solicitou a exibição desta tela.
     */
    protected void initAppBar(Toolbar appBar) {
        if ( getIntent().hasExtra(TITLE) )
            appBar.setTitle(getIntent().getStringExtra(TITLE));
        else
            appBar.setTitle(getString(R.string.get_location_activity_title));

        appBar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        appBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Called when the button used to save the location is clicked.
     *
     * @since 0.1.0
     */
    private void btnOkOnClick() {
        // get the pin position
        LatLng pos = mMap.getCameraPosition().target;
        final Place resPlace = new Place(pos.latitude, pos.longitude);

        String title = (mPlace != null)
                ? mPlace.getTitulo()
                : "";

        resPlace.setTitulo(title);

        // a dialog displayed to user provide a title for the choosen place
        new MaterialDialog.Builder(this)
                .title(R.string.inform_a_title)
                .content(R.string.inform_a_title_message)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .negativeText(R.string.cancel)
                .input(getString(R.string.choosen_place_title), title, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (input.toString().equals("")) {
                            new MaterialDialog.Builder(GetLocationActivity.this)
                                    .title(R.string.errormsg_invalid_title)
                                    .content(R.string.errormsg_place_title_required)
                                    .iconRes(R.drawable.ic_error)
                                    .show();
                        } else {
                            resPlace.setTitulo(input.toString());

                            Place.storeInstance(RES_PLACE, resPlace);
                            setResult(Activity.RESULT_OK);
                            finish();
                        }
                    }
                }).show();
    }

    /**
     * Exibe um diálogo indicando que a tela principal está sendo carregada.
     */
    private void startLoading(String msg, final Task.TaskCompletionSource task) {
        mProDialog = new ProgressDialog(this);
        mProDialog.setMessage(msg);
        mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProDialog.setCancelable(true);
        mProDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if ( !task.getTask().isCompleted()) {
                    task.setCancelled();
                }
            }
        });
        mProDialog.show();
    }

    /**
     * Finaliza o diálogo do carregamento da tela principal.
     */
    private void stopLoading() {
        mProDialog.dismiss();
        mProDialog = null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "onConnected");

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        initMapLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                // Verifica se os serviços de localização estão ativados no aparelho. Caso não estejam,
                // exibe um diálogo para o usuário solicitando que ele ative.
                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();
                }

                return false;
            }
        });
    }
}