package io.github.froodyapp.activity;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.froodyapp.R;
import io.github.froodyapp.ui.BaseFragment;
import io.github.froodyapp.util.Helpers;
import io.github.froodyapp.util.HelpersA;
import io.github.gsantner.opoc.util.SimpleMarkdownParser;

/**
 * Activity for information about the app
 */
public class AboutFragment extends BaseFragment {
    //########################
    //## Static
    //########################
    public static final String FRAGMENT_TAG = "AboutFragment";

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    //####################
    //##  Ui Binding
    //####################
    @BindView(R.id.about__activity__text_app_version)
    TextView textAppVersion;

    @BindView(R.id.about__activity__text_team)
    TextView textTeam;

    @BindView(R.id.about__activity__text_contributors)
    TextView textContributors;

    @BindView(R.id.about__activity__text_license)
    TextView textLicense;

    @BindViews({R.id.about__activity__sponsor_001, R.id.about__activity__sponsor_002})
    ImageView[] imageSponsors;


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
        textTeam.setMovementMethod(LinkMovementMethod.getInstance());
        textLicense.setMovementMethod(LinkMovementMethod.getInstance());
        textContributors.setMovementMethod(LinkMovementMethod.getInstance());

        Helpers helpers = Helpers.get();
        helpers.setHtmlToTextView(textTeam,
                Helpers.get().loadMarkdownForTextViewFromRaw(R.raw.maintainers, "")
        );

        helpers.setHtmlToTextView(textContributors,
                Helpers.get().loadMarkdownForTextViewFromRaw(R.raw.contributors, "")
        );

        // License text MUST be shown
        try {
            helpers.setHtmlToTextView(textLicense,
                    SimpleMarkdownParser.get().parse(getString(R.string.copyright_license_text_official).replace("\n", "  \n"),
                            "", SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW).getHtml()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }


        // App version
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            textAppVersion.setText(getString(R.string.app_version_v, info.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        TypedArray sponsorDrawables = context.getResources().obtainTypedArray(R.array.sponsor_image);
        final String[] sponsorLinks = getActivity().getResources().getStringArray(R.array.sponsor_link);
        for (int i = 0; i < imageSponsors.length; i++) {
            final int i_f = i;
            int resId = sponsorDrawables.getResourceId(i, R.drawable.divider_h16dp);
            imageSponsors[i].setImageResource(resId);
            imageSponsors[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Helpers.get().openWebpageInExternalBrowser(sponsorLinks[i_f]);
                }
            });
        }
        sponsorDrawables.recycle();
    }

    @OnClick(R.id.about__activity__text_app_version)
    public void onVersionClicked(View v) {
        Helpers.get().openWebpageInExternalBrowser(getString(R.string.app_www_source));
    }

    @OnClick({R.id.about__activity__text_app_version, R.id.about__activity__button_third_party_licenses, R.id.about__activity__button_app_license})
    public void onButtonClicked(View v) {
        Context context = v.getContext();
        switch (v.getId()) {
            case R.id.about__activity__text_app_version: {
                HelpersA.get(getActivity()).openWebpageInExternalBrowser(getString(R.string.app_www_source));
                break;
            }
            case R.id.about__activity__button_app_license: {
                HelpersA.get(getActivity()).showDialogWithHtmlTextView(R.string.license, Helpers.get().readTextfileFromRawRes(R.raw.license, "", ""), false, null);
                break;
            }
            case R.id.about__activity__button_third_party_licenses: {
                try {
                    HelpersA.get(getActivity()).showDialogWithHtmlTextView(R.string.license, new SimpleMarkdownParser().parse(
                            getResources().openRawResource(R.raw.licenses_3rd_party),
                            "", SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW).getHtml()
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
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
        activity.selectTab(2, true);
    }

    @Override
    public boolean onBackPressed() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        return true;
    }
}
