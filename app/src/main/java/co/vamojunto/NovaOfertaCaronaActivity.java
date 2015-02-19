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

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Tela para cadastro de uma nova oferta de carona.
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @since 0.1.0
 */
public class NovaOfertaCaronaActivity extends ActionBarActivity implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener{

    private static final String TAG = "NovaOfertaCaronaActivity";

    /**
     * TimePickerFragment utilizado para o usuário definir o horário da carona.
     */
    public static class TimePickerFragment extends DialogFragment {

        private int mHora, mMinuto;

        public void setHora(int hora) {
            this.mHora = hora;
        }

        public void setMinuto(int minuto) {
            this.mMinuto = minuto;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            TimePickerDialog dialog = new TimePickerDialog(getActivity(), (TimePickerDialog.OnTimeSetListener) getActivity(),
                    mHora, mMinuto, true);
            dialog.setTitle(getString(R.string.time_picker_title));

            return dialog;
        }
    }

    /**
     * DatePickerFragment utilizado para o usuário definir a data da carona
     */
    public static class DatePickerFragment extends DialogFragment {

        private int mDia, mMes, mAno;

        public void setDia(int dia) {
            this.mDia = dia;
        }

        public void setMes(int mes) {
            this.mMes = mes;
        }

        public void setAno(int ano) {
            this.mAno = ano;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener) getActivity(),
                    mAno, mMes, mDia);
            dialog.setTitle(getString(R.string.date_picker_title));

            return dialog;
        }
    }

    private Toolbar mAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_oferta_carona);

        initComponents();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
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
        Calendar data = leDataEditText();

        // Obtém dia, mês e ano marcados a partir do Calendar
        int dia = data.get(Calendar.DAY_OF_MONTH);
        int mes = data.get(Calendar.MONTH);
        int ano = data.get(Calendar.YEAR);

        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setDia(dia);
        newFragment.setMes(mes);
        newFragment.setAno(ano);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    /**
     * Método executado quando o campo hora do formulário é clicado. O método lê o valor definido
     * no campo, e utiliza para exibir um TimePickerDialog com a hora já definida no EditText.
     *
     * @param v Instância do EditText do campo hora clicado.
     */
    private void horaEditTextOnClick(View v) {
        Calendar horaMarcada = leHoraEditText();
        int hora = horaMarcada.get(Calendar.HOUR_OF_DAY);
        int minuto = horaMarcada.get(Calendar.MINUTE);

        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.setHora(hora);
        newFragment.setMinuto(minuto);

        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    /**
     * Executado quando o botão salvar do formulário é pressionado.
     *
     * @param v Instância do botão salvar clicado.
     */
    private void btnSalvarOnClick(View v) {
        if ( validaNumLugares() ) {
            Toast.makeText(this, "Show", Toast.LENGTH_LONG).show();
        }
    }

/***************************************************************************************************
 *
 * Métodos auxiliares.
 *
 **************************************************************************************************/

    /**
     * Inicializa as propriedades dos componentes da Activity
     */
    private void initComponents() {
        // Inicializa a AppBar
        initAppBar();

        // Obtém a data e hora atuais, e utiliza para inicializar os campos data e hora do formulário.
        Calendar agora = Calendar.getInstance();
        escreveHoraEditText(agora);
        escreveDataEditText(agora);

        // Vincula o método que será executado no evento OnClick no EditText da hora
        EditText horaEditText = (EditText) findViewById(R.id.hora_edit_text);
        horaEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                horaEditTextOnClick(v);
            }
        });

        // Vincula o método que será executado no evento OnClick no EditText da data
        EditText dataEditText = (EditText) findViewById(R.id.data_edit_text);
        dataEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataEditTextOnClick(v);
            }
        });

        // Vincula o método que será executado no evento OnClick do botão Salvar.
        Button btnSalvar = (Button) findViewById(R.id.btn_salvar);
        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSalvarOnClick(v);
            }
        });
    }


    /**
     * Inicializa as propriedades da AppBar
     */
    private void initAppBar() {
        mAppBar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mAppBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Lê o valor definido no campo DataEditText e o converte para um objeto do tipo Calendar.
     *
     * @return Calendar contendo a data que estava escrita no campo DataEditText do formulário.
     */
    private Calendar leDataEditText() {
        // Obtém a data definida no formulário no formato String
        EditText dataEditText = (EditText) NovaOfertaCaronaActivity.this.findViewById(R.id.data_edit_text);
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
        EditText dataEditText = (EditText) findViewById(R.id.data_edit_text);
        dataEditText.setText(dateFormat.format(data.getTime()));
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
        EditText horaEditText = (EditText) NovaOfertaCaronaActivity.this.findViewById(R.id.hora_edit_text);
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
        EditText horaEditText = (EditText) findViewById(R.id.hora_edit_text);
        horaEditText.setText(timeFormat.format(hora.getTime()));
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
        EditText numLugaresEditText = (EditText) findViewById(R.id.num_lugares_edit_text);
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
                }
            } catch (NumberFormatException e) {
                numLugaresEditText.setError(getString(R.string.error_num_lugares));
                numLugaresEditText.requestFocus();

                return false;
            }
        }

        return true;
    }
}
