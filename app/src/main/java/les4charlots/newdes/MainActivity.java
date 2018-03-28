package les4charlots.newdes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    //Déclaration de nos variables utilisées dans l'application
    String API_KEY = "8b3b7bac09784c4f8d91e217fa77c3f3";
    ListView listNews;
    ProgressBar loader;

    ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
    static final String KEY_AUTHOR = "name";
    static final String KEY_TITLE = "title";
    static final String KEY_DESCRIPTION = "description";
    static final String KEY_URL = "url";
    static final String KEY_URLTOIMAGE = "urlToImage";
    static final String KEY_PUBLISHEDAT = "publishedAt";

    //Création du menu contextuel en haut à droite
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //Gestion de la selection des options au sein du menu contextuel
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //Affichage au clic sur A propos
        if (id == R.id.action_settings) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.action_settings)
                    .setMessage(R.string.developed_by)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
            return true;
        }
        //Rafraichissement au clic sur rafraichir
        if (id == R.id.action_refresh) {
            if(Function.isNetworkAvailable(getApplicationContext()))
            {
                //Action de rafraichissement au clic sur rafraichir
                listNews.setAdapter(null);
                DownloadNews newsTask = new DownloadNews();
                newsTask.execute();
            }else{
                //Affichage de message d'erreur au clic
                Toast.makeText(getApplicationContext(), R.string.no_network, Toast.LENGTH_LONG).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //On récupère nos éléments
        listNews = (ListView) findViewById(R.id.listNews);
        loader = (ProgressBar) findViewById(R.id.loader);
        listNews.setEmptyView(loader);

        if(Function.isNetworkAvailable(getApplicationContext()))
        {
            //On télécharge les news si connexion internet
            DownloadNews newsTask = new DownloadNews();
            newsTask.execute();
        }else{
            //Message d'erreur si pas de connexion
            Toast.makeText(getApplicationContext(), R.string.no_network, Toast.LENGTH_LONG).show();
        }
    }

    //download la liste des news
    class DownloadNews extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        //On télécharge les news en arrière plan
        protected String doInBackground(String... args) {
            String xml = "";

            String urlParameters = "";
            xml = Function.excuteGet("https://newsapi.org/v2/top-headlines?country=fr&apiKey="+API_KEY, urlParameters);
            return  xml;
        }
        //On traite notre retour de l'api
        @Override
        protected void onPostExecute(String xml) {

            //On vérifie que le retour n'est pas vide
            if(xml.length()>10){

                try {
                    JSONObject jsonResponse = new JSONObject(xml);
                    JSONArray jsonArray = jsonResponse.optJSONArray("articles");
                    //On parcours l'ensemble du Json
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        JSONObject jsonSource = jsonObject.getJSONObject("source");
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_AUTHOR, jsonSource.optString(KEY_AUTHOR, "").toString());
                        map.put(KEY_TITLE, jsonObject.optString(KEY_TITLE, "").toString());
                        map.put(KEY_DESCRIPTION, jsonObject.optString(KEY_DESCRIPTION, "").toString());
                        map.put(KEY_URL, jsonObject.optString(KEY_URL, "").toString());
                        map.put(KEY_URLTOIMAGE, jsonObject.optString(KEY_URLTOIMAGE, "").toString());
                        String dateToConvert = jsonObject.optString(KEY_PUBLISHEDAT, "").toString();
                        map.put(KEY_PUBLISHEDAT,formatSpeDate(dateToConvert));
                        dataList.add(map);
                    }
                } catch (JSONException e) {
                    //Message d'erreur en cas de problème
                    Toast.makeText(getApplicationContext(), R.string.error_occured,  Toast.LENGTH_SHORT).show();
                }

                ListNewsAdapter adapter = new ListNewsAdapter(MainActivity.this, dataList);
                listNews.setAdapter(adapter);

                //Ajout du onClick sur chaque news
                listNews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Intent i = new Intent(MainActivity.this, DetailsActivity.class);
                        i.putExtra("url", dataList.get(+position).get(KEY_URL));
                        startActivity(i);
                    }
                });

            }else{
                //Message d'erreur dans le cas de pas de news
                Toast.makeText(getApplicationContext(), R.string.no_news, Toast.LENGTH_SHORT).show();
            }
        }

    }

    //Conversion de la date format UTC vers format jj-mm-yyyy hh-mm
    private String formatSpeDate(String dateToConvert){
        String firstPart = dateToConvert.substring(0,10);
        String[] splits = firstPart.split("-");
        firstPart = splits[2]+"-"+splits[1]+"-"+splits[0];
        String lastPart = dateToConvert.substring(11,16);
        return firstPart+" "+lastPart;
    }


}