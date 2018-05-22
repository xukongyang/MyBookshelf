package com.monke.monkeybook.model.content;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.model.AnalyzeRule.AnalyzeElement;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

public class SearchBook {

    public Observable<List<SearchBookBean>> analyzeSearchBook(final Response<String> response, String TAG, String name, BookSourceBean bookSourceBean) {
        return Observable.create(e -> {
            try {
                String baseURI;
                okhttp3.Response networkResponse = response.raw().networkResponse();
                if (networkResponse != null && networkResponse.request() != null) {
                    baseURI = networkResponse.request().url().toString();
                } else {
                    baseURI = response.raw().request().url().toString();
                }
                Document doc = Jsoup.parse(response.body());
                Elements booksE = AnalyzeElement.getElements(doc, bookSourceBean.getRuleSearchList());
                if (null != booksE && booksE.size() > 0) {
                    List<SearchBookBean> books = new ArrayList<>();
                    for (int i = 0; i < booksE.size(); i++) {
                        SearchBookBean item = new SearchBookBean();
                        item.setTag(TAG);
                        item.setOrigin(name);
                        AnalyzeElement analyzeElement = new AnalyzeElement(booksE.get(i), baseURI);
                        item.setAuthor(FormatWebText.getAuthor(analyzeElement.getResult(bookSourceBean.getRuleSearchAuthor())));
                        item.setKind(analyzeElement.getResult(bookSourceBean.getRuleSearchKind()));
                        item.setLastChapter(analyzeElement.getResult(bookSourceBean.getRuleSearchLastChapter()));
                        item.setName(analyzeElement.getResult(bookSourceBean.getRuleSearchName()));
                        item.setNoteUrl(analyzeElement.getResult(bookSourceBean.getRuleSearchNoteUrl()));
                        if (isEmpty(item.getNoteUrl())) {
                            item.setNoteUrl(baseURI);
                        }
                        item.setCoverUrl(analyzeElement.getResult(bookSourceBean.getRuleSearchCoverUrl()));
                        if (!isEmpty(item.getName())) {
                            books.add(item);
                        }
                    }
                    e.onNext(books);
                } else {
                    e.onNext(new ArrayList<>());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                e.onNext(new ArrayList<>());
            }
            e.onComplete();
        });
    }
}
