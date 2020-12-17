
package ir.sourcearena.filterbourse.ui.watcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baoyz.widget.PullRefreshLayout;
import com.google.android.material.tabs.TabLayout;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import id.voela.actrans.AcTrans;
import ir.sourcearena.filterbourse.MainActivity;
import ir.sourcearena.filterbourse.NamadRouter;
import ir.sourcearena.filterbourse.R;
import ir.sourcearena.filterbourse.Settings;
import ir.sourcearena.filterbourse.tools.NetworkChecker;
import ir.sourcearena.filterbourse.tools.ToastMaker;
import ir.sourcearena.filterbourse.ui.LoadingView;
import ir.sourcearena.filterbourse.ui.dialog.adapter;
import mehdi.sakout.fancybuttons.FancyButton;

public class Watchlist extends Fragment {


    LoadingView loading;
    View root, fin;

    SharedPreferences sp;
    Handler handler;
    String currentList;
    TabLayout tl;

    boolean loaded = false ;
    boolean permitted = true;

    AsyncTask task;


    @Override
    public void onResume() {
        super.onResume();
        permitted = true;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.watcher, container, false);
        loading = new LoadingView(inflater, root,getActivity());

        emp = root.findViewById(R.id.empty_list);
        SharedPreferences el = getActivity().getSharedPreferences("watchers",Context.MODE_PRIVATE);
        String nm = el.getString("cat_names","");
        if(nm.equals("")){
            el.edit().putString("cat_names","پیشفرض;").apply();
        }

        loadWatchers();
        ConstraintLayout add_watcher = root.findViewById(R.id.add_watcher);
        add_watcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_title();
            }
        });
        tl = root.findViewById(R.id.tabLayout);
        final SharedPreferences e = getActivity().getSharedPreferences("watchers", Context.MODE_PRIVATE);
        String[] no = e.getString("cat_names", "").split(";");
        for(int i=0; i < no.length; i++){
            if(!no[i].equals("")) {
                tl.addTab(tl.newTab().setText(no[i]));
            }
        }



        tl.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                try {
                    addToList(wu.get(tl.getSelectedTabPosition()));

                }catch (JSONException |IndexOutOfBoundsException s){

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                try {
                    addToList(wu.get(tl.getSelectedTabPosition()));

                }catch (JSONException s){

                }
            }
        });


        handler = new Handler();

        String nl = e.getString("cat_names","");

        String c = "";
        int len = 0;
        String[] nA = nl.split(";");
        for(int i = 0; i < nA.length; i++){

            if(!nA[i].equals("")){
                c += e.getString("d"+nA[i],"");

            }
        }
        final String finalC = c;

        newThread(c);





       /* Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {

        }else {*/
            handler = new Handler();


            final Runnable r = new Runnable() {
                public void run() {
                    loadWatchers();
                    String n = e.getString("cat_names","");

                    String c = "";
                    int len = 0;
                    String[] nA = n.split(";");
                    for(int i = 0; i < nA.length; i++){

                        if(!nA[i].equals("")){
                            c += e.getString("d"+nA[i],"");

                        }
                    }
                    final String finalC = c;

                    newThread(c);

                    if(loaded){
                        if(getActivity() != null) {
                            SharedPreferences s = getActivity().getSharedPreferences("per", Context.MODE_PRIVATE);


                            if (s.getBoolean("permitted",true))
                            {

                                Log.e("per", permitted + "");
                                if (new NetworkChecker(getContext()).isNetworkAvailable()) {
                                    task = new Request().execute(Settings.JSON_ALL + finalC);
                                }

                            }
                            else {
                            }
                        }
                    }
                    try {
                        Log.e("refresh","ok");
                        addToList(wu.get(tl.getSelectedTabPosition()));
                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }
                    handler.postDelayed(this, 5000);
                }

            };

            handler.postDelayed(r, 5000);





            if(c.equals("")){
                return root;
            }
        return loading.addLoadingBar(false);
    }

    private void add_title() {
        final DialogPlus dialog = DialogPlus.newDialog(getActivity())
                .setContentHolder(new adapter(R.layout.dialog_add_new_wathcer, "", getActivity()))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(DialogPlus dialog, View view) {


                    }
                })
                .setGravity(Gravity.CENTER)
                .setExpanded(false)
                .create();
        dialog.show();

        View rview = dialog.getHolderView();
        final EditText ed = rview.findViewById(R.id.editTextTextPersonName2);
        ed.requestFocus();
        FancyButton submit = rview.findViewById(R.id.btn_add_filter);
        final FancyButton cancel = rview.findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ed.getText().toString().equals("")){

                }else {
                    SharedPreferences e = getActivity().getSharedPreferences("watchers", Context.MODE_PRIVATE);
                    String n = e.getString("cat_names", "");

                    if (n.contains(ed.getText().toString())) {

                        new ToastMaker(getContext(),"نام دیده بان تکراری است");
                    }else{

                    e.edit().putString("cat_names", n + ed.getText().toString() + ";").apply();
                    tl.addTab(tl.newTab().setText(ed.getText().toString()));
                    dialog.dismiss();
                }

                }
            }
        });
    }


    List<WUtils> wu;
    WatcherAdapter rb;

    private void loadWatchers() {
        wu = new ArrayList<>();

        rv = root.findViewById(R.id.favorite_recycler);

        SharedPreferences e = getActivity().getSharedPreferences("watchers",Context.MODE_PRIVATE);
        String n = e.getString("cat_names","");




        String[] c = null;
        int len = 0;
        String[] nA = n.split(";");
        for(int i = 0; i < nA.length; i++){

            if(!nA[i].equals("")){
                c = e.getString("d"+nA[i],"").split(";");

                len = e.getString("d"+nA[i],"").equals("") ? 0:c.length;


                wu.add(new WUtils(nA[i],len+""));
            }
        }

    }

    TextView emp;
    int starti = 0;
    private void addToList(WUtils item) throws JSONException{
        utils = new ArrayList<>();
        rv.setLayoutManager(new LinearLayoutManager(getContext()) );
        SharedPreferences e = getActivity().getSharedPreferences("watchers",Context.MODE_PRIVATE);
        String c = e.getString("d" + item.getName(), "");
        JSONArray ja = new JSONArray(result);
        for (int i = 0; i < ja.length(); i++) {
            emp.setVisibility(View.GONE);

            JSONObject jo = ja.getJSONObject(i);
            String name = jo.getString("name");
            if (c.contains(name)) {


                utils.add(new Utils(jo.getString("name"),
                        jo.getString("full_name"),
                        jo.getString("state"),
                        jo.getString("close_price"),
                        jo.getString("close_price_change"),
                        jo.getString("close_price_change_percent")));

            }
        }
        if(c.equals("")){
            emp.setVisibility(View.VISIBLE);


        }



        ra = new RecyclerAdapter(getContext(), old,utils, new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Utils item) {
                Intent in = new Intent(getActivity(), NamadRouter.class);
                in.putExtra(Settings.TITLE_EXTRA, item.getName());
                getActivity().startActivity(in);
                new AcTrans.Builder(getActivity()).performFade();
                permitted = false;
                task.cancel(true);
                SharedPreferences s = getActivity().getSharedPreferences("per", Context.MODE_PRIVATE);
                s.edit().putBoolean("permitted",false).apply();

            }
        }, new RecyclerAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(final Utils items) {

                DialogPlus dialog = DialogPlus.newDialog(getActivity())
                        .setContentHolder(new adapter(R.layout.dialog_dideban, items.getFName(), getActivity()))
                        .setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(DialogPlus dialog, View view) {


                                if (view.getId() == R.id.card_delete) {

                                    final SharedPreferences sp = getActivity().getSharedPreferences("favorite", Context.MODE_PRIVATE);
                                    String currentList = sp.getString("favorite_list", "");
                                    sp.edit().putString("favorite_list", currentList.replace("," + items.getName(), "")).apply();
                                    sp.edit().putBoolean(items.getName(), false).apply();
                                    utils.remove(items);

                                    ra.notifyDataSetChanged();
                                    rv.setLayoutManager(layoutManager);
                                    rv.setAdapter(ra);

                                    dialog.dismiss();
                                }
                            }
                        })
                        .setExpanded(false)
                        .create();
                dialog.show();


            }
        });


            rv.setAdapter(ra);

        starti = 1 ;


    }


    RecyclerAdapter ra;
    List<Utils> utils;
    LinearLayoutManager layoutManager;
    RecyclerView rv;

    public static Watchlist newInstance(String text) {

        Watchlist f = new Watchlist();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }
    void newThread(final String c) {


        HandlerThread handlerThread = new HandlerThread("HandlerNews2" );
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        handler.post(new Runnable() {


            @Override
            public void run() {


                        task = new Request().execute(Settings.JSON_ALL + c);


            }
        });


    }
    boolean update = false;
    List<Utils> old;
    int start = 0;


    public void ParseJSon(String res) throws JSONException {


        loading.cancel();

        old = utils;

        utils = new ArrayList<>();


        JSONArray ja = new JSONArray(res);
        for (int i = 0; i < ja.length(); i++) {
            JSONObject jo = ja.getJSONObject(i);
            String name = jo.getString("name");


            utils.add(new Utils(jo.getString("name"),
                    jo.getString("full_name"),
                    jo.getString("state"),
                    jo.getString("close_price"),
                    jo.getString("close_price_change"),
                    jo.getString("close_price_change_percent")));

        }



            ra = new RecyclerAdapter(getContext(), old,utils, new RecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Utils item) {
                    Intent in = new Intent(getActivity(), NamadRouter.class);
                    in.putExtra(Settings.TITLE_EXTRA, item.getName());
                    getActivity().startActivity(in);
                    new AcTrans.Builder(getActivity()).performFade();
                    permitted = false;
                    task.cancel(true);
                    SharedPreferences s = getActivity().getSharedPreferences("per", Context.MODE_PRIVATE);
                    s.edit().putBoolean("permitted",false).apply();

                }
            }, new RecyclerAdapter.OnItemLongClickListener() {
                @Override
                public void onItemLongClick(final Utils items) {

                    DialogPlus dialog = DialogPlus.newDialog(getActivity())
                            .setContentHolder(new adapter(R.layout.dialog_dideban, items.getFName(), getActivity()))
                            .setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(DialogPlus dialog, View view) {


                                    if (view.getId() == R.id.card_delete) {

                                        final SharedPreferences sp = getActivity().getSharedPreferences("favorite", Context.MODE_PRIVATE);
                                        String currentList = sp.getString("favorite_list", "");
                                        sp.edit().putString("favorite_list", currentList.replace("," + items.getName(), "")).apply();
                                        sp.edit().putBoolean(items.getName(), false).apply();
                                        utils.remove(items);

                                        ra.notifyDataSetChanged();
                                        rv.setLayoutManager(layoutManager);
                                        rv.setAdapter(ra);

                                        dialog.dismiss();
                                    }
                                }
                            })
                            .setExpanded(false)
                            .create();
                    dialog.show();


                }
            });






            loaded = true;





    }

    @Override
    public void onPause() {
        super.onPause();


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        permitted = false;
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


            if (!result.equals("")) {




                   loading.cancel();

                Watchlist.this.result = result;
                if(start == 0){
                    try {

                        addToList(new WUtils("پیشفرض",""));
                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }
                    start = 1;
                }




            }else{
                //new ToastMaker(getContext(),Settings.FAIL_RELOAD);
                Log.e("res empty","watchlist");
            }
        }
    }
    String result = "";


}