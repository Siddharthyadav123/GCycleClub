package com.app.nibo.origindestinationpicker;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.nibo.R;
import com.app.nibo.autocompletesearchbar.NiboSearchSuggestionItem;
import com.app.nibo.base.BaseNiboFragment;
import com.app.nibo.gps.Data;
import com.app.nibo.gps.GpsServices;
import com.app.nibo.lib.BottomSheetBehaviorGoogleMapsLike;
import com.app.nibo.models.NiboSelectedOriginDestination;
import com.app.nibo.models.Route;
import com.app.nibo.origindestinationpicker.adapter.NiboBaseOrigDestSuggestionAdapter;
import com.app.nibo.utils.NiboConstants;
import com.app.nibo.utils.NiboStyle;
import com.app.nibo.utils.customviews.RoundedView;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static com.google.android.gms.maps.model.JointType.ROUND;

/**
 * A placeholder fragment containing a simple view.
 */
public class NiboOriginDestinationPickerFragment extends BaseNiboFragment<OriginDestinationContracts.Presenter>
        implements OriginDestinationContracts.View, LocationListener, GpsStatus.Listener {

    private CoordinatorLayout mCoordinatorlayout;
    private Toolbar mToolbar;
    private RoundedView mRoundedIndicatorOrigin;
    private RoundedView mRoundedIndicatorDestination;
    private EditText mOriginEditText;
    private EditText mDestinationEditText;
    private NestedScrollView mBottomSheet;
    private CardView mContentCardView;
    private ListView mSuggestionsListView;
    private ProgressBar mProgressBar;
    private NiboBaseOrigDestSuggestionAdapter mSearchItemAdapter;
    private ArrayList<NiboSearchSuggestionItem> mSearchSuggestions;
    private BottomSheetBehaviorGoogleMapsLike<View> mBehavior;
    private FloatingActionButton mDoneFab;


    private ArrayList<LatLng> mListLatLng = new ArrayList<>();

    private Marker mOriginMapMarker;
    private Marker mDestinationMarker;
    private Polyline mPrimaryPolyLine;
    private Polyline mSecondaryPolyLine;

    private TextView mTimeTaken;
    private TextView mOriginToDestinationTv;
    private LinearLayout mTimeDistanceLL;
    private LinearLayout mSuggestionsLL;
    private String mOriginEditTextHint;
    private String mDestinationEditTextHint;
    private int mOriginMarkerPinIconRes;
    private int mDestinationMarkerPinIconRes;
    private int mBackButtonIconRes;
    private int mTextFieldClearIconRes;
    private int mPrimaryPolyLineColor;
    private int mSecondaryPolyLineColor;

    //speed
    private static Data data;
    private Data.OnGpsServiceUpdate onGpsServiceUpdate;
    private LocationManager mLocationManager;

    private TextView titleSpeed;
    private TextView titleDistance;
    private TextView titleAvgSpeed;
    private TextView currentSpeed;
    private TextView averageSpeed;
    private TextView distance;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSearchSuggestions = new ArrayList<>();
        mSearchItemAdapter = new NiboBaseOrigDestSuggestionAdapter(getContext(), mSearchSuggestions);
        initView(view);
        initListeners();
        Bundle args;
        if ((args = getArguments()) != null) {

            mMarkerPinIconRes = args.getInt(NiboConstants.MARKER_PIN_ICON_RES);

            mOriginEditTextHint = args.getString(NiboConstants.ORIGIN_EDIT_TEXT_HINT_ARG);
            mDestinationEditTextHint = args.getString(NiboConstants.DEST_EDIT_TEXT_HINT_ARG);

            mOriginMarkerPinIconRes = args.getInt(NiboConstants.ORIGIN_MARKER_ICON_RES_ARG);
            mDestinationMarkerPinIconRes = args.getInt(NiboConstants.DEST_MARKER_ICON_RES_ARG);

            mBackButtonIconRes = args.getInt(NiboConstants.BACK_BUTTON_ICON_RES_ARG);
            mTextFieldClearIconRes = args.getInt(NiboConstants.TEXT_FIELD_CLEAR_ICON_RES_ARG);

            mPrimaryPolyLineColor = args.getInt(NiboConstants.PRIMARY_POLY_LINE_COLOR_RES);
            mSecondaryPolyLineColor = args.getInt(NiboConstants.SECONDARY_POLY_LINE_COLOR_RES);

        }

        if (mOriginEditTextHint != null) {
            mOriginEditText.setHint(mOriginEditTextHint);
        }

        if (mDestinationEditTextHint != null) {
            mDestinationEditText.setHint(mDestinationEditTextHint);
        }

        if (mBackButtonIconRes != 0) {
            mToolbar.setNavigationIcon(mBackButtonIconRes);
        }

        if (mTextFieldClearIconRes != 0) {
            mOriginEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, mTextFieldClearIconRes, 0);
            mDestinationEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, mTextFieldClearIconRes, 0);
        }
        initMap();
    }

    public static NiboOriginDestinationPickerFragment newInstance(String originEditTextHint, String destinationEditTextHint, NiboStyle mapStyle,
                                                                  int styleFileID, int originMarkerPinIconRes, int destinationMarkerPinIconRes, int backButtonIconRes,
                                                                  int textFieldClearIconRes, int primaryPolyLineColor, int secondaryPolyLineColor) {

        Bundle args = new Bundle();
        args.putString(NiboConstants.ORIGIN_EDIT_TEXT_HINT_ARG, originEditTextHint);
        args.putString(NiboConstants.DEST_EDIT_TEXT_HINT_ARG, destinationEditTextHint);
        args.putSerializable(NiboConstants.STYLE_ENUM_ARG, mapStyle);
        args.putInt(NiboConstants.STYLE_FILE_ID, styleFileID);

        args.putInt(NiboConstants.ORIGIN_MARKER_ICON_RES_ARG, originMarkerPinIconRes);
        args.putInt(NiboConstants.DEST_MARKER_ICON_RES_ARG, destinationMarkerPinIconRes);
        args.putInt(NiboConstants.BACK_BUTTON_ICON_RES_ARG, backButtonIconRes);
        args.putInt(NiboConstants.TEXT_FIELD_CLEAR_ICON_RES_ARG, textFieldClearIconRes);

        args.putInt(NiboConstants.PRIMARY_POLY_LINE_COLOR_RES, primaryPolyLineColor);
        args.putInt(NiboConstants.SECONDARY_POLY_LINE_COLOR_RES, secondaryPolyLineColor);

        NiboOriginDestinationPickerFragment fragment = new NiboOriginDestinationPickerFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public NiboOriginDestinationPickerFragment() {
    }

    @Override
    protected void extractGeocode(double lati, double longi) {

    }

    @Override
    protected void handleLocationRetrieval(Location latLng) {

    }


    private void sendRequest(LatLng origin, LatLng destination) {
        presenter.findDirections(origin.latitude, origin.longitude, destination.latitude, destination.longitude);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_origin_destination_picker;
    }


    @Override
    public void onMarkerDragStart(Marker marker) {
        super.onMarkerDragStart(marker);
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    private void initView(View convertView) {
        this.mCoordinatorlayout = (CoordinatorLayout) convertView.findViewById(R.id.coordinatorlayout);
        this.mToolbar = (Toolbar) convertView.findViewById(R.id.toolbar);
        this.mRoundedIndicatorOrigin = (RoundedView) convertView.findViewById(R.id.rounded_indicator_source);
        this.mRoundedIndicatorDestination = (RoundedView) convertView.findViewById(R.id.rounded_indicator_destination);
        this.mOriginEditText = (EditText) convertView.findViewById(R.id.origin_edit_text);
        this.mDestinationEditText = (EditText) convertView.findViewById(R.id.destination_edit_text);
        this.mBottomSheet = (NestedScrollView) convertView.findViewById(R.id.bottom_sheet);
        this.mContentCardView = (CardView) convertView.findViewById(R.id.content_card_view);
        this.mSuggestionsListView = (ListView) convertView.findViewById(R.id.suggestions_list);
        this.mProgressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
        this.mDoneFab = (FloatingActionButton) convertView.findViewById(R.id.done_fab);
        this.mTimeDistanceLL = (LinearLayout) convertView.findViewById(R.id.title_distance_ll);
        this.mSuggestionsLL = (LinearLayout) convertView.findViewById(R.id.suggestions_progress_ll);
        this.mTimeTaken = (TextView) convertView.findViewById(R.id.time_taken);
        this.mOriginToDestinationTv = (TextView) convertView.findViewById(R.id.origin_to_destination_tv);
        this.mOriginDestinationSeperatorLine = convertView.findViewById(R.id.orig_dest_seperator_line);

        //this.mRoundedIndicatorDestination.setChecked(true);
        this.mSuggestionsListView.setAdapter(mSearchItemAdapter);

        this.mOriginEditText.setOnTouchListener(getClearListener(mOriginEditText));
        this.mDestinationEditText.setOnTouchListener(getClearListener(mDestinationEditText));

        View bottomSheet = mCoordinatorlayout.findViewById(R.id.bottom_sheet);
        currentSpeed = convertView.findViewById(R.id.currentSpeed);
        averageSpeed = convertView.findViewById(R.id.avg_speed);
        distance = convertView.findViewById(R.id.distance);
        titleSpeed = convertView.findViewById(R.id.titleSpeed);
        titleDistance = convertView.findViewById(R.id.titleDistance);
        titleAvgSpeed = convertView.findViewById(R.id.titleAvgSpeed);
        setVisibilityOfSpeedLayout(View.GONE);

        this.mToolbar.setOnMenuItemClickListener(item -> {
            getActivity().finish();
            return false;
        });

        this.mDoneFab.setOnClickListener(v -> {
            if (!data.isRunning()) {
                bottomSheet.setVisibility(View.GONE);
                setVisibilityOfSpeedLayout(View.VISIBLE);
                data.setRunning(true);
                data.setFirstTime(true);
                getActivity().startService(new Intent(getActivity(), GpsServices.class));
            } else {
                setVisibilityOfSpeedLayout(View.GONE);
                data.setRunning(false);
                getActivity().stopService(new Intent(getActivity(), GpsServices.class));
            }
        });


        mBehavior = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);
        mBehavior.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                        if (mTimeDistanceLL.getVisibility() != View.VISIBLE) {
                            toggleViews();
                        }
                        mDoneFab.animate().scaleX(1).scaleY(1).setDuration(300).start();
                        mCenterMyLocationFab.animate().scaleX(1).scaleY(1).setDuration(300).start();
                        hideKeyboard();
                        Log.d(TAG, "STATE_COLLAPSED");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                        mDoneFab.animate().scaleX(0).scaleY(0).setDuration(300).start();
                        mCenterMyLocationFab.animate().scaleX(0).scaleY(0).setDuration(300).start();

                        Log.d(TAG, "STATE_DRAGGING");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                        Log.d(TAG, "STATE_EXPANDED");
                        mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:
                        if (mSuggestionsLL.getVisibility() != View.VISIBLE) {
                            toggleViews();
                        }

                        mDoneFab.animate().scaleX(0).scaleY(0).setDuration(300).start();
                        mCenterMyLocationFab.animate().scaleX(0).scaleY(0).setDuration(300).start();

                        mSearchSuggestions.clear();
                        mSearchItemAdapter.clear();
                        mSearchItemAdapter.notifyDataSetChanged();
                        Log.d(TAG, "STATE_ANCHOR_POINT");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN:
                        Log.d(TAG, "STATE_HIDDEN");
                        break;
                    default:
                        Log.d(TAG, "STATE_SETTLING");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        this.mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
        initSpeedRelatedStuffs();
    }

    private void setVisibilityOfSpeedLayout(int visibility) {
        currentSpeed.setVisibility(visibility);
        averageSpeed.setVisibility(visibility);
        distance.setVisibility(visibility);
        titleSpeed.setVisibility(visibility);
        titleDistance.setVisibility(visibility);
        titleAvgSpeed.setVisibility(visibility);
    }

    private void initSpeedRelatedStuffs() {
        data = new Data(onGpsServiceUpdate);
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        onGpsServiceUpdate = new Data.OnGpsServiceUpdate() {
            @Override
            public void update() {
                double maxSpeedTemp = data.getMaxSpeed();
                double distanceTemp = data.getDistance();
                double averageTemp;
//                if (sharedPreferences.getBoolean("auto_average", false)) {
//                    averageTemp = data.getAverageSpeedMotion();
//                } else {
                averageTemp = data.getAverageSpeed();
//                }

                String speedUnits;
                String distanceUnits;
//                if (sharedPreferences.getBoolean("miles_per_hour", false)) {
//                    maxSpeedTemp *= 0.62137119;
//                    distanceTemp = distanceTemp / 1000.0 * 0.62137119;
//                    averageTemp *= 0.62137119;
//                    speedUnits = "mi/h";
//                    distanceUnits = "mi";
//                } else {
                speedUnits = "km/h";
                if (distanceTemp <= 1000.0) {
                    distanceUnits = "m";
                } else {
                    distanceTemp /= 1000.0;
                    distanceUnits = "km";
                }
//                }

//                SpannableString s = new SpannableString(String.format("%.0f %s", maxSpeedTemp, speedUnits));
//                s.setSpan(new RelativeSizeSpan(0.5f), s.length() - speedUnits.length() - 1, s.length(), 0);
//                maxSpeed.setText(s);

//            SpannableString s = new SpannableString();
//            s.setSpan(new RelativeSizeSpan(0.5f), s.length() - speedUnits.length() - 1, s.length(), 0);
                averageSpeed.setText(String.format("%.0f", averageTemp) + " " + speedUnits);

//            s = new SpannableString(String.format("%.3f %s", distanceTemp, distanceUnits));
//            s.setSpan(new RelativeSizeSpan(0.5f), s.length() - distanceUnits.length() - 1, s.length(), 0);
                distance.setText(String.format("%.3f", distanceTemp) + " " + distanceUnits);
            }

            @Override
            public void firstLocationFound(double lat, double long0) {
                Toast.makeText(getActivity(), "Got loc :"+lat+" "+long0, Toast.LENGTH_SHORT).show();
                ///Helloo
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeSpeedRelatedStuffs();
    }

    private void resumeSpeedRelatedStuffs() {
//        firstfix = true;
//        if (!data.isRunning()) {
//            Gson gson = new Gson();
//            String json = sharedPreferences.getString("data", "");
//            data = gson.fromJson(json, Data.class);
//        }
        if (data == null) {
            data = new Data(onGpsServiceUpdate);
        } else {
            data.setOnGpsServiceUpdate(onGpsServiceUpdate);
        }

//        if (mLocationManager.getAllProviders().indexOf(LocationManager.GPS_PROVIDER) >= 0) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    Activity#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for Activity#requestPermissions for more details.
//                    return;
//                }
//            }
//            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
//        } else {
//            Log.w("MainActivity", "No GPS location provider found. GPS data display will not be available.");
//        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);

        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            showGpsDisabledDialog();
        }
        mLocationManager.addGpsStatusListener(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        pauseSpeedRelatedStuffs();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().stopService(new Intent(getActivity(), GpsServices.class));
    }

    private void pauseSpeedRelatedStuffs() {
        mLocationManager.removeUpdates(this);
        mLocationManager.removeGpsStatusListener(this);
    }

    @Override
    public void showSelectOriginMessage() {
        displayError("Please select an initial location");
    }

    @Override
    public void showSelectDestinationMessage() {
        displayError("Please select a final location");
    }

    @Override
    public void sendResults(NiboSelectedOriginDestination selectedOriginDestination) {
        Intent intent = new Intent();
        intent.putExtra(NiboConstants.SELECTED_ORIGIN_DESTINATION_RESULT, selectedOriginDestination);
        getActivity().setResult(RESULT_OK, intent);
        getActivity().finish();
    }

    @NonNull
    private View.OnTouchListener getClearListener(final EditText editText) {
        return new View.OnTouchListener() {
            final int DRAWABLE_LEFT = 0;
            final int DRAWABLE_TOP = 1;
            final int DRAWABLE_RIGHT = 2;
            final int DRAWABLE_BOTTOM = 3;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int leftEdgeOfRightDrawable = editText.getRight()
                            - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    // when EditBox has padding, adjust leftEdge like
                    // leftEdgeOfRightDrawable -= getResources().getDimension(R.dimen.edittext_padding_left_right);
                    if (event.getRawX() >= leftEdgeOfRightDrawable) {
                        // clicked on clear icon
                        editText.setText("");
                        editText.clearFocus();
                        return true;
                    }
                }
                return false;
            }
        };
    }

    @Override
    public void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        mDestinationEditText.setFocusableInTouchMode(true);
        mOriginEditText.setFocusableInTouchMode(true);
        mProgressBar.setVisibility(View.GONE);
    }

    private void removeMargins() {
        mContentCardView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    TransitionManager.beginDelayedTransition(mContentCardView);
                }
                setMargins(mContentCardView, 0, 0, 0, 0);
            }
        }, 200);
    }

    private void addMargins() {
        mContentCardView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    TransitionManager.beginDelayedTransition(mContentCardView);
                }
                setMargins(mContentCardView, dpToPx(16), 0, dpToPx(16), 0);
            }
        }, 200);
    }


    private void initListeners() {
        mOriginEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mRoundedIndicatorOrigin.setChecked(true);
                mRoundedIndicatorDestination.setChecked(false);
                mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT);
            } else {
                mRoundedIndicatorOrigin.setChecked(false);
            }
        });

        mDestinationEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mRoundedIndicatorDestination.setChecked(true);
                mRoundedIndicatorOrigin.setChecked(false);
                mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT);
            } else {
                mRoundedIndicatorDestination.setChecked(false);
            }
        });


        Observable<String> originObservable = getObservableForEditext(mOriginEditText);
        Observable<String> destinationObservable = getObservableForEditext(mDestinationEditText);

        originObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull String s) throws Exception {
                        if (mBehavior.getState() == BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED) {
                            mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT);
                        }
                        mDestinationEditText.setFocusableInTouchMode(false);
                        findResults(s);
                    }
                });

        destinationObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull String s) throws Exception {
                        if (mBehavior.getState() == BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED) {
                            mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT);
                        }
                        mOriginEditText.setFocusableInTouchMode(false);
                        findResults(s);

                    }
                });

        mSuggestionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mRoundedIndicatorOrigin.isChecked()) {
                    presenter.setOriginData(mSearchSuggestions.get(position));
                    presenter.getPlaceDetailsByID(mSearchSuggestions.get(position));
                } else if (mRoundedIndicatorDestination.isChecked()) {
                    presenter.setDestinationData(mSearchSuggestions.get(position));
                    presenter.getPlaceDetailsByID(mSearchSuggestions.get(position));
                }
            }
        });
    }

    @Override
    public void setOriginAddress(String shortTitle) {
        mOriginEditText.setText(shortTitle);
    }


    @Override
    public void setDestinationAddress(String shortTitle) {
        mDestinationEditText.setText(shortTitle);
    }

    @Override
    public void injectDependencies() {
        super.injectDependencies();

        presenter = injection.getOriginDestinationPickerPresenter();
    }

    protected void toggleVisibility(View... views) {
        for (View view : views) {
            boolean isVisible = view.getVisibility() == View.VISIBLE;
            view.setVisibility(isVisible ? View.INVISIBLE : View.VISIBLE);
        }
    }

    void toggleViews() {
        toggleVisibility(mSuggestionsLL);
        toggleVisibility(mTimeDistanceLL);
    }

    void addOriginMarker(LatLng latLng) {
        if (mMap != null) {
            if (mOriginMapMarker != null) {
                mOriginMapMarker.remove();
                mOriginMapMarker = null;
            }
            CameraPosition cameraPosition =
                    new CameraPosition.Builder().target(latLng)
                            .zoom(getDefaultZoom())
                            .build();

            hasWiderZoom = false;
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            if (mOriginMarkerPinIconRes != DEFAULT_MARKER_ICON_RES) {
                mOriginMapMarker = addMarker(latLng, mOriginMarkerPinIconRes);
            } else {
                mOriginMapMarker = addMarker(latLng);
            }


            mMap.setOnMarkerDragListener(this);

            showBothMarkersAndGetDirections();
        }
    }


    void addDestinationMarker(LatLng latLng) {
        if (mMap != null) {
            if (mDestinationMarker != null) {
                mDestinationMarker.remove();
                mDestinationMarker = null;
            }
            hasWiderZoom = false;
            if (mDestinationMarkerPinIconRes != DEFAULT_MARKER_ICON_RES) {
                mDestinationMarker = addMarker(latLng, mDestinationMarkerPinIconRes);
            } else {
                mDestinationMarker = addMarker(latLng);
            }

            showBothMarkersAndGetDirections();
        }
    }


    private void showBothMarkersAndGetDirections() {
        if (mOriginMapMarker != null && mDestinationMarker != null) {
            int width = getResources().getDisplayMetrics().widthPixels;
            int padding = (int) (width * 0.10); //
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(getLatLngBoundsForMarkers(), padding);
            mMap.moveCamera(cu);
            mMap.animateCamera(cu);

            sendRequest(mOriginMapMarker.getPosition(), mDestinationMarker.getPosition());

        } else {

        }
    }


    LatLngBounds getLatLngBoundsForMarkers() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(mOriginMapMarker.getPosition());
        builder.include(mDestinationMarker.getPosition());
        return builder.build();
    }

    @Override
    public void closeSuggestions() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideKeyboard();
                mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                mCoordinatorlayout.requestLayout();
            }
        });
    }

    private void findResults(String s) {
        presenter.getSuggestions(s);
    }


    @Override
    public void setSuggestions(List<NiboSearchSuggestionItem> niboSearchSuggestionItems) {
        mSearchSuggestions.clear();
        mSearchSuggestions.addAll(niboSearchSuggestionItems);
        mSearchItemAdapter.notifyDataSetChanged();
    }

    private Observable<String> getObservableForEditext(EditText editText) {
        return RxTextView.textChanges(editText).filter(new Predicate<CharSequence>() {
            @Override
            public boolean test(@NonNull CharSequence charSequence) throws Exception {
                return charSequence.length() > 3;
            }
        }).debounce(300, TimeUnit.MILLISECONDS).map(new Function<CharSequence, String>() {
            @Override
            public String apply(@NonNull CharSequence charSequence) throws Exception {
                return charSequence.toString();
            }
        });
    }

    @Override
    public void clearPreviousDirections() {
        Log.d(TAG, "STARTED");

        if (mPrimaryPolyLine != null) {
            mPrimaryPolyLine.remove();
        }

        if (mSecondaryPolyLine != null) {
            mSecondaryPolyLine.remove();
        }

        this.mListLatLng.clear();
    }

    @Override
    public void resetDirectionDetailViews() {
        mOriginToDestinationTv.setText("");
        mTimeTaken.setText("");
    }

    @Override
    public void setUpTimeAndDistanceText(String time, String distance) {
        mTimeTaken.setText(String.format(getString(R.string.time_distance), time, distance));
    }

    @Override
    public void setUpOriginDestinationText(String originAddress, String destinationAddress) {
        mOriginToDestinationTv.setText(String.format(getString(R.string.origin_to_dest_text), originAddress, destinationAddress));
    }


    private void drawPolyline(final List<Route> routes) {

        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = new PolylineOptions();

        mCoordinatorlayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mOriginEditText.clearFocus();
                mDestinationEditText.clearFocus();
                mCoordinatorlayout.requestLayout();
            }
        }, 1000);

        for (int i = 0; i < routes.size(); i++) {
            this.mListLatLng.addAll(routes.get(i).points);
        }

        lineOptions.width(10);
        if (mPrimaryPolyLineColor == 0) {
            lineOptions.color(Color.BLACK);
        } else {
            lineOptions.color(ContextCompat.getColor(getContext(), mPrimaryPolyLineColor));
        }
        lineOptions.startCap(new SquareCap());
        lineOptions.endCap(new SquareCap());
        lineOptions.jointType(ROUND);
        mPrimaryPolyLine = mMap.addPolyline(lineOptions);

        PolylineOptions greyOptions = new PolylineOptions();
        greyOptions.width(10);
        if (mSecondaryPolyLineColor == 0) {
            greyOptions.color(Color.GRAY);
        } else {
            lineOptions.color(ContextCompat.getColor(getContext(), mSecondaryPolyLineColor));
        }
        greyOptions.startCap(new SquareCap());
        greyOptions.endCap(new SquareCap());
        greyOptions.jointType(ROUND);
        mSecondaryPolyLine = mMap.addPolyline(greyOptions);

        animatePolyLine();
    }

    private void animatePolyLine() {

        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {

                List<LatLng> latLngList = mPrimaryPolyLine.getPoints();
                int initialPointSize = latLngList.size();
                int animatedValue = (int) animator.getAnimatedValue();
                int newPoints = (animatedValue * mListLatLng.size()) / 100;

                if (initialPointSize < newPoints) {
                    latLngList.addAll(mListLatLng.subList(initialPointSize, newPoints));
                    mPrimaryPolyLine.setPoints(latLngList);
                }


            }
        });

        animator.addListener(polyLineAnimationListener);
        animator.start();

    }

    Animator.AnimatorListener polyLineAnimationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {

            List<LatLng> primaryLatLng = mPrimaryPolyLine.getPoints();
            List<LatLng> secondaryLatLng = mSecondaryPolyLine.getPoints();

            secondaryLatLng.clear();
            secondaryLatLng.addAll(primaryLatLng);
            primaryLatLng.clear();

            mPrimaryPolyLine.setPoints(primaryLatLng);
            mSecondaryPolyLine.setPoints(secondaryLatLng);

            mPrimaryPolyLine.setZIndex(2);

            animator.start();
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {


        }
    };


    @Override
    public void showErrorFindingRouteMessage() {
        displayError("Error finding routes, please try another set.");
    }

    @Override
    public void showErrorFindingSuggestionsError() {
        showSnackbar(R.string.error_finding_suggestions, R.string.retry_title, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.retryGetSuggestions();
            }
        });
    }

    @Override
    public void setPlaceDataOrigin(Place place, NiboSearchSuggestionItem searchSuggestionItem) {
        addOriginMarker(place.getLatLng());
    }

    @Override
    public void setPlaceDataDestination(Place place, NiboSearchSuggestionItem searchSuggestionItem) {
        addDestinationMarker(place.getLatLng());
    }

    @Override
    public void displayGetPlaceDetailsError() {

    }

    @Override
    public boolean isOriginIndicatorViewChecked() {
        return mRoundedIndicatorOrigin.isChecked();
    }

    @Override
    public boolean isDestinationIndicatorViewChecked() {
        return mRoundedIndicatorDestination.isChecked();
    }

    @Override
    public void initMapWithRoute(List<Route> routes) {
        Log.d(TAG, "DONE");
        if (!routes.isEmpty()) {
            drawPolyline(routes);
        }
        if (mBehavior.getState() == (BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED) && mTimeDistanceLL.getVisibility() == View.INVISIBLE) {
            toggleViews();
        }
    }

    @Override
    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                int satsInView = 0;
                int satsUsed = 0;
                Iterable<GpsSatellite> sats = gpsStatus.getSatellites();
                for (GpsSatellite sat : sats) {
                    satsInView++;
                    if (sat.usedInFix()) {
                        satsUsed++;
                    }
                }
//                satellite.setText(String.valueOf(satsUsed) + "/" + String.valueOf(satsInView));
                if (satsUsed == 0) {
//                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
                    data.setRunning(false);
//                    status.setText("");
                    getActivity().stopService(new Intent(getActivity(), GpsServices.class));
//                    fab.setVisibility(View.INVISIBLE);
//                    refresh.setVisibility(View.INVISIBLE);
//                    accuracy.setText("");
//                    status.setText(getResources().getString(R.string.waiting_for_fix));
//                    firstfix = true;
                }
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                    showGpsDisabledDialog();
                }
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.hasAccuracy()) {
            double acc = location.getAccuracy();
            String units;
//            if (sharedPreferences.getBoolean("miles_per_hour", false)) {
//                units = "ft";
//                acc *= 3.28084;
//            } else {
            units = "m";
//            }
            SpannableString s = new SpannableString(String.format("%.0f %s", acc, units));
            s.setSpan(new RelativeSizeSpan(0.75f), s.length() - units.length() - 1, s.length(), 0);
//            accuracy.setText(s);

//            if (firstfix) {
////                status.setText("");
////                fab.setVisibility(View.VISIBLE);
////                if (!data.isRunning() && !TextUtils.isEmpty(maxSpeed.getText())) {
////                    refresh.setVisibility(View.VISIBLE);
////                }
//                firstfix = false;
//            }
        } else {
//            firstfix = true;
        }

        if (location.hasSpeed()) {
//            progressBarCircularIndeterminate.setVisibility(View.GONE);
            double speed = location.getSpeed() * 3.6;
            String units;
//            if (sharedPreferences.getBoolean("miles_per_hour", false)) { // Convert to MPH
//                speed *= 0.62137119;
//                units = "mi/h";
//            } else {
            units = "km/h";
//            }
            SpannableString s = new SpannableString(String.format(Locale.ENGLISH, "%.0f %s", speed, units));
            s.setSpan(new RelativeSizeSpan(0.25f), s.length() - units.length() - 1, s.length(), 0);
            currentSpeed.setText(s);
        }
    }

    public Data getData() {
        return data;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
