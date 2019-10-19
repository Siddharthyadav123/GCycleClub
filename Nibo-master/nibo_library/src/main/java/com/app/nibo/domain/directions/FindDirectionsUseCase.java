package com.app.nibo.domain.directions;

import com.app.nibo.domain.Params;
import com.app.nibo.domain.base.BaseUseCase;
import com.app.nibo.repo.contracts.IDirectionsRepository;
import com.app.nibo.utils.NiboConstants;

import io.reactivex.Observable;

/**
 * Created by aliumujib on 06/05/2018.
 */

public class FindDirectionsUseCase extends BaseUseCase {

    private IDirectionsRepository directionsRepository;

    public FindDirectionsUseCase(IDirectionsRepository directionsRepository) {
        this.directionsRepository = directionsRepository;
    }

    @Override
    protected Observable getObservable(Params params) {
        String destinationString = params.getString(NiboConstants.ORIGIN_PARAM, null);
        String originString = params.getString(NiboConstants.DESTINATION_PARAM, null);
        String apiKey = "AIzaSyAljXRCGbLQdwcObWcdUQS8Y8WlZWC2RTI";
        return directionsRepository.getRouteForPolyline(originString, destinationString, apiKey);
    }

}
