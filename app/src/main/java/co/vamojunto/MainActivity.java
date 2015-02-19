/*
 * Copyright (c) 2015. Vamo Junto Ltda. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Vamo Junto Ltda,
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you entered into with Vamo Junto Ltda.
 *
 * See LICENSE.txt
 */

package co.vamojunto;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

/**
 * Activity principal do sistema.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /** Usado para identificação nos logs */
    private static final String TAG = "MainActivity";

    /** Guarda a instância do MapFragment da Activity */
    private GoogleMap mMap;

    /** Instância da Google Play Services API Client, utilizada para manipular o Location API */
    private GoogleApiClient mGoogleApiClient;

    /** Armazena a última localização do usuário */
    private Location mLastLocation;

    /** Booleano para verificar se o app está resolvendo algum erro. */
    private boolean mResolvingError = false;

    private Toolbar mToolbar;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * Flag que indica se é o primeiro start da Activity, utilizada para inicializar a localização
     * do mapa na localização atual do usuário.
     */
    private boolean mFirstStart = true;

    /** Usuário autenticado no sistema */
    private ParseUser mCurrentUser;

/***************************************************************************************************
 *
 * Implementação dos eventos da Activity
 *
 **************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Verifica se o usuário está autenticado
        mCurrentUser = ParseUser.getCurrentUser();
        if ( mCurrentUser == null ) {
            Log.i(TAG, "Usuário não autenticado, exibindo tela de login.");

            // Caso não haja usuário autenticado exibe a tela de login.
            Intent intent = new Intent(this, LoginActivity.class);
            this.startActivity(intent);
            this.finish();
        } else {
            // Verifica se os serviços de localização estão ativados no aparelho. Caso não estejam,
            // exibe um diálogo para o usuário solicitando que ele ative.
            final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps();
            }

            // Inicializa a AppBar
            mToolbar = (Toolbar) findViewById(R.id.tool_bar);
            setSupportActionBar(mToolbar);

            // Constrói o GoogleApiClient
            buildGoogleApiClient();

            // Inicializa as configurações do MapFragment
            initMap();

            // Inicializa o NavigationDrawer da aplicação
            initDrawer();
        }
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

/***************************************************************************************************
 *
 * Implementação dos outros métodos criados
 *
 **************************************************************************************************/

    /**
     * Constrói o NavigationDrawer da aplicação, que contém o menu com as principais funcionalidades.
     */
    private void initDrawer() {
        mRecyclerView = (RecyclerView) findViewById(R.id.nav_drawer_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        ParseFile imgUsuarioPFile = mCurrentUser.getParseFile("img_perfil");
        Bitmap imgUsuario = null;

        // Caso o usuário não possua imagem de perfil cadastrada
        if (imgUsuarioPFile != null) {
            try {
                imgUsuario = BitmapFactory.decodeByteArray(imgUsuarioPFile.getData(), 0, imgUsuarioPFile.getData().length);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        mAdapter = new NavigationDrawerAdapter(this, mCurrentUser.getString("nome"),
                mCurrentUser.getEmail(), imgUsuario);

        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,mToolbar,R.string.openDrawer,R.string.closeDrawer){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // Código executado quando o Drawer é aberto. Nada a ser feito por enquanto
                Intent intent = new Intent(MainActivity.this, NovaOfertaCaronaActivity.class);
                MainActivity.this.startActivity(intent);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Código executado quando o Drawer é fechado.
            }

            // Executado quando o Drawer é deslizado.
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                // Código adicionado para corrigir o erro do mapa ficando sobre o menu no
                // Android 4.0.4
                mDrawerLayout.bringChildToFront(drawerView);
                mDrawerLayout.requestLayout();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
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
                // Muda o status da flag, para que o mapa seja reposicionado quando o usuário,
                // retornar à aplicação.
                mFirstStart = true;
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
     * Inicializa as configurações do mapa. Instancia o objeto GoogleMap a partir do Fragment,
     */
    protected void initMap() {
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        }

        if ( mMap != null )
            mMap.setMyLocationEnabled(true);
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
        if (mFirstStart) {
            initMapLocation();
            mFirstStart = false;
        }
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
