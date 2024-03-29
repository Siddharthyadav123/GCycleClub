package com.app.nibo.di;

import com.app.nibo.repo.contracts.IDirectionsRepository;
import com.app.nibo.repo.contracts.IGeoCodingRepository;
import com.app.nibo.repo.directions.DirectionsRepository;
import com.app.nibo.repo.location.GeoCodingRepository;

/**
 * Created by aliumujib on 03/05/2018.
 */

public class RepositoryModule {

    private static RepositoryModule repositoryModule;

    public static RepositoryModule getInstance(APIModule apiModule) {
        if (repositoryModule == null) {
            repositoryModule = new RepositoryModule(apiModule);
        }
        return repositoryModule;
    }

    private IDirectionsRepository directionsRepository;
    private IGeoCodingRepository geoCodingRepository;

    APIModule apiModule;

    private RepositoryModule(APIModule apiModule) {
        this.apiModule = apiModule;
    }

    public IDirectionsRepository getDirectionsRepository() {
        if (directionsRepository == null) {
            directionsRepository = new DirectionsRepository(apiModule.getDirectionsAPI());
        }
        return directionsRepository;
    }

    public IGeoCodingRepository getGeoCodingRepository() {
        if (geoCodingRepository == null) {
            geoCodingRepository = new GeoCodingRepository(apiModule.getGeocodeAPI());
        }
        return geoCodingRepository;
    }



}
