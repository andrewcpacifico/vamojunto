/*
 * Copyright (c) 2015. Vamo Junto Ltda. Todos os direitos reservados.
 *
 * Este software contém informações confidenciais e de propriedade da Vamo Junto Ltda,
 * ("Informações Confidenciais"). Você não deve divulgar tais informações, e deve usá-las somente em
 * conformidade com os termos do contrato de licença estabelecido entre você e a Vamo Junto Ltda.
 *
 * See LICENSE.txt
 */

package co.vamojunto;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.helpers.GooglePlacesHelper;
import co.vamojunto.model.Place;

/**
 * Tela utilizada para o usuário buscar uma localização no mapa.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class GetLocationActivity extends ActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GetLocationActivity";

    private static final String PACKAGE = "co.vamojunto.GetLocationActivity.";
    public static final String LAT = PACKAGE + "lat";
    public static final String LONG = PACKAGE + "long";
    public static final String TITLE = PACKAGE + "title";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);

        initComponents();
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
            if ( data.hasExtra(SearchPlaceActivity.PLACE)) {
                Place p = data.getParcelableExtra(SearchPlaceActivity.PLACE);

                posicionaMapaLocal(p);
            }
        }
    }

    /**
     * Posiciona o mapa da tela, em um local passado por parâmetro.
     *
     * @param p Instância de {@link Place} representando o local onde o mapa deve ser posicionado.
     */
    private void posicionaMapaLocal(Place p) {
        GooglePlacesHelper placesHelper = new GooglePlacesHelper(this);
        placesHelper.getLocationAsync(p).continueWith(new Continuation<LatLng, Void>() {
            @Override
            public Void then(final Task<LatLng> task) {
                if (task.getResult() != null) {
                    if (mMap != null) {
                        Handler handler = new Handler(GetLocationActivity.this.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(task.getResult(), 17.0f));
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
        // Constrói o GoogleApiClient
        buildGoogleApiClient();

        // Inicializa as configurações do MapFragment
        initMap();

        // Inicializa a Application Bar
        initAppBar();

        Button btnOk = (Button) findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtém a posição marcada pelo pin
                LatLng pos = mMap.getCameraPosition().target;

                Bundle bundle = new Bundle();
                bundle.putDouble(LAT, pos.latitude);
                bundle.putDouble(LONG, pos.longitude);

                Intent intent = new Intent();
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK, intent);
                finish();
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
            SupportMapFragment mapFragment = SupportMapFragment.newInstance();
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

            if ( mMap != null )
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

        if ( getIntent().hasExtra(TITLE) )
            appBar.setTitle(getIntent().getStringExtra(TITLE));
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