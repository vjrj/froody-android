package io.github.froodyapp.activity;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.froodyapp.R;
import io.github.froodyapp.ui.BaseFragment;
import io.github.froodyapp.ui.CustomDialogs;
import io.github.froodyapp.util.Helpers;

/**
 * Activity for information about the app
 */
public class InfoFragment extends BaseFragment {
    //########################
    //## Static
    //########################
    public static final String FRAGMENT_TAG = "InfoFragment";

    public static InfoFragment newInstance() {
        return new InfoFragment();
    }

    //####################
    //##  Ui Binding
    //####################
    @BindView(R.id.info__fragment__text_app_version)
    TextView textAppVersion;

    @BindView(R.id.info__fragment__text_maintainers)
    TextView textMaintainers;

    @BindView(R.id.info__fragment__text_contributors)
    TextView textContributors;


    //####################
    //##  Methods
    //####################
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.info__fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Context context = getContext();
        textMaintainers.setText(new SpannableString(Html.fromHtml(getMaintainersHtml(context))));
        textMaintainers.setMovementMethod(LinkMovementMethod.getInstance());

        textContributors.setText(new SpannableString(Html.fromHtml(
                Helpers.readTextfileFromRawRes(context, R.raw.contributors,
                        "<font color='" + ContextCompat.getColor(context, R.color.accent) + "'><b>*</b></font> ", "<br>")))
        );
        textContributors.setMovementMethod(LinkMovementMethod.getInstance());


        // App version
        try {
            PackageManager manager = getActivity().getPackageManager();
            PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
            textAppVersion.setText(getString(R.string.app_version_v, info.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!isDetached()) {
            inflater.inflate(R.menu.info__fragment_menu, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map: {
                MainActivity mainActivity = (MainActivity) (getActivity());
                mainActivity.showFragment(mainActivity.getFragment(MapOSMFragment.FRAGMENT_TAG));
                getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
            }
        }
        return false;
    }

    @OnClick({R.id.info__fragment__text_app_version, R.id.info__fragment__button_third_party_licenses, R.id.info__fragment__button_gplv3_license})
    public void onButtonClicked(View v) {
        Context context = v.getContext();
        switch (v.getId()) {
            case R.id.info__fragment__text_app_version: {
                Helpers.openWebpageWithExternalBrowser(context, getString(R.string.project_github_page));
                break;
            }
            case R.id.info__fragment__button_gplv3_license: {
                CustomDialogs.showDialogWithRawFileInWebView(context, "license.md", R.string.license);
                break;
            }
            case R.id.info__fragment__button_third_party_licenses: {
                CustomDialogs.showDialogWithRawFileInWebView(context, "licenses.html", R.string.license);
                break;
            }
        }
    }

    public String getMaintainersHtml(Context context) {
        String text = Helpers.readTextfileFromRawRes(context, R.raw.maintainers, "", "<br>");
        text = text.replace("SUBTABBY", "&nbsp;&nbsp;")
                .replace("NEWENTRY", "<font color='" + ContextCompat.getColor(context, R.color.accent) + "'><b>*</b></font> ");
        return text;
    }

    @Override
    public String getFragmentTag() {
        return FRAGMENT_TAG;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        activity.setTitle(R.string.about_);
        activity.navigationView.setCheckedItem(R.id.nav_informations);
    }

    @Override
    public boolean onBackPressed() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        return true;
    }
}
