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

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.parse.ParseUser;

/**
 * Activity principal do sistema.
 *
 * @author Andrew C. Pacifico (andrewcpacifico@gmail.com)
 * @since 0.1.0
 */
public class MainActivity extends ActionBarActivity {

    /** Usado para identificação nos logs */
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Verifica se o usuário está autenticado
        ParseUser currentUser = ParseUser.getCurrentUser();
        if ( currentUser == null ) {
            Log.i(TAG, "Usuário não autenticado, exibindo tela de login.");

            // Caso não haja usuário autenticado exibe a tela de login.
            Intent intent = new Intent(this, LoginActivity.class);
            this.startActivity(intent);
            this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
