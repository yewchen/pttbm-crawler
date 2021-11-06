package ptt.crawler.model;

public class Article {
    private Board parent; // 所屬板塊
    private String url; // 網址
    private String title; // 標題
    private String body; // 內容
    private String author; // 作者
    private String date; // 發文時間
    private String source; //原始資料
    private boolean isActive=true;

    public Article(Board parent, String url, String title, String author, String date, boolean isActive) {
        this.parent = parent;
        this.url = url;
        this.title = title;
        this.author = author;
        this.date = date;
        this.isActive = isActive;
    }

    public Board getParent() {
        return parent;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }
    
    public boolean getIsActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive=isActive; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source=source; }
    
    @Override
    public String toString() {
        return String.format("Article{ url='%s', title='%s', body='%s', author='%s', date='%s' }", url, title, body, author, date);
    }
}
