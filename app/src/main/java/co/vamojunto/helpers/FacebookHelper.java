/*
 * Copyright (c) 2015. Vamo Junto Ltda. Todos os direitos reservados.
 *
 * Este software contém informações confidenciais e de propriedade da Vamo Junto Ltda,
 * ("Informações Confidenciais"). Você não deve divulgar tais informações, e deve usá-las somente em
 * conformidade com os termos do contrato de licença estabelecido entre você e a Vamo Junto Ltda.
 *
 * See LICENSE.txt
 */

package co.vamojunto.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraCaptureSession;
import android.util.Log;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.ParseFacebookUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.Callable;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;

/**
 * FacebookHelper
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class FacebookHelper {

    private static final String TAG = "FacebookHelper";

    /**
     * Responsável por buscar a imagem de perfil de um usuário.
     *
     * @param userID Id do usuário que terá a imagem carregada.
     *
     * @return Um Bitmap contendo a imagem de perfil do usuário
     *
     * @throws SocketException
     * @throws SocketTimeoutException
     * @throws MalformedURLException
     * @throws IOException
     */
    public static Task<Bitmap> getProfilePictureAsync(String userID) throws IOException {
        final Task<Bitmap>.TaskCompletionSource tcs = Task.create();
        final String imageURL = "https://graph.facebook.com/" + userID + "/picture?type=large";

        Log.i(TAG, "Iniciando requisição para obtenção da imagem de perfil do usuário");
        Task.callInBackground(new Callable<Bitmap>() {
            public Bitmap call() throws IOException {
                InputStream in = new URL(imageURL).openConnection().getInputStream();

                return BitmapFactory.decodeStream(in);
            }
        }).continueWith(
            new Continuation<Bitmap, Object>() {
                @Override
                public Object then(Task<Bitmap> task) throws Exception {
                    Log.i(TAG, "Requisição para imagem de perfil do usuário finalizada");
                    tcs.setResult(task.getResult());

                    return null;
                }
            }
        );

        return tcs.getTask();
    }

    public static Task<GraphUser> getGraphUserAsync() {
        final Task<GraphUser>.TaskCompletionSource tcs = Task.create();

        Log.i(TAG, "Iniciando requisição para obtenção do email do usuário");
        Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser graphUser, Response response) {
                Log.i(TAG, "Requisição para email do usuário finalizada");

                if (graphUser != null) {
                    tcs.setResult(graphUser);
                }
            }
        }).executeAsync();

        return tcs.getTask();
    }
}