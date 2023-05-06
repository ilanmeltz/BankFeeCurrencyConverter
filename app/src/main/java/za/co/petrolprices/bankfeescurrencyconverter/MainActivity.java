package za.co.petrolprices.bankfeescurrencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText etAmount;
    private TextView tvConvertedAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etAmount = findViewById(R.id.etAmount);
        tvConvertedAmount = findViewById(R.id.tvConvertedAmount);

        findViewById(R.id.convertFeeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateConversion();
            }
        });
        getSupportActionBar().hide();
    }



    private void calculateConversion() {
        String amountString = etAmount.getText().toString();
        if (amountString.isEmpty()) {
            Toast.makeText(this, "Please enter an amount to convert", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountString);

        // Start the AsyncTask to fetch the conversion rate
        new GetConversionRateTask().execute(amount);
    }

    private class GetConversionRateTask extends AsyncTask<Double, Void, Double> {

        @Override
        protected Double doInBackground(Double... params) {
            try {
                URL url = new URL("https://v6.exchangerate-api.com/v6/4bc404b80217d2874508167a/pair/AUD/ZAR/1");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }

                bufferedReader.close();
                inputStream.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                return jsonObject.getDouble("conversion_rate");
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return -1.0;
            }
        }

        @Override
        protected void onPostExecute(Double conversionRate) {
            // Update the UI with the converted amount
            if (conversionRate == -1.0) {
                Toast.makeText(MainActivity.this, "Unable to fetch conversion rate", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(etAmount.getText().toString());
            double convertedAmount = amount * conversionRate;
            double feeAmount = convertedAmount * 0.025;
            double totalAmount = convertedAmount + feeAmount;
            tvConvertedAmount.setText("You will be charged roughly: R" + String.format("%.2f", totalAmount));
        }
    }

}
