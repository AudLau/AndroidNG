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



public class NewsArticleDownloader extends AsyncTask<String, Void, String> {

    private NewsService newsService;
    private String key = "40a1258f47314e4a8df7c1d54fd35d41";
    private String url = "https://newsapi.org/v1/articles?";
    private ArrayList<Article> articleslist = new ArrayList<>();
    private String source;

    public NewsArticleDownloader (NewsService ns, String source){
        newsService = ns;
        this.source = source;
    }

    @Override
    protected void onPostExecute(String s) {
        parseJSON(s);
        newsService.setArticles(articleslist);
    }


    @Override
    protected String doInBackground(String... params) {

        //Build the url
        Uri.Builder buildURL = Uri.parse(url).buildUpon();
        buildURL.appendQueryParameter("apiKey", key);
        buildURL.appendQueryParameter("source", source);
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
            JSONArray jArrSources = jObject.getJSONArray("articles");
            for (int i = 0; i < jArrSources.length(); i++) {
                Article article = new Article();
                article.setAuthor(jArrSources.getJSONObject(i).getString("author"));
                article.setTitle(jArrSources.getJSONObject(i).getString("title"));
                article.setUrl(jArrSources.getJSONObject(i).getString("url"));
                article.setUrl2image(jArrSources.getJSONObject(i).getString("urlToImage"));
                article.setDate(jArrSources.getJSONObject(i).getString("publishedAt"));
                article.setDescription(jArrSources.getJSONObject(i).getString("description"));
                articleslist.add(article);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e(TAG, "articlelist: " + articleslist);
    }
}
