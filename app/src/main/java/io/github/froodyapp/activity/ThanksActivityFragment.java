package io.github.froodyapp.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.froodyapp.R;
import io.github.froodyapp.util.ContextUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class ThanksActivityFragment extends Fragment {

    public ThanksActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.thanks__fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ContextUtils cu = ContextUtils.get();
        cu.setHtmlToTextView((view.findViewById(R.id.thanks_md)),
                ContextUtils.get().loadMarkdownForTextViewFromRaw(R.raw.more_projects, "")
        );



        super.onViewCreated(view, savedInstanceState);
    }
}
