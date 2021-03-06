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

package co.vamojunto.ui.fragments;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.model.Place;
import co.vamojunto.model.RideOffer;
import co.vamojunto.model.User;
import co.vamojunto.ui.activities.GetLocationActivity;
import co.vamojunto.ui.activities.NewRideActivity;

/**
 * The main {@link android.support.v4.app.Fragment} for the {@link co.vamojunto.ui.activities.NewRideActivity}
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 *
 * TODO Change this Fragment to english, to make it on the same pattern of all other classes
 */
public class NewRideFragment extends Fragment implements TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {

    private static final String TAG = "NewRideFragment";

    /** Flag utilizada para indicar se o campo origem está sendo editado. A flag é utilizada para
     * identificar para onde irá o resultado após a escolha de uma coordenada pelo usuário. */
    private boolean mEditingStartingPoint;

    /** Flag utilizada para indicar se o campo destino está sendo editado. A flag é utilizada para
     * identificar para onde irá o resultado após a escolha de uma coordenada pelo usuário. */
    private boolean mEditingDestination;

    private TextView mStartingPointTextView;
    private TextView mDestinationTextView;
    private EditText mHoraEditText;
    private EditText mDataEditText;
    private EditText mNumLugaresEditText;
    private EditText mDetalhesEditText;

    private Place mStartingPoint;
    private Place mDestination;
    private ProgressDialog mProDialog;

/***************************************************************************************************
 *
 * Ciclo de vida do Fragment.
 *
 **************************************************************************************************/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // result sent by GetLocationActivity
        if (requestCode == GetLocationActivity.GET_LOCATION_REQUEST_CODE) {
            // resultCode is RESULT_OK when the user successfully choose a place
            if ( resultCode == Activity.RESULT_OK ) {
                Place p = Place.getStoredInstance(GetLocationActivity.RES_PLACE);

                if (mEditingStartingPoint) {
                    mEditingStartingPoint = false;

                    mStartingPoint = p;
                    mStartingPointTextView.setText(p.getTitulo());
                } else if (mEditingDestination) {
                    mEditingDestination = false;

                    mDestination = p;
                    mDestinationTextView.setText(p.getTitulo());
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_new_ride_offer, container, false);

        initComponents(rootView);

        return rootView;
    }

/***************************************************************************************************
 *
 * Eventos de componentes da tela.
 *
 **************************************************************************************************/

    /**
     * Método executado quando o campo data do formulário é clicado. O método lê o valor definido
     * no campo, e utiliza para exibir um DatePickerDialog na data já definida.
     *
     * @param v Instância do EditText do campo data clicado.
     */
    private void dataEditTextOnClick(View v) {
        mDataEditText.setError(null);

        Calendar data = leDataEditText();
        // Obtém dia, mês e ano marcados a partir do Calendar
        int dia = data.get(Calendar.DAY_OF_MONTH);
        int mes = data.get(Calendar.MONTH);
        int ano = data.get(Calendar.YEAR);

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, ano, mes, dia);
        dialog.setTitle(getString(R.string.date_picker_title));
        dialog.show();
    }

    /**
     * Método executado quando o campo hora do formulário é clicado. O método lê o valor definido
     * no campo, e utiliza para exibir um TimePickerDialog com a hora já definida no EditText.
     *
     * @param v Instância do EditText do campo hora clicado.
     */
    private void horaEditTextOnClick(View v) {
        mHoraEditText.setError(null);

        Calendar horaMarcada = leHoraEditText();
        int hora = horaMarcada.get(Calendar.HOUR_OF_DAY);
        int minuto = horaMarcada.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(getActivity(), this, hora, minuto, true);
        dialog.setTitle(getString(R.string.time_picker_title));
        dialog.show();
    }

    /**
     * Executado quando o botão salvar do formulário é pressionado. Faz a validação dos dados do
     * formulário, caso todos os dados sejam válidos, instancia uma nova carona, e a salva na nuvem,
     * após o salvamento, caso nenhum erro ocorra, retorna essa instância como Intent Extra e finaliza
     * a Activity.
     *
     * @param v Instância do botão salvar clicado.
     */
    private void btnSalvarOnClick(View v) {
        if ( validaDados() ) {
            Calendar hora = leHoraEditText();
            Calendar dataHora = leDataEditText();
            dataHora.set(Calendar.HOUR_OF_DAY, hora.get(Calendar.HOUR_OF_DAY));
            dataHora.set(Calendar.MINUTE, hora.get(Calendar.MINUTE));

            final RideOffer c = new RideOffer(
                    dataHora,
                    (User) User.getCurrentUser(),
                    Integer.parseInt(mNumLugaresEditText.getText().toString()),
                    mDetalhesEditText.getText().toString(),
                    mStartingPoint,
                    mDestination
            );

            startLoading();
            c.saveInBackground().continueWith(new Continuation<Void, Void>() {
                @Override
                public Void then(Task<Void> task) throws Exception {
                    stopLoading();
                    Handler handler = new Handler(Looper.getMainLooper());

                    if (!task.isCancelled() && !task.isFaulted()) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                RideOffer.storeInstance(NewRideActivity.RES_RIDE, c);

                                getActivity().setResult(Activity.RESULT_OK);
                                getActivity().finish();
                            }
                        });
                    } else {
                        Log.e(TAG, task.getError().getMessage());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), getString(R.string.erro_cadastro_carona),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    return null;
                }
            });
        }
    }

    /**
     * Executado quando o campo do endereço de origem é selecionado pelo usuário. O Fragment de
     * busca de localização é exibido, para que o usuário possa selecionar um local, e atribuir
     * como ponto de partida da carona.
     *
     * @param v Instância do EditText clicado.
     */
    private void origemEditTextOnClick(View v) {
        mEditingStartingPoint = true;
        // Oculta o ícone de erro
        mStartingPointTextView.setError(null);

        Intent intent = new Intent(getActivity(), GetLocationActivity.class);
        intent.putExtra(GetLocationActivity.TITLE, getString(R.string.choose_starting_point));
        intent.putExtra(GetLocationActivity.PIN_RES_ID, R.drawable.ic_pin_orig);
        intent.putExtra(GetLocationActivity.BUTTON_MSG, getString(R.string.set_start_point));

        if (mStartingPoint != null) {
            Place.storeInstance(GetLocationActivity.INITIAL_PLACE, mStartingPoint);
        }

        startActivityForResult(intent, GetLocationActivity.GET_LOCATION_REQUEST_CODE);
    }

    /**
     * Executado quando o campo do endereço de destino é selecionado pelo usuário. O Fragment de
     * busca de localização é exibido, para que o usuário possa selecionar um local, e atribuir
     * como ponto de partida da carona.
     *
     * @param v Instância do EditText clicado.
     */
    private void destinoEditTextOnclick(View v) {
        mEditingDestination = true;
        // Oculta o ícone de erro
        mDestinationTextView.setError(null);

        Intent intent = new Intent(getActivity(), GetLocationActivity.class);
        intent.putExtra(GetLocationActivity.TITLE, getString(R.string.search_place));
        intent.putExtra(GetLocationActivity.PIN_RES_ID, R.drawable.ic_pin_dest);
        intent.putExtra(GetLocationActivity.BUTTON_MSG, getString(R.string.set_destination));

        if (mDestination != null) {
            Place.storeInstance(GetLocationActivity.INITIAL_PLACE, mDestination);
        }

        startActivityForResult(intent, GetLocationActivity.GET_LOCATION_REQUEST_CODE);
    }


 /***************************************************************************************************
 *
 * Métodos auxiliares.
 *
 **************************************************************************************************/

    /**
     * Inicializa as propriedades dos componentes da Activity
     */
    private void initComponents(View rootView) {
        mEditingDestination = mEditingStartingPoint = false;
        mStartingPoint = mDestination = null;

        mStartingPointTextView = (TextView) rootView.findViewById(R.id.startingPointTextView);
        mDestinationTextView = (TextView) rootView.findViewById(R.id.destinationTextView);
        mHoraEditText = (EditText) rootView.findViewById(R.id.time_edit_text);
        mDataEditText = (EditText) rootView.findViewById(R.id.date_edit_text);
        mNumLugaresEditText = (EditText) rootView.findViewById(R.id.num_lugares_edit_text);
        mDetalhesEditText = (EditText) rootView.findViewById(R.id.details_edit_text);

        // Obtém a data e hora atuais, e utiliza para inicializar os campos data e hora do formulário.
        Calendar agora = Calendar.getInstance();
        escreveHoraEditText(agora);
        escreveDataEditText(agora);

        // Vincula o método que será executado no evento OnClick no EditText da hora
        mHoraEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                horaEditTextOnClick(v);
            }
        });

        // Vincula o método que será executado no evento OnClick no EditText da data
        mDataEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataEditTextOnClick(v);
            }
        });

        // Vincula o método que será executado no evento OnClick do botão Salvar.
        Button btnSalvar = (Button) rootView.findViewById(R.id.save_button);
        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSalvarOnClick(v);
            }
        });

        // Vincula o método que será executado no evento OnClick do EditText da origem da carona
        rootView.findViewById(R.id.startingPointGroup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                origemEditTextOnClick(v);
            }
        });

        // Vincula o método que será executado no evento OnClick do EditText do destino da carona
        rootView.findViewById(R.id.destinationGroup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destinoEditTextOnclick(v);
            }
        });
    }

    /**
     * Lê o valor definido no campo DataEditText e o converte para um objeto do tipo Calendar.
     *
     * @return Calendar contendo a data que estava escrita no campo DataEditText do formulário.
     */
    private Calendar leDataEditText() {
        // Obtém a data definida no formulário no formato String
        EditText dataEditText = (EditText) getActivity().findViewById(R.id.date_edit_text);
        String strData = String.valueOf(dataEditText.getText());

        // Utiliza um SimpleDateFormat para fazer o parse da data e o converte para um Calendar
        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format));
        Calendar data = Calendar.getInstance();
        try {
            data.setTime(dateFormat.parse(strData));
            return data;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Preenche o campo data no formulário a partir de um Calendar especificado
     *
     * @param data Calendar contendo a data a ser exibida no campo DataEditText do formulário.
     */
    private void escreveDataEditText(Calendar data) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format));
        mDataEditText.setText(dateFormat.format(data.getTime()));
    }

    /**
     * Método que trata o que acontece após o usuário selecionar uma data no DatePickerFragment
     * exibido como diálogo. A data selecionada pelo usuário é formatada e escrita no DataEditText.
     *
     * @param view Instância do DatePicker
     * @param year Ano selecionado pelo usuário
     * @param month Mês selecionado pelo usuário
     * @param day Dia selecionado pelo usuário
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.YEAR, year);

        escreveDataEditText(c);
    }

    /**
     * Lê o valor definido no campo HoraEditText e o converte para um objeto do tipo Calendar.
     *
     * @return Calendar contendo a data que estava escrita no campo HOraEditText do formulário,
     *         ou null caso algum erro ocorra.
     */
    private Calendar leHoraEditText() {
        // Obtém a mHora definida no formulário no formato String
        EditText horaEditText = (EditText) getActivity().findViewById(R.id.time_edit_text);
        String strHora = String.valueOf(horaEditText.getText());

        // Utiliza um SimpleDateFormat para fazer o parse da mHora e o converte para um Calendar
        SimpleDateFormat timeFormat = new SimpleDateFormat(getString(R.string.time_format));
        Calendar hora = Calendar.getInstance();
        try {
            hora.setTime(timeFormat.parse(strHora));
            return hora;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Preenche o campo mHora no formulário a partir de um Calendar especificado
     *
     * @param hora Calendar contendo a mHora a ser exibida no campo HoraEditText do formulário.
     */
    private void escreveHoraEditText(Calendar hora) {
        SimpleDateFormat timeFormat = new SimpleDateFormat(getString(R.string.time_format));
        mHoraEditText.setText(timeFormat.format(hora.getTime()));
    }

    /**
     * Método que trata o que acontece após o usuário selecionar um horário no TimePickerDialog
     * exibido. O horário selecionado pelo usuário é formatado, e escrito no HoraEditText.
     *
     * @param view Instância do TimePicker
     * @param hourOfDay Hora escolhida pelo usuário
     * @param minute Minuto escolhido pelo usuário
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar horario = Calendar.getInstance();
        horario.set(Calendar.HOUR_OF_DAY, hourOfDay);
        horario.set(Calendar.MINUTE, minute);

        escreveHoraEditText(horario);
    }

    /**
     * Método utilizado para validar o valor digitado pelo usuário no campo referente ao número
     * de lugares disponíveis na carona.
     *
     * @return True caso o campo tenha um valor numérico entre 0 e 7, e false caso contrário.
     */
    public boolean validaNumLugares() {
        // Valida o campo número de lugares
        EditText numLugaresEditText = (EditText) getActivity().findViewById(R.id.num_lugares_edit_text);
        String strNumLugares = String.valueOf(numLugaresEditText.getText());

        if ( strNumLugares.equals("") ) {
            numLugaresEditText.setError(getString(R.string.errormsg_required_field));
            numLugaresEditText.requestFocus();

            return false;
        } else {
            try {
                int numLugares = Integer.parseInt(strNumLugares);

                if ( numLugares < 1 || numLugares > 7 ) {
                    numLugaresEditText.setError(getString(R.string.error_num_lugares));
                    numLugaresEditText.requestFocus();

                    return false;
                }
            } catch (NumberFormatException e) {
                numLugaresEditText.setError(getString(R.string.error_num_lugares));
                numLugaresEditText.requestFocus();

                return false;
            }
        }

        return true;
    }

    /**
     * Valida os dados do formulário, caso algum campo tenha um valor inválido, exibe uma mensagem
     * de erro no primeiro campo incorreto encontrado.
     *
     * @return True caso todos os dados sejam válidos, e false caso contrário.
     */
    public boolean validaDados() {
        // Valida o campo origem do formullário.
        if ( mStartingPoint == null ) {
            mStartingPointTextView.setError(getString(R.string.errormsg_required_field));
            mStartingPointTextView.requestFocus();

            return false;
        }

        // Valida o campo destino do formulário
        if ( mDestination == null ) {
            mDestinationTextView.setError(getString(R.string.errormsg_required_field));
            mDestinationTextView.requestFocus();

            return false;
        }

        // Verifica se a data e horário da carona, são válidos, para serem válidos, a data e hora
        // não podem ter passado no momento do cadastro.

        // Esse primeiro trecho foi utilizado para extrair apenas a data e a hora atuais separadamente
        // para que esses valores pudessem ser comparados individualmente com os valores de cada
        // campo do formulário.
        Date dtAtual = new Date();
        SimpleDateFormat dtFormat = new SimpleDateFormat(getString(R.string.date_format));
        String strData = dtFormat.format(dtAtual);

        SimpleDateFormat hrFormat = new SimpleDateFormat(getString(R.string.time_format));
        String strHora = hrFormat.format(dtAtual);

        Calendar hoje = Calendar.getInstance();
        Calendar agora = Calendar.getInstance();
        try {
            hoje.setTime(dtFormat.parse(strData));
            agora.setTime(hrFormat.parse(strHora));
        } catch (ParseException e) {
            Log.d(TAG, e.getMessage() + ". Você deveria ter utilizado os formatos de data e hora padrão que são String Resources do projeto" );
        }

        // Realiza as comparações em si, caso a data selecionada, seja anterior a atual, exibe a mensagem
        // e invalida o cadastro. Caso a data selecionada seja a mesma da atual, faz a validação
        // da hora.
        Calendar dataCarona = leDataEditText();
        if (dataCarona.before(hoje)) {
            mDataEditText.setError(getString(R.string.error_past_date));
            Toast.makeText(getActivity(), getString(R.string.error_past_date), Toast.LENGTH_LONG).show();

            return false;
        } else if (dataCarona.equals(hoje)) {
            Calendar horaCarona = leHoraEditText();
            if (horaCarona.before(agora)) {
                mHoraEditText.setError(getString(R.string.error_past_time));
                Toast.makeText(getActivity(), getString(R.string.error_past_time), Toast.LENGTH_LONG).show();

                return false;
            }
        }

        // Valida o número de lugares.
        if (!validaNumLugares()) {
            return false;
        }

        return true;
    }

    /**
     * Exibe um diálogo indicando que a tela principal está sendo carregada.
     */
    private void startLoading() {
        mProDialog = new ProgressDialog(getActivity());
        mProDialog.setMessage(getString(R.string.salvando_carona));
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

    public NewRideFragment() {
        // Required empty public constructor
    }

}
