package com.app.nibo.repo.contracts;


import com.app.nibo.models.Route;

import java.util.List;

/**
 * Created by Abdul-Mujib Aliu on 4/3/2016.
 */
public interface DirectionFinderListener {
    void onDirectionFinderStart();

    void onDirectionFinderError(String errorMessage);

    void onDirectionFinderSuccess(List<Route> route);
}
