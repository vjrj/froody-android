package io.github.froodyapp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.froodyapp.BuildConfig;
import io.github.froodyapp.R;
import io.github.froodyapp.model.FroodyEntryPlus;
import io.github.froodyapp.ui.BaseFragment;
import io.github.froodyapp.util.AppCast;
import io.github.froodyapp.util.AppSettings;
import io.github.froodyapp.util.Helpers;
import io.github.froodyapp.util.MyEntriesHelper;

public class MoreFragment extends BaseFragment {
    //########################
    //## Static
    //########################
    public static final String FRAGMENT_TAG = "MoreFragment";

    public static MoreFragment newInstance() {
        return new MoreFragment();
    }

    //####################
    //##  Ui Binding
    //####################
    @BindView(R.id.more__fragment__my_entries__count)
    TextView textMyEntriesCount;

    //####################
    //##  Methods
    //####################
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.more__fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    @SuppressWarnings({"deprecation", "ConstantConditions"})
    public void onViewCreated(View root, Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
        Context context = getContext();

        root.findViewById(R.id.more__fragment__button_support__bitcoin_donation)
                .setVisibility(BuildConfig.IS_GPLAY_BUILD ? View.GONE : View.VISIBLE);

    }

    @Override
    public String getFragmentTag() {
        return FRAGMENT_TAG;
    }

    @Override
    public void onResume() {
        super.onResume();
        Context context = getContext();
        if (context != null) {
            LocalBroadcastManager.getInstance(context).registerReceiver(localBroadcastReceiver, AppCast.getLocalBroadcastFilter());
        }
        MainActivity activity = (MainActivity) getActivity();
        activity.setTitle(R.string.app_name);
        activity.selectTab(2);

        textMyEntriesCount.setText(String.valueOf(new MyEntriesHelper(activity).getMyEntriesCount()));
    }

    @Override
    public boolean onBackPressed() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        return true;
    }

    @OnClick({R.id.more__fragment__button_info__app, R.id.more__fragment__button_info__blog, R.id.more__fragment__button_support__bitcoin_donation, R.id.more__fragment__button_support__bug_report, R.id.more__fragment__button_support__spread, R.id.more__fragment__button_support__translate, R.id.more__fragment__my_entries, R.id.more__fragment__settings})
    public void onViewClicked(View view) {
        MainActivity activity = (MainActivity) getActivity();
        Context context = view.getContext();
        switch (view.getId()) {
            case R.id.more__fragment__button_info__app: {
                activity.showFragment(activity.getFragment(InfoFragment.FRAGMENT_TAG));
                break;
            }
            case R.id.more__fragment__button_info__blog: {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.project_github_page)));
                startActivity(intent);
                break;
            }
            case R.id.more__fragment__button_support__bitcoin_donation: {
                Helpers.donateBitcoinRequest(context);
                break;
            }
            case R.id.more__fragment__button_support__bug_report: {
                if (!new AppSettings(context).isDevDebugModeEnabled()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(getString(R.string.project_bugtracker)));
                    startActivity(intent);
                } else {
                    activity.showFragment(activity.getFragment(DevFragment.FRAGMENT_TAG));
                }
                break;
            }
            case R.id.more__fragment__button_support__spread: {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
                i.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.project_github_page));
                context.startActivity(Intent.createChooser(i, context.getString(R.string.share_chooser_share_entry)));
                break;
            }
            case R.id.more__fragment__button_support__translate: {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.project_translation_service)));
                startActivity(intent);
                break;
            }
            case R.id.more__fragment__my_entries: {
                List<FroodyEntryPlus> entries = new MyEntriesHelper(context).getMyEntries();
                if (entries.size() > 0) {
                    BotsheetEntryMulti botsheetFragment = BotsheetEntryMulti.newInstance(entries, R.string.nav__my_entries);
                    botsheetFragment.show(activity.getSupportFragmentManager(), botsheetFragment.getTag());
                } else {
                    Toast.makeText(context, R.string.no_entries_were_published_by_me, Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.more__fragment__settings: {
                startActivity(new Intent(context, SettingActivity.class));
                break;
            }
        }
    }

    @Override
    public void onPause() {
        Context context = getContext();
        if (context != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(localBroadcastReceiver);
        }
        super.onPause();
    }


    @SuppressWarnings("unchecked")
    private final BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AppCast.FROODY_ENTRY_DELETED.ACTION: {
                    textMyEntriesCount.setText(String.valueOf(new MyEntriesHelper(context).getMyEntriesCount()));
                    break;
                }

            }
        }
    };
}
