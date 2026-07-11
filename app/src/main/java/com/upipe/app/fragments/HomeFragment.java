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
    private final TabLayout.OnTabSelectedListener tabSelectedListener =
            new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(final TabLayout.Tab tab) {
                    updateTitleForPage(tab.getPosition());
                }

                @Override
                public void onTabUnselected(final TabLayout.Tab tab) {
                    // No-op
                }

                @Override
                public void onTabReselected(final TabLayout.Tab tab) {
                    updateTitleForPage(tab.getPosition());
                }
            };

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
        setupIconTabs();
        tabLayout.addOnTabSelectedListener(tabSelectedListener);
        updateTitleForPage(viewPager.getCurrentItem());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewPager != null) {
            updateTitleForPage(viewPager.getCurrentItem());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tabLayout != null) {
            tabLayout.removeOnTabSelectedListener(tabSelectedListener);
        }
        if (viewPager != null) {
            viewPager.setAdapter(null);
        }
        viewPager = null;
        tabLayout = null;
    }

    private void setupIconTabs() {
        setupIconTab(0, R.drawable.ic_home, R.string.tab_home);
        setupIconTab(1, R.drawable.ic_live_tv, R.string.home_live_tab);
    }

    private void setupIconTab(final int position, final int iconRes, final int descriptionRes) {
        final TabLayout.Tab tab = tabLayout.getTabAt(position);
        if (tab == null) {
            return;
        }

        tab.setText(null);
        tab.setIcon(iconRes);
        tab.setContentDescription(getString(descriptionRes));
    }

    private void updateTitleForPage(final int position) {
        setTitle(getString(position == 1 ? R.string.home_live_tab : R.string.tab_home));
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
            return null;
        }
    }
}
