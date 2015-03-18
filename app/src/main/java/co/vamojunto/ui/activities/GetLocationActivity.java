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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;

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

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.helpers.GooglePlacesHelper;
import co.vamojunto.model.Place;
import co.vamojunto.util.Globals;

/**
 * Tela utilizada para o usuário buscar uma localização no mapa.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class GetLocationActivity extends ActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GetLocationActivity";

    private static final String PACKAGE = "co.vamojunto.ui.activities.GetLocationActivity.";

    public static final String RES_PLACE = PACKAGE + "res_place";
    public static final String INITIAL_PLACE = PACKAGE + "place";
    public static final String TITULO = PACKAGE + "title";
    public static final String PIN_RES_ID = PACKAGE + "pinId";
    public static final String BUTTON_MSG = PACKAGE + "buttonMsg";

    public static final int GET_LOCATION_REQUEST_CODE = 3127;

    /** Guarda a instância do MapFragment da Activity */
    private GoogleMap mMap;

    /** Instância da Google Play Services API Client, utilizada para manipular o Location API */
    private GoogleApiClient mGoogleApiClient;

    /** Armazena a última localização do usuário */
    private Location mLastLocation;

    /** Booleano para verificar se o app está resolvendo algum erro. */
    private boolean mResolvingError = false;

    /** Guarda uma instância de um local, caso o usuário tenha feito uma busca. */
    private Place mLocal;

    private ProgressDialog mProDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);

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
        editor.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SearchPlaceActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mLocal = Place.getStoredInstance(SearchPlaceActivity.PLACE);

            if (mLocal != null)
                posicionaMapaLocal(mLocal);
        }
    }

    /**
     * Posiciona o mapa da tela, em um local passado por parâmetro.
     *
     * @param p Instância de {@link Place} representando o local onde o mapa deve ser posicionado.
     */
    private void posicionaMapaLocal(Place p) {
        GooglePlacesHelper placesHelper = new GooglePlacesHelper(this);

        // Utilizado para gerenciar a execução da tarefa de busca pelas coordenadas, e posicionamento
        // do mapa. Criando um TaskCompletionSource, é possível fornecer ao usuário uma opção para
        // cancelar a operação caso a conexão esteja muito lenta, evitando do app ficar travado
        // com a ProgressDialog.
        final Task<LatLng>.TaskCompletionSource tcs = Task.create();

        startLoading("Posicionando mapa no local selecionado...", tcs);

        placesHelper.getLocationAsync(p).continueWith(new Continuation<LatLng, Void>() {
            @Override
            public Void then(final Task<LatLng> task) {
                tcs.setResult(task.getResult());

                return null;
            }
        });

        // Trata o resultado da operação de busca e posicionamento do mapa.
        tcs.getTask().continueWith(new Continuation<LatLng, Void>() {
            @Override
            public Void then(final Task<LatLng> task) throws Exception {
                if (task.isCancelled()) {
                    Log.d(TAG, "Busca cancelada");
                } else if (!task.isFaulted()) {
                    if (task.getResult() != null && mMap != null) {
                        Handler handler = new Handler(GetLocationActivity.this.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(task.getResult(), Globals.DEFAULT_ZOOM_LEVEL));

                                mLocal.setLatitude(task.getResult().latitude);
                                mLocal.setLongitude(task.getResult().longitude);
                                stopLoading();
                            }
                        });
                    }
                }

                return null;
            }
        });
    }

    /**
     * Inicializa os componentes da tela
     */
    private void initComponents() {
        mLocal = null;

        // Constrói o GoogleApiClient
        buildGoogleApiClient();

        // Inicializa as configurações do MapFragment
        initMap();

        // Inicializa a Application Bar
        initAppBar();

        Button btnOk = (Button) findViewById(R.id.ok_button);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOkOnClick(v);
            }
        });

        // Verifica se um extra contendo a mensagem do botão foi enviado.
        if ( getIntent().hasExtra(BUTTON_MSG) ) {
            btnOk.setText(getIntent().getStringExtra(BUTTON_MSG));
        }

        // Verifica se a Activity que chamou esta tela, enviou um extra com o resource id de uma
        // imagem para o pin de escolha de localização.
        if ( getIntent().hasExtra(PIN_RES_ID) ) {
            ImageView pinImg = (ImageView) findViewById(R.id.img_pin);
            int resId = getIntent().getIntExtra(PIN_RES_ID, -1);

            if( resId != -1 )
                pinImg.setImageDrawable(getResources().getDrawable(resId));
        }

        AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.local_auto_complete);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SearchPlaceActivity.class);
                startActivityForResult(intent, SearchPlaceActivity.REQUEST_CODE);
            }
        });
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
            mLocal = Place.getStoredInstance(INITIAL_PLACE);
            if (mLocal != null) {
                if (mLocal.hasCoord()) {
                    lat = mLocal.getLatitude();
                    lng = mLocal.getLongitude();
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

            SupportMapFragment mapFragment = SupportMapFragment.newInstance(mapOptions);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.container, mapFragment);
            transaction.commit();

            // Obtém a instância do GoogleMap
            mapFragment.getMapAsync(new OnMapReadyCallback() {
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
            });
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
            LatLng coord = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            if (mMap != null)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 17.0f));
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
    private void initAppBar() {
        Toolbar appBar = (Toolbar) findViewById(R.id.tool_bar);

        if ( getIntent().hasExtra(TITULO) )
            appBar.setTitle(getIntent().getStringExtra(TITULO));
        else
            appBar.setTitle(getString(R.string.get_location_activity_title));

        appBar.setNavigationIcon(R.drawable.ic_light_close);
        appBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Executado quando o botão da parte de baixo da tela é clicado.Verifica se o usuário fez uma busca
     * por um local, então retorna o resultado.
     *
     * @param v Instância do botão
     */
    private void btnOkOnClick(View v) {
        // Obtém a posição marcada pelo pin
        LatLng pos = mMap.getCameraPosition().target;
        Place resPlace = new Place(pos.latitude, pos.longitude);

        // Verifica se o usuário fez uma busca por local, e se modificou a posição do mapa, após a busca
        if (resPlace.equals(mLocal)) {
            resPlace.setTitulo(mLocal.getTitulo());
            resPlace.setEndereco(mLocal.getEndereco());
            resPlace.setGooglePlaceId(mLocal.getGooglePlaceId());
        }

        Place.storeInstance(RES_PLACE, resPlace);
        setResult(Activity.RESULT_OK);
        finish();
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
                task.setCancelled();
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


/***************************************************************************************************
 *
 * Implementação dos métodos da interface GoogleApiClient.ConnectionCallbacks
 *
 **************************************************************************************************/

    /**
     * Executado quando a conexão da GoogleApiClient é finalizada. Obtém a localização atual do
     * usuário, e armazena no campo mLastLocation.
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "onConnected");

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        // Posiciona o mapa na localização atual do usuário, apenas se for a primeira vez que o
        // evento está sendo executado, ou caso o usuário tenha utilizado o dialog exibido para
        // ativar o serviço de localização no aparelho.
        initMapLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended");
    }

/***************************************************************************************************
 *
 * Implementação dos métodos da interface GoogleApiClient.OnConnectionFailedListener
 *
 **************************************************************************************************/

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed");
    }

}