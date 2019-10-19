package com.app.nibo.repo.contracts;

import com.app.nibo.models.Route;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by aliumujib on 03/05/2018.
 */

public interface IDirectionsRepository {


    Observable<List<Route>> getRouteForPolyline(String origin, String destination,  String apiKey);

}
