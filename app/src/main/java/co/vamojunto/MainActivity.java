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
import android.support.v4.app.FragmentTransaction;
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

import co.vamojunto.fragment.MainFragment;
import co.vamojunto.fragment.MinhasCaronasFragment;
import co.vamojunto.fragment.NovaCaronaFragment;
import co.vamojunto.util.Globals;

/**
 * Activity principal do sistema.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class MainActivity extends ActionBarActivity {

    /**
     * Usado para identificação nos logs
     */
    private static final String TAG = Globals.PACKAGE + "MainActivity";

    private Toolbar mToolbar;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * Usuário autenticado no sistema
     */
    private ParseUser mCurrentUser;


/*************************************************************************************************
 *
 * Implementação dos eventos da Activity
 *
 *************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verifica se o usuário está autenticado
        mCurrentUser = ParseUser.getCurrentUser();
        if (mCurrentUser == null) {
            Log.i(TAG, "Usuário não autenticado, exibindo tela de login");

            // Caso não haja usuário autenticado exibe a tela de login.
            Intent intent = new Intent(this, LoginActivity.class);
            this.startActivity(intent);
            this.finish();
        } else {
            setContentView(R.layout.activity_main);

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new MainFragment())
                        .commit();
            }

            // Inicializa a AppBar
            mToolbar = (Toolbar) findViewById(R.id.tool_bar);
            setSupportActionBar(mToolbar);

            // Inicializa o NavigationDrawer da aplicação
            initDrawer();
        }
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
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                getSupportFragmentManager().beginTransaction().add(R.id.container, new MinhasCaronasFragment())
                        .commit();

                mDrawerLayout.closeDrawers();
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
}
