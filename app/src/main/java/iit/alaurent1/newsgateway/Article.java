package iit.alaurent1.newsgateway;

import java.io.Serializable;

public class Article implements Serializable{
    private String author;
    private String title;
    private String description;
    private String url;
    private String url2image;
    private String date;


    public Article() {
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl2image() {
        return url2image;
    }

    public void setUrl2image(String url2image) {
        this.url2image = url2image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
