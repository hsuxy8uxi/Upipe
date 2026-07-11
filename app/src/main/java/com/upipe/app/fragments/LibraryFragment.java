package com.upipe.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.upipe.app.BaseFragment;
import com.upipe.app.R;
import com.upipe.app.util.NavigationHelper;

public final class LibraryFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        setTitle(getString(R.string.tab_library));
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    protected void initListeners() {
        final View rootView = requireView();

        rootView.findViewById(R.id.library_subscriptions)
                .setOnClickListener(v -> NavigationHelper.openSubscriptionFragment(getFM()));
        rootView.findViewById(R.id.library_playlists)
                .setOnClickListener(v -> NavigationHelper.openBookmarksFragment(getFM()));
        rootView.findViewById(R.id.library_history)
                .setOnClickListener(v -> NavigationHelper.openStatisticFragment(getFM()));
        rootView.findViewById(R.id.library_about)
                .setOnClickListener(v -> NavigationHelper.openAbout(requireContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(getString(R.string.tab_library));
    }
}
