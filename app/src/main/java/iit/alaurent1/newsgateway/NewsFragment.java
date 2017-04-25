package iit.alaurent1.newsgateway;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.DateFormat;
import java.text.ParseException;
import 	java.text.SimpleDateFormat;
import java.util.Date;

import com.squareup.picasso.Picasso;


public class NewsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "NEwsFragment";
    public static final String DATA_ARTICLE_FRAG = "DATA_ARTICLE_FRAG";
    public static final String DATA_INDICE = "DATA_INDICE";
    public static final String DATA_TOTAL = "DATA_TOTAL";
    private Article article;

    public static final NewsFragment newInstance(Article article, String indice, String total)
    {
        NewsFragment f = new NewsFragment();
        Bundle bdl = new Bundle(1);
        bdl.putSerializable(DATA_ARTICLE_FRAG, article);
        bdl.putString(DATA_INDICE, indice);
        bdl.putString(DATA_TOTAL, total);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.news_fragment, container, false);
        article =  (Article) getArguments().getSerializable(DATA_ARTICLE_FRAG);
        String position = getArguments().getString(DATA_INDICE);
        String total = getArguments().getString(DATA_TOTAL);

        TextView article_title = (TextView)v.findViewById(R.id.article_title);
        TextView article_author = (TextView)v.findViewById(R.id.article_author);
        TextView article_date = (TextView)v.findViewById(R.id.article_date);
        TextView article_preview = (TextView)v.findViewById(R.id.article_preview);
        TextView article_count = (TextView)v.findViewById(R.id.article_count);
        ImageView article_img = (ImageView)v.findViewById(R.id.article_img);

        article_title.setOnClickListener(this);
        article_preview.setOnClickListener(this);
        article_img.setOnClickListener(this);

        article_title.setText(article.getTitle());
        article_title.setText(article.getTitle());
        article_author.setText(article.getAuthor());
        article_preview.setText(article.getDescription());
        article_count.setText(Integer.parseInt(position)+1 + "/" + total);

        if(article.getDate()!= null) {
            DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            fromFormat.setLenient(false);
            DateFormat toFormat = new SimpleDateFormat("MM/dd/yyyy hh:mmaa");
            toFormat.setLenient(false);
            String dateStr = article.getDate();
            Date date;

            int count = 0;
            int maxTries = 2;
            boolean pass = false;
            while(!pass) {
                try {
                    date = fromFormat.parse(dateStr);
                    article_date.setText(toFormat.format(date));
                    pass = true;
                } catch (ParseException e) {
                    fromFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
                    if (++count == maxTries) {
                        pass=true;
                        article_date.setText("");
                    }
                }
            }

        }

        loadPhoto(article.getUrl2image(), v);

        return v;
    }

    private void loadPhoto(String url, View v){

        final ImageView article_img = (ImageView)v.findViewById(R.id.article_img);

        Picasso picasso = new Picasso.Builder(this.getContext())
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        picasso.load(R.drawable.brokenimage)
                                .into(article_img);
                    }
                })
                .build();

        picasso.load(url)
                .error(R.drawable.brokenimage)
                .placeholder(R.drawable.placeholder)
                .into(article_img);
    }

    @Override
    public void onClick(View v) {
        String url = article.getUrl();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            //Restore the fragment's state here
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's state here
    }
}