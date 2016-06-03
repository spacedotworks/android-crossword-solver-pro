package space.works.crosswordsolverpremium;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings.Secure;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;


public class CrosswordConceptualSolverPremiumActivity extends Activity {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static InputStream getInputStreamFromUrl(String url) {
        InputStream content = null;
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(new HttpGet(url));
            content = response.getEntity().getContent();
        } catch (Exception e) {
            Log.e("[GET REQUEST]", "Network exception");
        }
        return content;
    }

    public void launchMain() {
        finish();
        startActivity(new Intent(CrosswordConceptualSolverPremiumActivity.this,
                CrosswordConceptualSolverPremiumActivity.class));
    }

    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //prevent network error on GB and ICS
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        final TextView info1 = (TextView) findViewById(R.id.info1);
        info1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                new AlertDialog.Builder(CrosswordConceptualSolverPremiumActivity.this)
                        .setTitle("How to enter clues")
                        .setMessage("- You may enter the clue as it appears in the " +
                                "puzzle.\n\n- For clues which include blanks, e.g. " +
                                "Helen of ____, an underscore _ can be used to " +
                                "represent the blanks"
                        )
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("AlertDialog", "Negative");
                            }
                        })
                        .show();
            }
        });


        final TextView ex1 = (TextView) findViewById(R.id.Ex1);
        ex1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText infoa = (EditText) findViewById(R.id.find);
                infoa.setText("");
            }
        });

        final TextView ex2 = (TextView) findViewById(R.id.Ex2);
        ex2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText infob = (EditText) findViewById(R.id.patt);
                infob.setText("");
            }
        });

        final TextView info2 = (TextView) findViewById(R.id.info2);
        info2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(CrosswordConceptualSolverPremiumActivity.this)
                        .setTitle("How to enter patterns")
                        .setMessage("-If none of the characters are known, you may " +
                                "enter a number representing the length of the word " +
                                "\n\n- Alternatively you may use question marks or " +
                                "fullstops (? or .) to represent each letter. E.g. 5, " +
                                "?????, ..... are equivalent.\n\n-If you are certain " +
                                "of some letters, you enter them in the correct order " +
                                "as CAPITAL letters, e.g. ??R?E??? or 2R1E3. Note that " +
                                "these letters may ignored if better answers exist."
                        )
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("AlertDialog", "Negative");
                            }
                        })
                        .show();
            }
        });

        final Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isNetworkAvailable(getBaseContext())) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "No network connectivity!", Toast.LENGTH_LONG);
                    toast.getView().setBackgroundColor(Color.GRAY);
                    toast.show();
                } else {

                    final String android_id =
                            Secure.getString(getBaseContext().getContentResolver(),
                                    Secure.ANDROID_ID);

                    final EditText find = (EditText) findViewById(R.id.find);

                    String foo = find.getText().toString();

                    final EditText pattern = (EditText) findViewById(R.id.patt);
                    pattern.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                    String bar = pattern.getText().toString();

                    //hide softkeyboard after submit
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(pattern.getWindowToken(), 0);
                    //validation

                    if (pattern.getText().toString().equals("")) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Pattern is mandatory!!", Toast.LENGTH_LONG);
                        toast.getView().setBackgroundColor(Color.GRAY);
                        toast.show();
                    } else {
                        new PostPost().execute(String.valueOf(foo), String.valueOf(bar),
                                String.valueOf(android_id));
                    }
                }
            }
        });
    }

    class PostPost extends AsyncTask<String, Integer, String> {
        ProgressDialog dialog = new ProgressDialog(CrosswordConceptualSolverPremiumActivity.this);

        void show() {
            dialog.setMessage(
                    "Loading... This may take a few seconds if your connection is slow."
            );
            dialog.show();
        }

        void hide() {
            dialog.dismiss();
        }

        protected void onPreExecute() {
            show();
        }

        protected void onProgressUpdate(Integer... progress) {
            setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            hide();
        }

        protected String doInBackground(String... params) {
            String url = getString(R.string.url);
            List<NameValuePair> paramas = new LinkedList<NameValuePair>();
            paramas.add(new BasicNameValuePair("text", String.valueOf(params[0])));
            paramas.add(new BasicNameValuePair("pattern", String.valueOf(params[1])));
            paramas.add(new BasicNameValuePair("uid",getString(R.string.uid)));
            String paramString = URLEncodedUtils.format(paramas, "utf-8");
            url += paramString;

            final InputStream test = getInputStreamFromUrl(url);
            runOnUiThread(new Runnable() {
                public void run() {
                    String total = null;
                    try {
                        total = IOUtils.toString(test);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    final TextView mTextView = (TextView) findViewById(R.id.textView1);
                    mTextView.setShadowLayer(1, 0, 0, Color.BLACK);
                    mTextView.setText(total);

                    if (mTextView.getText().toString().length() > 0) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Possible Solutions Found",
                                Toast.LENGTH_SHORT);
                        toast.getView().setBackgroundColor(Color.GRAY);
                        toast.show();
                        publishProgress(100);
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "No results",
                                Toast.LENGTH_SHORT);
                        toast.getView().setBackgroundColor(Color.GRAY);
                        toast.show();
                        publishProgress(100);
                    }
                }
            });
            return "done";
        }
    }
}