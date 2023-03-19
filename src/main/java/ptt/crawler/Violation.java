package ptt.crawler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ptt.crawler.model.Article;

public class Violation {
	
	//因為自刪文有時候會在標題列被清空, 所以不見得查得到
	public List<Article> getDeleteSelfByDate(Reader reader, List<Article> result, String date) {
		List<Article> violation = new ArrayList<>();
		try {
            
            /* 1. 自刪文 */
            System.out.println(date+" 自刪文清單：");
            Set<String> checkedUrl = new HashSet<>();
            for ( Article article : result ) {
            	
            	/* 檢查日期 */
            	if ( !article.getDate().equals(date) ) continue;
            	
            	/* 文章已不存在 */
            	if ( !article.getIsActive() ) {
            		
            		/* 去ALLPOST查該作者之發文 */
            		List<Article> apResult = reader.getAPList(article.getAuthor());
            		for ( Article apArticle : apResult ) {
            			/* 查出跟刪文記錄同一篇的文章 */
            			if ( apArticle.getDate().equals(article.getDate()) ) {
            				/* 查該作者有刪文記錄當天 , 有發過交易文or競標文的文章 */
            				if ( (apArticle.getTitle().contains("交易") && !apArticle.getTitle().contains("Re:")) ||
            					 (apArticle.getTitle().contains("競標") && !apArticle.getTitle().contains("Re:")) ) {
            					/* 檢查該交易文or競標文是否還存在 */
            					if ( reader.checkPostActive(apArticle.getUrl()) == false ) {
            						/* 若該文章已檢查過, 則不紀錄 */
            						if ( checkedUrl.contains(apArticle.getUrl()) ) continue;
            						violation.add(apArticle);
            						checkedUrl.add(apArticle.getUrl());
            					}
            						
            				}
            			}
            		}
            	}
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return violation;
	}
	
	public List<Article> getDeleteBidByDate(List<Article> deleteResult, String date) {
		List<Article> violation = new ArrayList<>();
		try {
            System.out.println(date+" 自刪競標文清單：");
            for ( Article article : deleteResult ) {
            	/* 檢查日期 && 過濾刪除 */
            	if ( !article.getDate().equals(date) || !article.getIsActive() ) continue;
            	/* 檢查標題分類 */
            	if ( article.getTitle().contains("競標") ) {
            		violation.add(article);
            	}
            }
        } catch (Exception e) { e.printStackTrace(); }
		return violation;
	}
	
	public List<Article> getDeleteTradeBidByDate(List<Article> deleteResult, List<Article> allResult, String date) {
		List<Article> violation = new ArrayList<>();
		try {
            System.out.println(date+" 自刪交易文清單：");
            for ( Article deleteArticle : deleteResult ) {
            	/* 檢查日期 && 過濾刪除 */
            	if ( !deleteArticle.getDate().equals(date) || !deleteArticle.getIsActive() ) continue;
            	/* 檢查標題分類 */
            	if ( deleteArticle.getTitle().contains("交易") ) {
            		/* 檢查今天有沒有發過交易文 */
            		for ( Article article : allResult ) {
            			if ( !article.getDate().equals(date) || !article.getIsActive() ) continue;
            			/* 若自刪交易文的作者等, 同一天仍有發交易文 */
            			if ( deleteArticle.getAuthor().equals(article.getAuthor()) ) {
            				if ( article.getTitle().contains("交易") && !article.getTitle().contains("Re:") ) {
            					violation.add(deleteArticle);
            					violation.add(article);
            				}
            			}
            		}
            	}
            }
        } catch (Exception e) { e.printStackTrace(); }
		return violation;
	}
	
	public List<Article> getNoTagByDate(List<Article> result, String date) {
		List<Article> violation = new ArrayList<>();
		try {
            
            /* 1. 無分類文檢查 */
            System.out.println(date+" 無分類文清單：");
            for ( Article article : result ) {
            	
            	/* 檢查日期 && 過濾刪除 */
            	if ( !article.getDate().equals(date) || !article.getIsActive() ) continue;
            	
            	/* 檢查標題分類 */
            	if ( !article.getTitle().contains("[") ) {
            		violation.add(article);
            	}
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return violation;
	}
	
	public List<Article> getExceedPostListByDate(List<Article> result, String date) {
		
		List<Article> violation = new ArrayList<>();
		
        try {
            Set<String> checkedAuthor = new HashSet<>();
            
            /* 1. 交易文檢查 */
            System.out.println(date+" 交易文超貼清單：");
            for ( Article article : result ) {
            	
            	/* 檢查日期 && 過濾刪除 */
            	if ( !article.getDate().equals(date) || !article.getIsActive() ) continue;
            	
            	/* 若該作者已檢查過, 則不再執行 */
            	if ( checkedAuthor.contains(article.getAuthor()) ) continue;
            	
            	/* 檢查交易文類 */
            	if ( article.getTitle().contains("[交易]") || article.getTitle().contains("[競標]") ) {
            		int tradeCnt=0;
            		/* 根據作者遍歷其所有交易文 */
            		List<Article> list = new ArrayList<>();
            		for ( Article chk : result ) {
            			/* 檢查日期 */
                    	if ( !chk.getDate().equals(date) ) continue;
            			if ( chk.getAuthor().equals(article.getAuthor()) ) {
            				if ( chk.getTitle().contains("[交易]") || chk.getTitle().contains("[競標]") ) {
            					tradeCnt++;
            					list.add(chk);
            				}
            			}
            		}
            		/* 若兩篇交易文皆為競標回文, 則跳過 */
            		if ( (tradeCnt == 2) &&
            				(list.get(0).getTitle().contains("Re:") || list.get(1).getTitle().contains("Re:"))	) {
            			continue;
            		}
            		/* 若交易文數量超過1 或 競標文超過2篇 */
            		if ( tradeCnt > 1 ) {
            			checkedAuthor.add(article.getAuthor());	//紀錄已查詢過之作者
            			for ( Article atc : list  ) {
            				violation.add(atc);
            			}
            		}
            	}
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return violation;

	}
	
    
}

