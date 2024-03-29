package com.app.nibo.placepicker;

import com.app.nibo.autocompletesearchbar.NiboSearchSuggestionItem;
import com.app.nibo.autocompletesearchbar.SearchSuggestionsBuilder;
import com.app.nibo.repo.location.SuggestionsProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.reactivex.Observable;

public class PlaceSuggestionsBuilder implements SearchSuggestionsBuilder {
    private List<NiboSearchSuggestionItem> mHistorySuggestions = new ArrayList<>();

    private String TAG = getClass().getSimpleName();
    private SuggestionsProvider suggestionsProvider;

    public PlaceSuggestionsBuilder(SuggestionsProvider suggestionsProvider) {
        this.suggestionsProvider = suggestionsProvider;
    }

    @Override
    public Collection<NiboSearchSuggestionItem> buildEmptySearchSuggestion(int maxCount) {
        List<NiboSearchSuggestionItem> items = new ArrayList<>();
        items.addAll(mHistorySuggestions);
        return items;
    }


    @Override
    public Observable<Collection<NiboSearchSuggestionItem>> rXbuildSearchSuggestion(int maxCount, final String query) {
        return suggestionsProvider.getSuggestions(query);
    }
}
