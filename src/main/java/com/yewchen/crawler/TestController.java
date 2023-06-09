package com.yewchen.crawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ptt.crawler.Reader;
import ptt.crawler.Violation;
import ptt.crawler.model.Article;

@RestController
public class TestController {
	
	@GetMapping("/abc")
	public String test() throws IOException, ParseException {
		
		String diabloBMList = "TEST123456789";
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
		
		return "abc123";
	}
	
	@GetMapping("/123")
	public String tesa() throws IOException, ParseException {
		
		File initialFile = new File("src/main/resources/violation.txt");
	    InputStream in = null;
	    String res = "null";
	    try {
	    	in = new FileInputStream(initialFile);
	    	byte[] data = new byte[40960];
	    	in.read(data);
		    res = new String(data);
	    } catch ( Exception e ) {
	    	in.close();
	    	e.printStackTrace(); 
	    }
	    in.close();
		return res;
	}
	
	
	
	@GetMapping("/slow")
	public String index() throws IOException, ParseException {
		
	    /* 抓資料(今天到昨天的所有文章) */
		Reader reader = new Reader();
		List<Article> result = reader.getBMList("Diablo");
		List<Article> deleteList = new ArrayList<>();
		List<Article> violationList = new ArrayList<>();
		StringBuffer sb = new StringBuffer();
		
		/* 今天自刪文 */
		deleteList = new Violation().getDeleteSelfByDate(reader, result, Reader.laterDate(-0));
		/* 抓今天自刪競標文 */
		violationList = new Violation().getDeleteBidByDate(deleteList, Reader.laterDate(-0));
		sb.append(htmlList("今日自刪競標文清單", violationList));
		/* 抓今天自刪交易文 */
		violationList = new Violation().getDeleteTradeBidByDate(deleteList, result, Reader.laterDate(-0));
		sb.append(htmlList("今日自刪交易文清單", violationList));
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
		sb.append(htmlList("昨日自刪交易文清單", violationList));
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
