/*
 * Copyright (c) 2015. Vamo Junto Ltda. Todos os direitos reservados.
 *
 * Este software contém informações confidenciais e de propriedade da Vamo Junto Ltda,
 * ("Informações Confidenciais"). Você não deve divulgar tais informações, e deve usá-las somente em
 * conformidade com os termos do contrato de licença estabelecido entre você e a Vamo Junto Ltda.
 *
 * See LICENSE.txt
 */

package co.vamojunto.fragment;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
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
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.GetLocationActivity;
import co.vamojunto.NovaOfertaCaronaActivity;
import co.vamojunto.R;
import co.vamojunto.helpers.GeocodingHelper;
import co.vamojunto.model.Carona;
import co.vamojunto.model.Place;
import co.vamojunto.model.Usuario;

/**
 * {@link android.support.v4.app.Fragment} com a interface padrão da tela de cadastro de carona.
 * Exibe um formulário para cadastro, para que o usuário informe os campos necessários.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class NovaCaronaFragment extends Fragment implements TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {

    private static final String TAG = "NovaCaronaFragment";

    /** Flag utilizada para indicar se o campo origem está sendo editado. A flag é utilizada para
     * identificar para onde irá o resultado após a escolha de uma coordenada pelo usuário. */
    private boolean mEditandoOrigem;

    /** Flag utilizada para indicar se o campo destino está sendo editado. A flag é utilizada para
     * identificar para onde irá o resultado após a escolha de uma coordenada pelo usuário. */
    private boolean mEditandoDestino;

    private EditText mOrigemEditText;
    private EditText mDestinoEditText;
    private EditText mHoraEditText;
    private EditText mDataEditText;
    private EditText mNumLugaresEditText;
    private EditText mDetalhesEditText;

    private Place mOrigem;
    private Place mDestino;
    private ProgressDialog mProDialog;

/***************************************************************************************************
 *
 * Ciclo de vida do Fragment.
 *
 **************************************************************************************************/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Verifica se o resultado foi enviado pela tela de seleção de localização.
        if (requestCode == GetLocationActivity.GET_LOCATION_REQUEST_CODE) {
            if ( resultCode == Activity.RESULT_OK ) {
                Bundle extras = data.getExtras();
                Place p = extras.getParcelable(GetLocationActivity.RES_PLACE);

                if (p.hasCoord())
                    traduzCoordenadas(p);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_nova_carona, container, false);

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

            final Carona c = new Carona(
                    dataHora,
                    (Usuario) Usuario.getCurrentUser(),
                    Integer.parseInt(mNumLugaresEditText.getText().toString()),
                    mDetalhesEditText.getText().toString(),
                    mOrigem,
                    mDestino
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
                                Intent intent = new Intent();
                                intent.putExtra(NovaOfertaCaronaActivity.RES_CARONA, c);

                                getActivity().setResult(Activity.RESULT_OK, intent);
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
        mEditandoOrigem = true;
        // Oculta o ícone de erro
        mOrigemEditText.setError(null);

        Intent intent = new Intent(getActivity(), GetLocationActivity.class);
        intent.putExtra(GetLocationActivity.TITULO, getString(R.string.choose_starting_point));
        intent.putExtra(GetLocationActivity.PIN_RES_ID, R.drawable.ic_pin_orig);
        intent.putExtra(GetLocationActivity.BUTTON_MSG, getString(R.string.set_start_point));

        if (mOrigem != null) {
            intent.putExtra(GetLocationActivity.INITIAL_PLACE, mOrigem);
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
        mEditandoDestino = true;
        // Oculta o ícone de erro
        mDestinoEditText.setError(null);

        Intent intent = new Intent(getActivity(), GetLocationActivity.class);
        intent.putExtra(GetLocationActivity.TITULO, getString(R.string.search_place));
        intent.putExtra(GetLocationActivity.PIN_RES_ID, R.drawable.ic_pin_dest);
        intent.putExtra(GetLocationActivity.BUTTON_MSG, getString(R.string.set_destiny));

        if (mDestino != null) {
            intent.putExtra(GetLocationActivity.INITIAL_PLACE, mDestino);
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
        mEditandoDestino = mEditandoOrigem = false;
        mOrigem = mDestino = null;

        mOrigemEditText = (EditText) rootView.findViewById(R.id.origem_edit_text);
        mDestinoEditText = (EditText) rootView.findViewById(R.id.destino_edit_text);
        mHoraEditText = (EditText) rootView.findViewById(R.id.hora_edit_text);
        mDataEditText = (EditText) rootView.findViewById(R.id.data_edit_text);
        mNumLugaresEditText = (EditText) rootView.findViewById(R.id.num_lugares_edit_text);
        mDetalhesEditText = (EditText) rootView.findViewById(R.id.detalhes_edit_text);

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
        Button btnSalvar = (Button) rootView.findViewById(R.id.btn_salvar);
        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSalvarOnClick(v);
            }
        });

        // Vincula o método que será executado no evento OnClick do EditText da origem da carona
        mOrigemEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                origemEditTextOnClick(v);
            }
        });

        // Vincula o método que será executado no evento OnClick do EditText do destino da carona
        mDestinoEditText.setOnClickListener(new View.OnClickListener() {
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
        EditText dataEditText = (EditText) getActivity().findViewById(R.id.data_edit_text);
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
        EditText horaEditText = (EditText) getActivity().findViewById(R.id.hora_edit_text);
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
            numLugaresEditText.setError(getString(R.string.error_required_field));
            numLugaresEditText.requestFocus();

            return false;
        } else {
            try {
                int numLugares = Integer.parseInt(strNumLugares);

                if ( numLugares < 0 || numLugares > 7 ) {
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
        if ( mOrigem == null ) {
            mOrigemEditText.setError("Campo obrigatório");
            // Como o campo não é selecionável, a mensagem de erro acabava não aparecendo, então
            // utilizei um Toast.
            Toast.makeText(getActivity(), getString(R.string.defina_origem), Toast.LENGTH_LONG).show();

            return false;
        }

        // Valida o campo destino do formulário
        if ( mDestino == null ) {
            mDestinoEditText.setError("Campo obrigatório");
            mDestinoEditText.requestFocus();

            Toast.makeText(getActivity(), getString(R.string.defina_destino), Toast.LENGTH_LONG).show();

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
            mDataEditText.setError(getString(R.string.data_passado));
            Toast.makeText(getActivity(), getString(R.string.data_passado), Toast.LENGTH_LONG).show();

            return false;
        } else if (dataCarona.equals(hoje)) {
            Calendar horaCarona = leHoraEditText();
            if (horaCarona.before(agora)) {
                mHoraEditText.setError(getString(R.string.hora_passado));
                Toast.makeText(getActivity(), getString(R.string.hora_passado), Toast.LENGTH_LONG).show();

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
     * Converte o par de coordenadas recebidos da GetLocationActivity, em um endereço.
     *
     * @param p Instância de Place retornada, caso o usuário tenha feito uma busca por local, ou null.
     */
    private void traduzCoordenadas(Place p) {
        // Verifica qual localização está sendo editada para poder atribuir o valor ao campo correto
        // Em seguida converte as coordenadas para o endereço por escrito. Essa solução tá
        // muito feia, mas são 00:00 do dia 19/02/2015, eu estou programando desde as 7:00
        // então vai ficar assim mesmo. Você que está lendo isso agora, favor pensar numa
        // maneira mais bonita de fazer essa parte.
        if ( mEditandoOrigem ) {
            mEditandoOrigem = false;
            mOrigem = p;

            if ( p.isGooglePlace() ) {
                mOrigemEditText.setText(p.getTitulo());
            } else {
                mOrigemEditText.setText(getString(R.string.loading_address));

                LatLng latLng = new LatLng(p.getLatitude(), p.getLongitude());
                GeocodingHelper.getEnderecoAsync(getActivity(), latLng).continueWith(
                    new Continuation<Address, Void>() {
                        @Override
                        public Void then(Task<Address> task) throws Exception {
                            final String e = task.getResult().getAddressLine(0);

                            mOrigemEditText.post(new Runnable() {
                                @Override
                                public void run() {
                                    mOrigemEditText.setText(e);
                                    mOrigem.setTitulo(e);
                                    mOrigem.setEndereco(e);
                                }
                            });

                            return null;
                        }
                    }
                );
            }
        } else if ( mEditandoDestino ) {
            mEditandoDestino = false;
            mDestino = p;

            if ( p.isGooglePlace() ) {
                mDestinoEditText.setText(p.getTitulo());
            } else {
                mDestinoEditText.setText(getString(R.string.loading_address));

                LatLng latLng = new LatLng(p.getLatitude(), p.getLongitude());
                GeocodingHelper.getEnderecoAsync(getActivity(), latLng).continueWith(
                    new Continuation<Address, Void>() {
                        @Override
                        public Void then(Task<Address> task) throws Exception {
                            final String e = task.getResult().getAddressLine(0);

                            mDestinoEditText.post(new Runnable() {
                                @Override
                                public void run() {
                                    mDestinoEditText.setText(e);
                                    mDestino.setTitulo(e);
                                    mDestino.setEndereco(e);
                                }
                            });

                            return null;
                        }
                    }
                );
            }
        }
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

    public NovaCaronaFragment() {
        // Required empty public constructor
    }

}
