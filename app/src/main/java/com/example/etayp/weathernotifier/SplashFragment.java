package com.example.etayp.weathernotifier;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

/**
 * A simple {@link Fragment} subclass.
 */
public class SplashFragment extends Fragment {


    public SplashFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);

        View loadingIcon = view.findViewById(R.id.loading_icon);
        Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.rotate_around_center_point);
        animation.setInterpolator(new LinearInterpolator());
        loadingIcon.startAnimation(animation);

        return view;
    }

}
