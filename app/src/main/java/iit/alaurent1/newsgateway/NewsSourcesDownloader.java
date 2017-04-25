package iit.alaurent1.newsgateway;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;


public class NewsSourcesDownloader extends AsyncTask<String, Void, String> {

    private MainActivity mainActivity;
    private String key = "40a1258f47314e4a8df7c1d54fd35d41";
    private String url = "https://newsapi.org/v1/sources?";
    private ArrayList<Source> sourcelist = new ArrayList<>();
    private ArrayList<String> categorylist = new ArrayList<>();
    private String category;

    public NewsSourcesDownloader (MainActivity ma, String category){
        mainActivity = ma;
        if(category.equalsIgnoreCase("all") || category.isEmpty()){
            this.category = "";
        } else {
            this.category = category;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        parseJSON(s);
        mainActivity.setSources(sourcelist, categorylist);
    }



    @Override
    protected String doInBackground(String... params) {

        //Build the url
        Uri.Builder buildURL = Uri.parse(url).buildUpon();
        buildURL.appendQueryParameter("language", "en");
        buildURL.appendQueryParameter("country", "us");
        buildURL.appendQueryParameter("apiKey", key);
        buildURL.appendQueryParameter("category", category);
        String urlToUse = buildURL.build().toString();

        Log.d(TAG, "doInBackground: " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.e(TAG, "doInBackground: " + sb.toString());

        } catch (Exception e) {
            Log.e(TAG, "doInBackground: ", e);
            return null;
        }
        return sb.toString();
    }


    private void parseJSON(String s) {
        try {
            JSONObject jObject = new JSONObject(s);
            JSONArray jArrSources = jObject.getJSONArray("sources");
            for (int i = 0; i < jArrSources.length(); i++) {
                Source source = new Source();
                source.setId(jArrSources.getJSONObject(i).getString("id"));
                source.setName(jArrSources.getJSONObject(i).getString("name"));
                source.setUrl(jArrSources.getJSONObject(i).getString("url"));
                source.setCategory(jArrSources.getJSONObject(i).getString("category"));
                sourcelist.add(source);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        for(int i=0; i<sourcelist.size(); i++){
            if( !categorylist.contains(sourcelist.get(i).getCategory()) ){
                categorylist.add(sourcelist.get(i).getCategory());
            }
        }

        Log.e(TAG, "sourcelist: " + sourcelist + "categorylist: " + categorylist);
    }
}
