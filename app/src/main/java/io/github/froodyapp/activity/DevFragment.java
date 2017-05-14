package io.github.froodyapp.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Random;

import io.github.froodyapp.R;
import io.github.froodyapp.ui.BaseFragment;
import io.github.froodyapp.util.AppSettings;

/**
 * Activity for information about the app
 */
public class DevFragment extends BaseFragment implements View.OnClickListener {
    //########################
    //## Static
    //########################
    public static final String FRAGMENT_TAG = "DevFragment";

    public static DevFragment newInstance() {
        return new DevFragment();
    }

    //####################
    //##  Stuff
    //####################
    Button button;
    AppSettings appSettings;
    Random random;


    //####################
    //##  Methods
    //####################
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        button = new Button(inflater.getContext());
        button.setText("Do something");
        return button;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        button.setOnClickListener(this);
        Context context = getContext();
        appSettings = new AppSettings(context);
        random = new Random();

        // Not on default server ;)
        if (appSettings.getFroodyServer().equals(getString(R.string.server_default))) {
            getFragmentManager().popBackStack();
            return;
        }

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public String getFragmentTag() {
        return FRAGMENT_TAG;
    }

    @Override
    public boolean onBackPressed() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        return true;
    }
}
