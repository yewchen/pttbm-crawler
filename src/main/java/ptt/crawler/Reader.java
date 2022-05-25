package ptt.crawler;

import org.jsoup.select.Elements;
import ptt.crawler.model.*;
import ptt.crawler.config.Config;

import okhttp3.*;
import org.jsoup.*;
import org.jsoup.nodes.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Reader {
    private OkHttpClient okHttpClient;
    private final Map<String, List<Cookie>> cookieStore; // 保存 Cookie
    private final CookieJar cookieJar;
    
    /* 今天日期 */
    String today = laterDate(-0);
    String today_later1 = laterDate(-1);
    String today_later2 = laterDate(-2);
    public static String laterDate(int x) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH,x);
    	return new SimpleDateFormat("M/dd").format(calendar.getTime());
    }
    public static String nowTime() {
        Calendar calendar = Calendar.getInstance();
    	return new SimpleDateFormat("YYYY/MM/dd HH:mm:ss").format(calendar.getTime());
    }

    public Reader() throws IOException {
        /* 初始化 */
        cookieStore = new HashMap<>();
        cookieJar = new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
                List<Cookie> cookies = cookieStore.getOrDefault(
                    httpUrl.host(), 
                    new ArrayList<>()
                );
                cookies.addAll(list);
                cookieStore.put(httpUrl.host(), cookies);
            }
            
            /* 每次發送帶上儲存的 Cookie */
            @Override
            public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                return cookieStore.getOrDefault(
                    httpUrl.host(), 
                    new ArrayList<>()
                );
            }
        };
        
        /* 不需要Proxy */
        okHttpClient = new OkHttpClient.Builder().cookieJar(cookieJar).build();
        
        /* 獲得網站的初始 Cookie */
        Request request = new Request.Builder().get().url(Config.PTT_URL).build();
        okHttpClient.newCall(request).execute();
    }

    public List<Article> getBMList(String boardName) throws IOException, ParseException {
        Board board = Config.BOARD_LIST.get(boardName);
        board.setUrl("/bbs/"+boardName);

        /* 如果看板需要成年檢查 */
        if (board.getAdultCheck() == true) {
            runAdultCheck(board.getUrl());
        }
        
        /* 開始撈資料 */
        List<Article> result = new ArrayList<>();
        boolean chgDay = false;
        do {
        	/* 抓取目標頁面 */
            Request request = new Request.Builder()
                .url(Config.PTT_URL + board.getUrl())
                .get()
                .build();

            Response response = okHttpClient.newCall(request).execute();
            String body = response.body().string();
            response.close();
            
            /* 抓出上一頁的URL */
            Document doc = Jsoup.parse(body);
            Elements articleList = doc.select(".action-bar .btn-group.btn-group-paging .btn.wide");
            for (Element element: articleList) {
            	if ("‹ 上頁".contains(element.text())) {
            		board.setUrl(element.attr("href"));
            	}
            }
            
            /* 轉換文章列表 HTML 到 Article */
            List<Map<String, String>> articles = parseArticle(body);
            for (Map<String, String> article: articles) {
            	
            	/* 過濾置底文 */
            	if ( article.get("title").contains("[公告]") ) continue;
            	/* 抓出自刪文章 */
            	boolean isActive = true;
            	String deleteAuthor = "";
            	if ( article.get("author").equals("-") ) {
            		deleteAuthor = article.get("source");
            		if ( article.get("source").contains("[") ) { // 只有自刪才會用[]符號把author標註起來, 板主刪文則會是<>標著author
            			deleteAuthor = article.get("source").substring(article.get("source").indexOf("[")+1, article.get("source").indexOf("]"));
            			isActive = false;
            		} else {
            			continue;
            		}
            	}
            	/* 跨日跳出 */
            	if ( today_later2.equals(article.get("date")) ) {
            		chgDay = true;
            		break;
            	}
            	
                String url = article.get("url");
                String title = article.get("title");
                String date = article.get("date");
                String author = article.get("author");
                
                if ( isActive )
                	result.add(new Article(board, url, title, author, date, isActive));
                else
                	result.add(new Article(board, url, title, deleteAuthor, date, isActive));	
            }
        } while ( chgDay == false );

        return result;
    }
    
    public List<Article> getAPList(String author) throws IOException, ParseException {
        Board board = Config.BOARD_LIST.get("Allpost");
        board.setUrl("/bbs/ALLPOST/search?q=author%3A"+author);

        /* 如果看板需要成年檢查 */
        if (board.getAdultCheck() == true) {
            runAdultCheck(board.getUrl());
        }
        
        /* 開始撈資料 */
        List<Article> result = new ArrayList<>();
        /* 抓取目標頁面 */
        Request request = new Request.Builder()
            .url(Config.PTT_URL + board.getUrl())
            .get()
            .build();

        Response response = okHttpClient.newCall(request).execute();
        String body = response.body().string();
        response.close();
        
        /* 轉換文章列表 HTML 到 Article */
        List<Map<String, String>> articles = parseArticle(body);
        for (Map<String, String> article: articles) {
        	
            String url = article.get("url");
            String title = article.get("title");
            String date = article.get("date");

            result.add(new Article(board, url, title, author, date, true));

        }

        return result;
    }
    
    public boolean checkPostActive(String url) throws IOException, ParseException {
        Board board = Config.BOARD_LIST.get("Diablo");
        board.setUrl(url);

        /* 如果看板需要成年檢查 */
        if (board.getAdultCheck() == true) {
            runAdultCheck(board.getUrl());
        }
        
        /* 抓取目標頁面 */
        Request request = new Request.Builder()
            .url(Config.PTT_URL + board.getUrl())
            .get()
            .build();

        Response response = okHttpClient.newCall(request).execute();
        String body = response.body().string();
        response.close();
        
        if ( body.contains("<title>404</title>") ) {
        	return false;
        }

        return true;
    }
    
    

    /* 進行年齡確認 */
    private void runAdultCheck(String url) throws IOException {
        FormBody formBody = new FormBody.Builder()
            .add("from", url)
            .add("yes", "yes")
            .build();

        Request request = new Request.Builder()
            .url(Config.PTT_URL + "/ask/over18")
            .post(formBody)
            .build();

        okHttpClient.newCall(request).execute();
    }

    /* 解析看板文章列表 */
    @SuppressWarnings("serial")
	private List<Map<String, String>> parseArticle(String body) {
        List<Map<String, String>> result = new ArrayList<>();
        Document doc = Jsoup.parse(body);
        Elements articleList = doc.select(".r-ent");
        
        for (Element element: articleList) {
            String url = element.select(".title a").attr("href");
            String title = element.select(".title a").text();
            String author = element.select(".meta .author").text();
            String date = element.select(".meta .date").text();
            String source = element.select(".title").html();

            result.add(new HashMap<String, String>(){{
                put("url", url);
                put("title", title);
                put("author", author);
                put("date", date);
                put("source", source);
            }});
        }

        return result;
    }
}
