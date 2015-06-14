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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.R;
import co.vamojunto.helpers.GeocodingHelper;
import co.vamojunto.model.Place;
import co.vamojunto.model.RideRequest;
import co.vamojunto.model.User;
import co.vamojunto.ui.activities.GetLocationActivity;
import co.vamojunto.ui.activities.NewRideRequestActivity;

/**
 * The main {@link android.support.v4.app.Fragment} for the {@link co.vamojunto.ui.activities.NewRideRequestActivity}
 *
 * @author Andrew C. Pacifico <andrewcpacifico@gmail.com>
 * @version 1.0.0
 * @since 0.1.0
 */
public class NewRideRequestFragment extends Fragment implements TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {

    private static final String TAG = "NewRideRequestFragment";
    /**
     * Flag that indicates whether or not the starting point field is being edited. This flag
     * is used on activity result, to define where to place the coordinates chosen by user
     * after a GetPositionActivity calling.
     *
     * @since 0.1.0
     */
    private boolean mEditingStartingPoint;

    /**
     * Flag that indicates whether or not the destination field is being edited. This flag
     * is used on activity result, to define where to place the coordinates chosen by user
     * after a GetPositionActivity calling.
     *
     * @since 0.1.0
     */
    private boolean mEditingDestination;

    /**
     * The TextView that holds the starting point of the ride request.
     *
     * @since 0.6.0
     */
    private TextView mStartingPointTextView;

    /**
     * The TextView that holds the destination point of the ride request.
     *
     * @since 0.6.0
     */
    private TextView mDestinationTextView;

    /**
     * The EditText that holds the time when the user needs a ride.
     *
     * @since 0.1.0
     */
    private EditText mTimeEditText;

    /**
     * The EditText that holds the date when the user needs a ride.
     *
     * @since 0.1.0
     */
    private EditText mDateEditText;

    /**
     * The EditText that holds additional details about the ride request.
     *
     * @since 0.1.0
     */
    private EditText mDetailsEditText;

    /**
     * The starting point of the ride
     *
     * @since 0.1.0
     */
    private Place mStartingPoint;

    /**
     * The destination of the ride
     *
     * @since 0.1.0
     */
    private Place mDestination;

    /**
     * A progress dialog, displayed when any data is being loaded.
     *
     * @since 0.1.0
     */
    private ProgressDialog mProDialog;

    public NewRideRequestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // result sent by GetLocationActivity
        if (requestCode == GetLocationActivity.GET_LOCATION_REQUEST_CODE) {
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
        View rootView = inflater.inflate(R.layout.fragment_new_ride_request, container, false);

        initComponents(rootView);

        return rootView;
    }

    /**
     * Handles the OnClick event for the mDateEditText. Shows a DatePickerDialog with the field content
     * as the initial value.
     *
     * @since 0.1.0
     */
    private void mDateEditTextOnClick() {
        mDateEditText.setError(null);

        Calendar date = getDateEditTextValue();
        // gets the day, month and year defined on Calendar
        int dia = date.get(Calendar.DAY_OF_MONTH);
        int mes = date.get(Calendar.MONTH);
        int ano = date.get(Calendar.YEAR);

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, ano, mes, dia);
        dialog.setTitle(getString(R.string.date_picker_title));
        dialog.show();
    }

    /**
     * Handles the OnClick event for the mTimeEditText. Shows a TimePickerDialog with the field content
     * as the initial value.
     *
     * @since 0.1.0
     */
    private void mTimeEditTextOnClick() {
        mTimeEditText.setError(null);

        Calendar time = getTimeEditTextValue();
        int hora = time.get(Calendar.HOUR_OF_DAY);
        int minuto = time.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(getActivity(), this, hora, minuto, true);
        dialog.setTitle(getString(R.string.time_picker_title));
        dialog.show();
    }

    /**
     * Handles the OnClick event for the saveButton. If the data is valid, send it to the cloud, then
     * if no error occurs, returns a RideRequest instance as an Intent Extra, and finishes the Activity.
     *
     * @since 0.1.0
     */
    private void saveButtonOnClick() {
        if ( isDataValid() ) {
            Calendar time = getTimeEditTextValue();
            Calendar datetime = getDateEditTextValue();
            datetime.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
            datetime.set(Calendar.MINUTE, time.get(Calendar.MINUTE));

            final RideRequest rideRequest = new RideRequest((User) User.getCurrentUser(), datetime,
                    mDetailsEditText.getText().toString(), mStartingPoint, mDestination);

            startLoading();
            rideRequest.saveInBackground().continueWith(new Continuation<Void, Void>() {
                @Override
                public Void then(Task<Void> task) throws Exception {
                    stopLoading();
                    Handler handler = new Handler(Looper.getMainLooper());

                    if (!task.isCancelled() && !task.isFaulted()) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                RideRequest.storeInstance(NewRideRequestActivity.RES_RIDE, rideRequest);

                                getActivity().setResult(Activity.RESULT_OK);
                                getActivity().finish();
                            }
                        });
                    } else {
                        Log.e(TAG, task.getError().getMessage());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), getString(R.string.error_ride_request_registration),
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
     * Handles the OnClick event for the mStartingPointEditText. The GetLocationActivity is displayed
     * to user selects a place, and use it as the ride starting point.
     *
     * @since 0.1.0
     */
    private void mStartingPointEditTextOnClick() {
        mEditingStartingPoint = true;
        // hides the error icon
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
     * Handles the OnClick event for the mDestinationEditText. The GetLocationActivity is displayed
     * to user selects a place, and use it as the ride destination.
     *
     * @since 0.1.0
     */
    private void mDestinationEditTextOnClick() {
        mEditingDestination = true;
        // hides the error icon
        mDestinationTextView.setError(null);

        Intent intent = new Intent(getActivity(), GetLocationActivity.class);
        intent.putExtra(GetLocationActivity.TITLE, getString(R.string.choose_destination));
        intent.putExtra(GetLocationActivity.PIN_RES_ID, R.drawable.ic_pin_dest);
        intent.putExtra(GetLocationActivity.BUTTON_MSG, getString(R.string.set_destination));

        if (mDestination != null) {
            Place.storeInstance(GetLocationActivity.INITIAL_PLACE, mDestination);
        }

        startActivityForResult(intent, GetLocationActivity.GET_LOCATION_REQUEST_CODE);
    }

/***************************************************************************************************
 *
 * Other methods
 *
 **************************************************************************************************/

    /**
     * Itializes the componentes.
     *
     * @param rootView The Fragment's inflated layout.
     * @since 0.1.0
     */
    private void initComponents(View rootView) {
        mEditingDestination = mEditingStartingPoint = false;
        mStartingPoint = mDestination = null;

        mStartingPointTextView = (TextView) rootView.findViewById(R.id.startingPointTextView);
        mDestinationTextView = (TextView) rootView.findViewById(R.id.destinationTextView);
        mTimeEditText = (EditText) rootView.findViewById(R.id.time_edit_text);
        mDateEditText = (EditText) rootView.findViewById(R.id.date_edit_text);
        mDetailsEditText = (EditText) rootView.findViewById(R.id.details_edit_text);

        // gets the current date and time, and use it to initialize the date and time fields in the form
        Calendar now = Calendar.getInstance();
        setTimeEditTextValue(now);
        setDateEditTextValue(now);

        mTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimeEditTextOnClick();
            }
        });

        mDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDateEditTextOnClick();
            }
        });

        Button saveButton = (Button) rootView.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButtonOnClick();
            }
        });

        rootView.findViewById(R.id.starting_point_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartingPointEditTextOnClick();
            }
        });

        rootView.findViewById(R.id.destination_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDestinationEditTextOnClick();
            }
        });
    }

    /**
     * Validates the form data, if any field has a invalid value, setError on the first incorrect
     * field found.
     *
     * @return <code>true</code> if all form data is valid, and <code>false</code> otherwise.
     * @since 0.1.0
     */
    public boolean isDataValid() {
        // checks if the starting point was set
        if ( mStartingPoint == null ) {
            mStartingPointTextView.setError(getString(R.string.errormsg_required_field));
            mStartingPointTextView.requestFocus();

            return false;
        }

        // checks if the destination was set
        if ( mDestination == null ) {
            mDestinationTextView.setError(getString(R.string.errormsg_required_field));
            mDestinationTextView.requestFocus();

            return false;
        }

        // checks if the date and time are valid, to be considered valid, the date and the time
        // have to be in the futures

        // this first block is to extract just the date, and just the time separated, to compare
        // with the date and time set by user, in the code block below
        Date dtToday = new Date();
        SimpleDateFormat dtFormat = new SimpleDateFormat(getString(R.string.date_format));
        String strData = dtFormat.format(dtToday);

        SimpleDateFormat hrFormat = new SimpleDateFormat(getString(R.string.time_format));
        String strHora = hrFormat.format(dtToday);

        Calendar today = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        try {
            today.setTime(dtFormat.parse(strData));
            now.setTime(hrFormat.parse(strHora));
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage() + ". You should use the default date_format and time_format" );
        }

        // performs the comparisons, if the date is invalid, the form is considered invalid, if the
        // date is today, checks the time
        Calendar rideDate = getDateEditTextValue();
        if (rideDate.before(today)) {
            mDateEditText.setError(getString(R.string.error_past_date));
            Toast.makeText(getActivity(), getString(R.string.error_past_date), Toast.LENGTH_LONG).show();

            return false;
        } else if (rideDate.equals(today)) {
            Calendar rideTime = getTimeEditTextValue();
            if (rideTime.before(now)) {
                mTimeEditText.setError(getString(R.string.error_past_time));
                Toast.makeText(getActivity(), getString(R.string.error_past_time), Toast.LENGTH_LONG).show();

                return false;
            }
        }

        return true;
    }

    /**
     * Reads the content of the mDateEditText field, and converts it to an Calendar object.
     *
     * @return A Calendar with the date stored on the mDateEditText.
     * @since 0.1.0
     */
    private Calendar getDateEditTextValue() {
        // gets the date as a String
        String strData = String.valueOf(mDateEditText.getText());

        // converts the string date to an Calendar object
        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format));
        Calendar date = Calendar.getInstance();
        try {
            date.setTime(dateFormat.parse(strData));
            return date;
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }

    /**
     * Reads the content of the mTimeEditText field, and converts it to an Calendar object.
     *
     * @return A Calendar with the time stored on the mDateEditText.
     * @since 0.1.0
     */
    private Calendar getTimeEditTextValue() {
        // gets the time as a String
        String strData = String.valueOf(mTimeEditText.getText());

        // converts the time string to an Calendar object
        SimpleDateFormat timeFormat = new SimpleDateFormat(getString(R.string.time_format));
        Calendar time = Calendar.getInstance();
        try {
            time.setTime(timeFormat.parse(strData));
            return time;
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }

    /**
     * Sets text of the mDateEditText field, from a specified Calendar
     *
     * @param date A Calendar instance to initialize the field.
     * @since 0.1.0
     */
    private void setDateEditTextValue(Calendar date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format));
        mDateEditText.setText(dateFormat.format(date.getTime()));
    }

    /**
     * Sets text of the mTimeEditText field, from a specified Calendar
     *
     * @param time A Calendar instance to initialize the field.
     * @since 0.1.0
     */
    private void setTimeEditTextValue(Calendar time) {
        SimpleDateFormat timeFormat = new SimpleDateFormat(getString(R.string.time_format));
        mTimeEditText.setText(timeFormat.format(time.getTime()));
    }

    /**
     * Shows a dialog indicating that the main screen is bein loaded.
     *
     * @since 0.1.0
     */
    private void startLoading() {
        mProDialog = new ProgressDialog(getActivity());
        mProDialog.setMessage(getString(R.string.saving_ride_request));
        mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProDialog.setCancelable(false);
        mProDialog.show();
    }

    /**
     * Finishes the loading dialog;
     *
     * @since 0.1.0
     */
    private void stopLoading() {
        mProDialog.dismiss();
        mProDialog = null;
    }

    /**
     * Executed when the user selects a date on DatePickerDialog. Uses the date chosen and sets as
     * mDateEditText value.
     *
     * @param view DatePicker instance.
     * @param year The year chosen by user.
     * @param month The month chosen by user.
     * @param day The day chosen by user.
     *
     * @since 0.1.0
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.YEAR, year);

        setDateEditTextValue(c);
    }

    /**
     * Executed when the user selects a time on TimePickerDialog. Uses the time chosen and sets as
     * mTimeEditText value.
     *
     * @param view The TimePicker instance.
     * @param hourOfDay The time chosen by user
     * @param minute The minute chosen by user
     *
     * @since 0.1.0
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar chosenTime = Calendar.getInstance();
        chosenTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        chosenTime.set(Calendar.MINUTE, minute);

        setTimeEditTextValue(chosenTime);
    }
}
