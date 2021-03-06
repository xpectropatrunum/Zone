package ir.sourcearena.filterbourse.Account;

import id.voela.actrans.AcTrans;
import ir.sourcearena.filterbourse.MainActivity;
import ir.sourcearena.filterbourse.R;
import ir.sourcearena.filterbourse.Settings;
import ir.sourcearena.filterbourse.tools.GetUser;
import ir.sourcearena.filterbourse.tools.ToastMaker;
import ir.sourcearena.filterbourse.ui.watcher.WUtils;
import ir.sourcearena.filterbourse.ui.watcher.Watchlist;
import mehdi.sakout.fancybuttons.FancyButton;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Login extends AppCompatActivity {

    FancyButton submit, later;
    EditText ed,ed2;
    TextView tv, label;
    GetUser gu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        gu = new GetUser(getBaseContext());
        defineViews();
        actionViews();
    }

    private void actionViews() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!ed.getText().toString().equals("")){
                    new Request().execute(Settings.LOGIN_API+"?phone="+ed.getText().toString());
                }else{
                    YoYo.with(Techniques.Shake).duration(1000).playOn(ed);
                }

            }
        });
        later.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gu.putFirst();

                startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP ));

            }
        });
    }

    private void defineViews() {
        submit = findViewById(R.id.btn_login);
        later = findViewById(R.id.btn_later);
        ed = findViewById(R.id.phone_number);
        ed2 = findViewById(R.id.username);
        tv = findViewById(R.id.textView30);
        label = findViewById(R.id.textView4);
    }

    private class Request extends AsyncTask<String, Void, String> {

        StringBuffer stringBuffer;

        @Override
        protected String doInBackground(String... params) {
            stringBuffer = null;
            try {





                URL url = new URL(params[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Connection", "close");

                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();



                int code = httpURLConnection.getResponseCode();
                if (code == 200) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

                    stringBuffer = new StringBuffer();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuffer.append(line);
                    }


                }

            } catch (Exception e) {

                e.printStackTrace();
            }
            if (stringBuffer == null) {
                return "";
            }
            return stringBuffer.toString();


        }


        int p = 0;

        @Override
        protected void onPostExecute(final String result) {
            if(STAGE == 2){
                finished(result);
            }else{
                nextLevel(result);
            }




        }
    }

    private void finished(String res) {
        if(res.equals("OK")){
            new ToastMaker(getBaseContext(),"تبریک");

            gu.putUsername(username);
            gu.putName(name);

            gu.putPremium(false);
            gu.putTime(0f);
            gu.putFirst();
            startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP ));
        }else{
            new ToastMaker(getBaseContext(),"خطا" +
                    "");
        }

    }

    String phone;
    private void nextLevel(String result) {

        final String[] m = result.split(",");

        try {
            int k = Integer.parseInt(m[0]);
            if(k > 0){
                ed.setText("");
                label.setText("کد 6 رقمی را وارد کنید");
                tv.setVisibility(View.VISIBLE);
                tv.setText("کد به "+m[2]+" ارسال شد");
                later.setEnabled(false);
                ed.setHint("123456");
                phone = m[2];


                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!ed.getText().toString().equals("")){

                            if(ed.getText().toString().equals(m[1])){
                                if(m[3].equals("1")){
                                    GetUser gu = new GetUser(getBaseContext());
                                    gu.putUsername(m[4]);
                                    gu.putName(m[6]);
                                    Float f = Float.parseFloat(m[5]);
                                    if(f > 0){
                                        gu.putPremium(true);
                                        gu.putTime(f);
                                    }else{
                                        gu.putPremium(false);
                                        gu.putTime(0f);
                                    }
                                    gu.putFirst();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP ));
                                }else{
                                    finishRegister();
                                }


                            }else{
                                YoYo.with(Techniques.Shake).duration(1000).playOn(ed);
                                new ToastMaker(getBaseContext(),"کد اشتباه است");
                            }
                        }else{
                            YoYo.with(Techniques.Shake).duration(1000).playOn(ed);
                        }
                    }
                });

            }


        }catch (NumberFormatException e){
            tv.setVisibility(View.VISIBLE);
            tv.setText("خطا در ارسال کد لطفا دوباره تلاش کنید");
        }


    }
    String username, name ;
    int STAGE = 0;
    private void finishRegister() {
        STAGE = 2;
        label.setText("نام کاربری");
        tv.setVisibility(View.INVISIBLE);
        ed.setHint("bourse_zone");
        ed2.setHint("بورس زون");
        ed.setInputType(InputType.TYPE_CLASS_TEXT);
        ed.setText("");
        LinearLayout ll = findViewById(R.id.linearLayout7);
        ll.setVisibility(View.VISIBLE);



        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = ed.getText().toString();
                name = ed2.getText().toString();
                boolean valid = (username != null) && username.matches("^[a-zA-Z0-9](_(?!(\\.|_))|\\.(?!(_|\\.))|[a-zA-Z0-9]){6,18}[a-zA-Z0-9]$");
                if(valid){
                    new Request().execute(Settings.REGISTER_API+"?phone="+phone+"&username="+username+"&name="+name);
                }else{
                    YoYo.with(Techniques.Shake).duration(1000).playOn(ed);
                    new ToastMaker(getBaseContext(),"نام کاربری معتبر نیست");
                }
            }
        });

    }
}
