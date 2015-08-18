package org.cryse.widget.persistentsearch.sample;

import android.content.Context;

import org.cryse.widget.persistentsearch.SearchItem;
import org.cryse.widget.persistentsearch.SearchSuggestionsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SampleSuggestionsBuilder implements SearchSuggestionsBuilder {
    private Context mContext;
    private List<SearchItem> mHistorySuggestions = new ArrayList<SearchItem>();;

    public SampleSuggestionsBuilder(Context context) {
        this.mContext = context;
        createHistorys();
    }

    private void createHistorys() {
        SearchItem item1 = new SearchItem(
                "Isaac Newton",
                "Isaac Newton",
                SearchItem.TYPE_SEARCH_ITEM_HISTORY
        );
        mHistorySuggestions.add(item1);
        SearchItem item2 = new SearchItem(
                "Albert Einstein",
                "Albert Einstein",
                SearchItem.TYPE_SEARCH_ITEM_HISTORY
        );
        mHistorySuggestions.add(item2);
        SearchItem item3 = new SearchItem(
                "John von Neumann",
                "John von Neumann",
                SearchItem.TYPE_SEARCH_ITEM_HISTORY
        );
        mHistorySuggestions.add(item3);
        SearchItem item4 = new SearchItem(
                "Alan Mathison Turing",
                "Alan Mathison Turing",
                SearchItem.TYPE_SEARCH_ITEM_HISTORY
        );
        mHistorySuggestions.add(item4);
    }

    @Override
    public Collection<SearchItem> buildEmptySearchSuggestion(int maxCount) {
        List<SearchItem> items = new ArrayList<SearchItem>();
        items.addAll(mHistorySuggestions);
        return items;
    }

    @Override
    public Collection<SearchItem> buildSearchSuggestion(int maxCount, String query) {
        List<SearchItem> items = new ArrayList<SearchItem>();
        if(query.startsWith("@")) {
            SearchItem peopleSuggestion = new SearchItem(
                    "Search People: " + query.substring(1),
                    query,
                    SearchItem.TYPE_SEARCH_ITEM_SUGGESTION
            );
            items.add(peopleSuggestion);
        } else if(query.startsWith("#")) {
            SearchItem toppicSuggestion = new SearchItem(
                    "Search Topic: " + query.substring(1),
                    query,
                    SearchItem.TYPE_SEARCH_ITEM_SUGGESTION
            );
            items.add(toppicSuggestion);
        } else {
            SearchItem peopleSuggestion = new SearchItem(
                    "Search People: " + query,
                    "@" + query,
                    SearchItem.TYPE_SEARCH_ITEM_SUGGESTION
            );
            items.add(peopleSuggestion);
            SearchItem toppicSuggestion = new SearchItem(
                    "Search Topic: " + query,
                    "#" + query,
                    SearchItem.TYPE_SEARCH_ITEM_SUGGESTION
            );
            items.add(toppicSuggestion);
        }
        for(SearchItem item : mHistorySuggestions) {
            if(item.getValue().startsWith(query)) {
                items.add(item);
            }
        }
        return items;
    }
}
