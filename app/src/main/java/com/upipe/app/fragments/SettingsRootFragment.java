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

public final class SettingsRootFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        setTitle(getString(R.string.settings));
        return inflater.inflate(R.layout.fragment_settings_root, container, false);
    }

    @Override
    protected void initListeners() {
        requireView().findViewById(R.id.open_settings)
                .setOnClickListener(v -> NavigationHelper.openSettings(requireContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(getString(R.string.settings));
    }
}
