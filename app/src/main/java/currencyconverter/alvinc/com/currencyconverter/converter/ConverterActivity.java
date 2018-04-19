package currencyconverter.alvinc.com.currencyconverter.converter;

import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.lang.ref.WeakReference;
import java.util.List;

import currencyconverter.alvinc.com.currencyconverter.R;
import currencyconverter.alvinc.com.currencyconverter.databinding.ActivityConverterBinding;

public class ConverterActivity extends AppCompatActivity implements ConverterActivityView {


    private ActivityConverterBinding binding;
    private ConverterPresenter converterPresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_converter);

        ClickListener clickListener = new ClickListener(this);
        binding.buttonOne.setOnClickListener(clickListener);
        binding.buttonTwo.setOnClickListener(clickListener);
        binding.buttonThree.setOnClickListener(clickListener);
        binding.buttonFour.setOnClickListener(clickListener);
        binding.buttonFive.setOnClickListener(clickListener);
        binding.buttonSix.setOnClickListener(clickListener);
        binding.buttonSeven.setOnClickListener(clickListener);
        binding.buttonEight.setOnClickListener(clickListener);
        binding.buttonNine.setOnClickListener(clickListener);
        binding.buttonZero.setOnClickListener(clickListener);
        binding.buttonDelete.setOnClickListener(clickListener);
        binding.buttonConvert.setOnClickListener(clickListener);
        binding.resetRates.setOnClickListener(clickListener);

        LongClickListener longClickListener = new LongClickListener(this);
        binding.buttonDelete.setOnLongClickListener(longClickListener);

        converterPresenter = new ConverterPresenter(this);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        converterPresenter.persistData();
    }

    @Override
    public void setInputValue(String inputValue) {
        binding.textViewInput.setText(inputValue);
    }

    @Override
    public void setOutputValue(String outputValue) {
        binding.textViewOutput.setText(outputValue);
    }

    @Override
    public void setInfoText(String conversionRatio) {
        binding.textViewConversionRatio.setText(conversionRatio);
    }

    @Override
    public void setLoadingSpinnerVisible() {
        binding.progressbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void setLoadingSpinnerGone() {
        binding.progressbar.setVisibility(View.GONE);
    }

    @Override
    public void showError(String error) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(getString(R.string.error_connection));
        alertBuilder.setMessage(error);
        alertBuilder.setCancelable(false);
        alertBuilder.setNegativeButton("Close App", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        if (converterPresenter.currenciesList != null && converterPresenter.currenciesList.size() > 0) {
            // if we have currency data we can let the user play in offline mode
            // if we don't even have a currency list we risk running into nulls in various places.
            alertBuilder.setPositiveButton("Continue", new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //do nothing, let the user enjoy offline mode.
                }
            });
        }
        alertBuilder.create().show();
    }

    @Override
    public void setCurrencies(List<String> currenciesList) {
        ArrayAdapter<String> spinnerArrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currenciesList);

        binding.spinnerCurrencyInput.setAdapter(spinnerArrayAdapter);
        binding.spinnerCurrencyInput.setOnItemSelectedListener(new InputItemSelectedListener(this));

        binding.spinnerCurrencyOutput.setAdapter(spinnerArrayAdapter);
        binding.spinnerCurrencyOutput.setOnItemSelectedListener(new OutputItemSelectedListener(this));
    }

    public void onLongClick(View view) {
        converterPresenter.deleteAllChars();
    }

    public void onInputCurrencySelected(int position) {
        converterPresenter.setInputCurrencyChoice(position);
    }

    public void onOutputCurrencySelected(int position) {
        converterPresenter.setOutputCurrencyChoice(position);
    }

    /**
     * breaks MVP principles here, but the alternatives is to either
     * have one listener for each button, calling the presenter method;
     * or have the ID passed to presenter, both aren't cleaner.
     */
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_one:
                converterPresenter.appendChar('1');
                break;
            case R.id.button_two:
                converterPresenter.appendChar('2');
                break;
            case R.id.button_three:
                converterPresenter.appendChar('3');
                break;
            case R.id.button_four:
                converterPresenter.appendChar('4');
                break;
            case R.id.button_five:
                converterPresenter.appendChar('5');
                break;
            case R.id.button_six:
                converterPresenter.appendChar('6');
                break;
            case R.id.button_seven:
                converterPresenter.appendChar('7');
                break;
            case R.id.button_eight:
                converterPresenter.appendChar('8');
                break;
            case R.id.button_nine:
                converterPresenter.appendChar('9');
                break;
            case R.id.button_zero:
                converterPresenter.appendChar('0');
                break;
            case R.id.button_delete:
                converterPresenter.deleteChar();
                break;
            case R.id.button_convert:
                converterPresenter.convert();
                break;
            case R.id.reset_rates:
                converterPresenter.clearData();
                break;
        }
    }

    private static class ClickListener implements View.OnClickListener {
        private WeakReference<ConverterActivity> converterActivityWeakReference;

        private ClickListener(ConverterActivity converterActivity) {
            this.converterActivityWeakReference = new WeakReference<>(converterActivity);
        }

        @Override
        public void onClick(View view) {
            ConverterActivity converterActivity = converterActivityWeakReference.get();
            if (converterActivity == null) return;
            converterActivity.onClick(view);
        }
    }

    private static class LongClickListener implements View.OnLongClickListener {
        private WeakReference<ConverterActivity> converterActivityWeakReference;

        private LongClickListener(ConverterActivity converterActivity) {
            this.converterActivityWeakReference = new WeakReference<>(converterActivity);
        }

        @Override
        public boolean onLongClick(View view) {
            ConverterActivity converterActivity = converterActivityWeakReference.get();
            if (converterActivity == null) return false;
            converterActivity.onLongClick(view);
            return true;
        }
    }

    private static class OutputItemSelectedListener implements AdapterView.OnItemSelectedListener {
        private WeakReference<ConverterActivity> converterActivityWeakReference;

        private OutputItemSelectedListener(ConverterActivity converterActivity) {
            this.converterActivityWeakReference = new WeakReference<>(converterActivity);
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            ConverterActivity converterActivity = converterActivityWeakReference.get();
            if (converterActivity == null) return;
            converterActivity.onOutputCurrencySelected(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            ConverterActivity converterActivity = converterActivityWeakReference.get();
            if (converterActivity == null) return;
            converterActivity.onOutputCurrencySelected(0);
        }
    }

    private static class InputItemSelectedListener implements AdapterView.OnItemSelectedListener {
        WeakReference<ConverterActivity> converterActivityWeakReference;

        InputItemSelectedListener(ConverterActivity converterActivity) {
            this.converterActivityWeakReference = new WeakReference<>(converterActivity);
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            ConverterActivity converterActivity = converterActivityWeakReference.get();
            if (converterActivity == null) return;
            converterActivity.onInputCurrencySelected(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            ConverterActivity converterActivity = converterActivityWeakReference.get();
            if (converterActivity == null) return;
            converterActivity.onInputCurrencySelected(0);
        }
    }
}

