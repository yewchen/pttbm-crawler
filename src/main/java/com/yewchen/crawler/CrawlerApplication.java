package com.yewchen.crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import ptt.crawler.Reader;
import ptt.crawler.Violation;
import ptt.crawler.model.Article;
@SpringBootApplication
public class CrawlerApplication {

	/* 主程式 */
	public static void main(String[] args) {
		SpringApplication.run(CrawlerApplication.class, args);
	}
	
	/* 執行緒 */
	@Component
	class EventSubscriber implements DisposableBean, Runnable {

	    private Thread thread;
	    private volatile boolean someCondition = true;

	    EventSubscriber(){
	        this.thread = new Thread(this);
	        this.thread.start();
	    }

	    @Override
	    public void run(){
	        while(someCondition){
	            try {
	            	System.out.println("Start Crawler...");
	            	crawlerAndSetToFile();
					Thread.sleep(1000);
				} catch (Exception e) { e.printStackTrace(); }
	        }
	    }

	    @Override
	    public void destroy(){
	        someCondition = false;
	    }

	}
	
    public void crawlerAndSetToFile() throws ParseException, IOException {
    	
    	String diabloBMList = getDiabloBMList();
    	File initialFile = new File("src/main/resources/violation.txt");
    	OutputStream out = null;
		try {
			out = new FileOutputStream(initialFile);
			byte[] data = diabloBMList.getBytes();
			out.write(data);
		} catch (IOException e) { 
			out.close();
			e.printStackTrace(); 
		} 		
		out.close();
	}
	
	public String getDiabloBMList() throws IOException, ParseException {
		
	    /* 抓資料(今天到昨天的所有文章) */
		Reader reader = new Reader();
		List<Article> result = reader.getBMList("Diablo");
		List<Article> deleteList = new ArrayList<>();
		List<Article> violationList = new ArrayList<>();
		StringBuffer sb = new StringBuffer();
		sb.append("執行時間 : "+Reader.nowTime() + "<br>");
		
		/* 今天自刪文 */
		deleteList = new Violation().getDeleteSelfByDate(reader, result, Reader.laterDate(-0));
		/* 抓今天自刪競標文 */
		violationList = new Violation().getDeleteBidByDate(deleteList, Reader.laterDate(-0));
		sb.append(htmlList("今日自刪競標文清單", violationList));
		/* 抓今天自刪交易文 */
		violationList = new Violation().getDeleteTradeBidByDate(deleteList, result, Reader.laterDate(-0));
		sb.append(htmlList("今日自刪超貼交易文清單", violationList));
		/* 抓今天超貼違規文章 */
		violationList = new Violation().getExceedPostListByDate(result, Reader.laterDate(-0));
		sb.append(htmlList("今日交易文超貼清單", violationList));
		/* 抓今天標題無分類文章 */
		violationList = new Violation().getNoTagByDate(result, Reader.laterDate(-0));
		sb.append(htmlList("今日標題無分類清單", violationList));
		
		sb.append("<hr>");
		
		/* 昨日自刪文 */
		deleteList = new Violation().getDeleteSelfByDate(reader, result, Reader.laterDate(-1));
		/* 抓昨日自刪競標文 */
		violationList = new Violation().getDeleteBidByDate(deleteList, Reader.laterDate(-1));
		sb.append(htmlList("昨日自刪競標文清單", violationList));
		/* 抓昨日自刪交易文 */
		violationList = new Violation().getDeleteTradeBidByDate(deleteList, result, Reader.laterDate(-1));
		sb.append(htmlList("昨日自刪超貼交易文清單", violationList));
		/* 抓昨日超貼違規文章 */
		violationList = new Violation().getExceedPostListByDate(result, Reader.laterDate(-1));
		sb.append(htmlList("昨日交易文超貼清單", violationList));
		/* 抓昨日標題無分類文章 */
		violationList = new Violation().getNoTagByDate(result, Reader.laterDate(-1));
		sb.append(htmlList("昨日標題無分類清單", violationList));
		
		
		return sb.toString();
	}
	
	public StringBuffer htmlList(String title, List<Article> violationList) {
		StringBuffer sb = new StringBuffer();
		sb.append(title+"<br>");
		for ( Article article : violationList ) {
			sb.append(article.getDate()+" "+ article.getAuthor()+" "+article.getTitle()+"  ");
			sb.append("<a href=\"https://www.ptt.cc"+article.getUrl()+"\">https://www.ptt.cc"+article.getUrl()+"</a><br>");
		}
		return sb;
	}



}
