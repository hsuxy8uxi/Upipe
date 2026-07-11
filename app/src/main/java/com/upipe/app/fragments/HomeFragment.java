package com.upipe.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.material.tabs.TabLayout;

import com.upipe.app.BaseFragment;
import com.upipe.app.R;
import com.upipe.app.fragments.list.kiosk.FilteredKioskFragment;

public class HomeFragment extends BaseFragment {
    private TabLayout tabLayout;
    private androidx.viewpager.widget.ViewPager viewPager;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    protected void initViews(final View rootView, final Bundle savedInstanceState) {
        super.initViews(rootView, savedInstanceState);

        tabLayout = rootView.findViewById(R.id.home_tab_layout);
        viewPager = rootView.findViewById(R.id.home_view_pager);
        viewPager.setAdapter(new HomePagerAdapter(getChildFragmentManager()));
        viewPager.setOffscreenPageLimit(1);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viewPager != null) {
            viewPager.setAdapter(null);
        }
        viewPager = null;
        tabLayout = null;
    }

    private final class HomePagerAdapter extends FragmentPagerAdapter {
        HomePagerAdapter(@NonNull final FragmentManager fragmentManager) {
            super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(final int position) {
            final FilteredKioskFragment fragment = position == 1
                    ? FilteredKioskFragment.newLiveFeed()
                    : FilteredKioskFragment.newVideoFeed();
            fragment.useAsFrontPage(true);
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(final int position) {
            return position == 1
                    ? getString(R.string.home_live_tab)
                    : getString(R.string.tab_home);
        }
    }
}
