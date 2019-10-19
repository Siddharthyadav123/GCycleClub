package com.app.nibo.domain.places;

import com.app.nibo.domain.Params;
import com.app.nibo.domain.base.BaseUseCase;
import com.app.nibo.repo.contracts.ISuggestionRepository;
import com.app.nibo.utils.NiboConstants;

import io.reactivex.Observable;

/**
 * Created by aliumujib on 06/05/2018.
 */

public class GetPlaceSuggestionsUseCase extends BaseUseCase {
    private ISuggestionRepository suggestionRepository;

    public GetPlaceSuggestionsUseCase(ISuggestionRepository suggestionRepository) {
        this.suggestionRepository = suggestionRepository;
    }

    @Override
    protected Observable getObservable(Params params) {
        return suggestionRepository.getSuggestions(params.getString(NiboConstants.SUGGESTION_QUERY_PARAM, null));
    }
}
