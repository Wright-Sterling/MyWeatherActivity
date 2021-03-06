package com.example.myweatheractivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_HTTP_URL_CONNECTION = "HTTP_URL_CONNECTION";

    // Child thread sent message type value to activity main thread Handler.
    private static final int REQUEST_CODE_SHOW_RESPONSE_TEXT = 1;

    // The key of message stored server returned data.
    private static final String KEY_RESPONSE_TEXT = "KEY_RESPONSE_TEXT";

    // Request method GET. The value must be uppercase.
    private static final String REQUEST_METHOD_GET = "GET";

    // Request weather input text box.
    private EditText weatherZipEntry = null;

    // Send http request button.
    private Button requestWeatherButton = null;

    // TextView to display server returned page html text.
    private TextView responseTextView = null;

    // TextViews to display current weather from Weather object.
    private TextView descriptionTextView = null;
    private TextView temperatureTextView = null;
    private TextView humidityTextView = null;
    private TextView highTempTextView = null;
    private TextView lowTempTextView = null;

    // This handler used to listen to child thread show return page html text message and display those text in responseTextView.
    private Handler uiUpdater = null;

    // This should really be stored separately and not included in public repo
    private static final String AP_ID = "123e236852641b9b3bfd755ffa553566";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Get Weather by Zip Code");

        // Init app ui controls.
        initControls();

        // When click request weather button.
        requestWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String zip = weatherZipEntry.getText().toString();
                String reqUrl = "https://api.openweathermap.org/data/2.5/weather?zip=" + zip
                        +"&units=imperial&APPID=" + AP_ID;
                if(!TextUtils.isEmpty(reqUrl))
                {
                    if(URLUtil.isHttpUrl(reqUrl) || URLUtil.isHttpsUrl(reqUrl))
                    {
                        startSendHttpRequestThread(reqUrl);
                    }else
                    {
                        Toast.makeText(getApplicationContext(), "The request url is not a valid http or https url.", Toast.LENGTH_LONG).show();
                    }
                }else
                {
                    Toast.makeText(getApplicationContext(), "The request url can not be empty.", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    // Initialize app controls.
    @SuppressLint("HandlerLeak")
    private void initControls()
    {
        if(weatherZipEntry == null)
        {
            weatherZipEntry = (EditText)findViewById(R.id.weatherZipEntry);
        }

        if(requestWeatherButton == null)
        {
            requestWeatherButton = (Button)findViewById(R.id.request_weather_button);
        }

        if(descriptionTextView == null)
        {
            descriptionTextView = (TextView)findViewById(R.id.description);
        }

        if(temperatureTextView == null)
        {
            temperatureTextView = (TextView)findViewById(R.id.temperature);
        }

        if(humidityTextView == null)
        {
            humidityTextView = (TextView)findViewById(R.id.humidity);
        }

        if(highTempTextView == null)
        {
            highTempTextView = (TextView)findViewById(R.id.high_temp);
        }

        if(lowTempTextView == null)
        {
            lowTempTextView = (TextView)findViewById(R.id.low_temp);
        }

        if(responseTextView == null)
        {
            responseTextView = (TextView)findViewById(R.id.my_weather_activity_text_view);
        }

        // This handler is used to wait for child thread message to update server response text in TextView.
        if(uiUpdater == null)
        {
            uiUpdater = new Handler()
            {
                @Override
                public void handleMessage(Message msg) {
                    if(msg.what == REQUEST_CODE_SHOW_RESPONSE_TEXT)
                    {
                        Bundle bundle = msg.getData();
                        if(bundle != null)
                        {
                            String responseText = bundle.getString(KEY_RESPONSE_TEXT);
                            Weather currentWeather = new Weather(responseText);
                            JSONObject weatherJson = (JSONObject) currentWeather.parseWeather();
                            descriptionTextView.setText(currentWeather.getDescription());
                            temperatureTextView.setText(currentWeather.getCurrentTemperature());
                            humidityTextView.setText(currentWeather.getCurrentHumidity());
                            highTempTextView.setText(currentWeather.getTodayHighTemp());
                            lowTempTextView.setText(currentWeather.getTodayLowTemp());
                            responseTextView.setText(responseText);
                        }
                    }
                }
            };
        }
    }

    /* Start a thread to send http request to web server use HttpURLConnection object. */
    private void startSendHttpRequestThread(final String reqUrl)
    {
        Thread sendHttpRequestThread = new Thread()
        {
            @Override
            public void run() {
                // Maintain http url connection.
                HttpURLConnection httpConn = null;

                // Read text input stream.
                InputStreamReader isReader = null;

                // Read text into buffer.
                BufferedReader bufReader = null;

                // Save server response text.
                StringBuilder readTextBuf = new StringBuilder();

                try {
                    // Create a URL object use page url.
                    URL url = new URL(reqUrl);

                    // Open http connection to web server.
                    httpConn = (HttpURLConnection)url.openConnection();

                    // Set http request method to get.
                    httpConn.setRequestMethod(REQUEST_METHOD_GET);

                    // Set connection timeout and read timeout value.
                    httpConn.setConnectTimeout(10000);
                    httpConn.setReadTimeout(10000);

                    // Get input stream from web url connection.
                    InputStream inputStream = httpConn.getInputStream();

                    // Create input stream reader based on url connection input stream.
                    isReader = new InputStreamReader(inputStream);

                    // Create buffered reader.
                    bufReader = new BufferedReader(isReader);

                    // Read line of text from server response.
                    String line = bufReader.readLine();

                    // Loop while return line is not null.
                    while(line != null)
                    {
                        // Append the text to string buffer.
                        readTextBuf.append(line);

                        // Continue to read text line.
                        line = bufReader.readLine();
                    }

                    // Send message to main thread to update response text in TextView after read all.
                    Message message = new Message();

                    // Set message type.
                    message.what = REQUEST_CODE_SHOW_RESPONSE_TEXT;

                    // Create a bundle object.
                    Bundle bundle = new Bundle();
                    // Put response text in the bundle with the special key.
                    bundle.putString(KEY_RESPONSE_TEXT, readTextBuf.toString());
                    // Set bundle data in message.
                    message.setData(bundle);
                    // Send message to main thread Handler to process.
                    uiUpdater.sendMessage(message);
                }catch(MalformedURLException ex)
                {
                    Log.e(TAG_HTTP_URL_CONNECTION, ex.getMessage(), ex);
                }catch(IOException ex)
                {
                    Log.e(TAG_HTTP_URL_CONNECTION, ex.getMessage(), ex);
                }finally {
                    try {
                        if (bufReader != null) {
                            bufReader.close();
                            bufReader = null;
                        }

                        if (isReader != null) {
                            isReader.close();
                            isReader = null;
                        }

                        if (httpConn != null) {
                            httpConn.disconnect();
                            httpConn = null;
                        }
                    }catch (IOException ex)
                    {
                        Log.e(TAG_HTTP_URL_CONNECTION, ex.getMessage(), ex);
                    }
                }
            }
        };
        // Start the child thread to request web page.
        sendHttpRequestThread.start();
    }
}
