package com.app.nibo.di;

import com.app.nibo.origindestinationpicker.OriginDestinationContracts;
import com.app.nibo.origindestinationpicker.OriginDestinationPickerPresenter;
import com.app.nibo.placepicker.NiboPickerContracts;
import com.app.nibo.placepicker.NiboPickerPresenter;

/**
 * Created by aliumujib on 05/05/2018.
 */

public class PresenterModule {

    InteractorModule interactorModule;

    public PresenterModule(InteractorModule interactorModule) {
        this.interactorModule = interactorModule;
    }


    public NiboPickerContracts.Presenter getNiboPickerPresenter() {
        return new NiboPickerPresenter(interactorModule.getGeocodeCordinatesUseCase(), interactorModule.getGetPlaceDetailsUseCase());
    }

    public OriginDestinationContracts.Presenter getOriginDestinationPickerPresenter() {
        return new OriginDestinationPickerPresenter(interactorModule.getFindDirectionsUseCase(), interactorModule.getPlaceSuggestionsUseCase(), interactorModule.getGetPlaceDetailsUseCase());
    }
}
