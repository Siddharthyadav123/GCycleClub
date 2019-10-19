package com.app.nibo.domain.places;

import com.app.nibo.domain.Params;
import com.app.nibo.domain.base.BaseUseCase;
import com.app.nibo.repo.contracts.ISuggestionRepository;
import com.app.nibo.utils.NiboConstants;

import io.reactivex.Observable;

/**
 * Created by aliumujib on 05/05/2018.
 */

public class GetPlaceDetailsUseCase extends BaseUseCase {

    ISuggestionRepository suggestionRepository;

    public GetPlaceDetailsUseCase(ISuggestionRepository suggestionRepository) {
        this.suggestionRepository = suggestionRepository;
    }

    @Override
    protected Observable getObservable(Params params) {
        String placeId = params.getString(NiboConstants.PLACE_ID_PARAM, null);
        return suggestionRepository.getPlaceByID(placeId);
    }

}
