package com.upipe.app.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.upipe.app.BaseFragment;
import com.upipe.app.NewPipeDatabase;
import com.upipe.app.R;
import com.upipe.app.database.playlist.PlaylistMetadataEntry;
import com.upipe.app.database.stream.StreamStatisticsEntry;
import com.upipe.app.database.stream.model.StreamEntity;
import com.upipe.app.local.history.HistoryRecordManager;
import com.upipe.app.local.playlist.LocalPlaylistManager;
import com.upipe.app.local.subscription.SubscriptionManager;
import com.upipe.app.util.Localization;
import com.upipe.app.util.NavigationHelper;
import com.upipe.app.util.image.CoilHelper;
import com.upipe.app.views.AnimatedProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public final class LibraryFragment extends BaseFragment {
    private static final int MAX_HISTORY_PREVIEW_ITEMS = 5;
    private static final int MAX_PLAYLIST_PREVIEW_ITEMS = 10;

    private final CompositeDisposable disposables = new CompositeDisposable();

    private LinearLayout historyPreview;
    private LinearLayout playlistsPreview;
    private TextView profileSummary;
    private TextView historySummary;
    private TextView playlistsSummary;
    private TextView subscriptionsSummary;
    private LocalPlaylistManager playlistManager;

    private int subscriptionCount;
    private int playlistCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        setTitle(getString(R.string.tab_library));
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    protected void initViews(final View rootView, final Bundle savedInstanceState) {
        super.initViews(rootView, savedInstanceState);

        historyPreview = rootView.findViewById(R.id.library_history_preview);
        playlistsPreview = rootView.findViewById(R.id.library_playlists_preview);
        profileSummary = rootView.findViewById(R.id.library_profile_summary);
        historySummary = rootView.findViewById(R.id.library_history_summary_text);
        playlistsSummary = rootView.findViewById(R.id.library_playlists_summary_text);
        subscriptionsSummary = rootView.findViewById(R.id.library_subscriptions_summary_text);
        playlistManager = new LocalPlaylistManager(NewPipeDatabase.getInstance(requireContext()));

        keepPagerFromStealingHorizontalScroll(
                rootView.findViewById(R.id.library_history_scroller));
        keepPagerFromStealingHorizontalScroll(
                rootView.findViewById(R.id.library_playlists_scroller));
        keepPagerFromStealingHorizontalScroll(historyPreview);
        keepPagerFromStealingHorizontalScroll(playlistsPreview);

        subscribeToLibraryData();
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
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(getString(R.string.tab_library));
    }

    @Override
    public void onDestroyView() {
        disposables.clear();
        historyPreview = null;
        playlistsPreview = null;
        profileSummary = null;
        historySummary = null;
        playlistsSummary = null;
        subscriptionsSummary = null;
        playlistManager = null;
        super.onDestroyView();
    }

    private void subscribeToLibraryData() {
        final HistoryRecordManager historyRecordManager =
                new HistoryRecordManager(requireContext());
        final SubscriptionManager subscriptionManager = new SubscriptionManager(requireContext());

        disposables.add(historyRecordManager.getStreamStatistics()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::renderHistoryPreview,
                        throwable -> renderHistoryPreview(Collections.emptyList())));

        disposables.add(playlistManager.getPlaylists()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::renderPlaylistsPreview,
                        throwable -> renderPlaylistsPreview(Collections.emptyList())));

        disposables.add(subscriptionManager.subscriptions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriptions -> {
                    subscriptionCount = subscriptions.size();
                    if (subscriptionsSummary != null) {
                        subscriptionsSummary.setText(getResources().getQuantityString(
                                R.plurals.library_subscriptions_count,
                                subscriptionCount,
                                subscriptionCount));
                    }
                    updateProfileSummary();
                }, throwable -> {
                    subscriptionCount = 0;
                    updateProfileSummary();
                }));
    }

    private void renderHistoryPreview(final List<StreamStatisticsEntry> history) {
        if (!isAdded() || historyPreview == null || historySummary == null) {
            return;
        }

        historyPreview.removeAllViews();
        historySummary.setText(R.string.library_history_summary);

        if (history.isEmpty()) {
            historyPreview.addView(createEmptyCard(R.drawable.ic_history,
                    getString(R.string.library_empty_history),
                    getString(R.string.library_empty_history_summary),
                    () -> NavigationHelper.openStatisticFragment(getFM())));
        } else {
            final List<StreamStatisticsEntry> sortedHistory = new ArrayList<>(history);
            sortedHistory.sort(Comparator.comparing(
                    StreamStatisticsEntry::getLatestAccessDate).reversed());

            final int previewCount = Math.min(sortedHistory.size(), MAX_HISTORY_PREVIEW_ITEMS);
            for (int i = 0; i < previewCount; i++) {
                historyPreview.addView(createHistoryCard(sortedHistory.get(i)));
            }

            if (sortedHistory.size() > MAX_HISTORY_PREVIEW_ITEMS) {
                historyPreview.addView(createEmptyCard(R.drawable.ic_history,
                        getString(R.string.library_see_more_history),
                        getString(R.string.library_see_more_history_summary),
                        () -> NavigationHelper.openStatisticFragment(getFM())));
            }
        }

        updateProfileSummary();
    }

    private void renderPlaylistsPreview(final List<PlaylistMetadataEntry> playlists) {
        if (!isAdded() || playlistsPreview == null || playlistsSummary == null) {
            return;
        }

        playlistsPreview.removeAllViews();
        playlistCount = playlists.size();
        playlistsSummary.setText(getResources().getQuantityString(
                R.plurals.library_playlists_count,
                playlistCount,
                playlistCount));

        if (playlists.isEmpty()) {
            playlistsPreview.addView(createEmptyCard(R.drawable.ic_add,
                    getString(R.string.library_empty_playlists),
                    getString(R.string.library_empty_playlists_summary),
                    this::showCreatePlaylistDialog));
        } else {
            final int previewCount = Math.min(playlists.size(), MAX_PLAYLIST_PREVIEW_ITEMS);
            for (int i = 0; i < previewCount; i++) {
                playlistsPreview.addView(createPlaylistCard(playlists.get(i)));
            }
        }

        updateProfileSummary();
    }

    private View createHistoryCard(final StreamStatisticsEntry entry) {
        final View card = LayoutInflater.from(requireContext())
                .inflate(R.layout.list_stream_card_item, historyPreview, false);
        card.setLayoutParams(previewCardLayoutParams());
        keepPagerFromStealingHorizontalScroll(card);

        final StreamEntity stream = entry.getStreamEntity();
        final ImageView thumbnail = card.findViewById(R.id.itemThumbnailView);
        final TextView title = card.findViewById(R.id.itemVideoTitleView);
        final TextView uploader = card.findViewById(R.id.itemUploaderView);
        final TextView duration = card.findViewById(R.id.itemDurationView);
        final TextView additionalDetails = card.findViewById(R.id.itemAdditionalDetails);
        final AnimatedProgressBar progress = card.findViewById(R.id.itemProgressView);

        title.setText(stream.getTitle());
        uploader.setText(stream.getUploader());
        if (additionalDetails != null) {
            additionalDetails.setText(Localization.relativeTime(entry.getLatestAccessDate()));
        }
        if (stream.getDuration() > 0) {
            duration.setText(Localization.getDurationString(stream.getDuration()));
            duration.setVisibility(View.VISIBLE);
        } else {
            duration.setVisibility(View.GONE);
        }
        if (progress != null && stream.getDuration() > 0 && entry.getProgressMillis() > 0) {
            final int durationSeconds = (int) Math.min(Integer.MAX_VALUE, stream.getDuration());
            final int progressSeconds = (int) Math.min(durationSeconds,
                    TimeUnit.MILLISECONDS.toSeconds(entry.getProgressMillis()));
            progress.setMax(durationSeconds);
            progress.setProgress(progressSeconds);
            progress.setVisibility(View.VISIBLE);
        } else if (progress != null) {
            progress.setVisibility(View.GONE);
        }

        CoilHelper.INSTANCE.loadThumbnail(thumbnail, stream.getThumbnailUrl());
        card.setOnClickListener(v -> NavigationHelper.openVideoDetailFragment(requireContext(),
                getFM(), stream.getServiceId(), stream.getUrl(), stream.getTitle(), null, false));
        return card;
    }

    private View createPlaylistCard(final PlaylistMetadataEntry playlist) {
        final View card = LayoutInflater.from(requireContext())
                .inflate(R.layout.list_playlist_card_item, playlistsPreview, false);
        card.setLayoutParams(previewCardLayoutParams());
        keepPagerFromStealingHorizontalScroll(card);

        final ImageView thumbnail = card.findViewById(R.id.itemThumbnailView);
        final TextView title = card.findViewById(R.id.itemTitleView);
        final TextView streamCount = card.findViewById(R.id.itemStreamCountView);
        final TextView uploader = card.findViewById(R.id.itemUploaderView);

        final String playlistName = TextUtils.isEmpty(playlist.getOrderingName())
                ? getString(R.string.unknown_content)
                : playlist.getOrderingName();
        title.setText(playlistName);
        streamCount.setText(Localization.localizeStreamCountMini(requireContext(),
                playlist.getStreamCount()));
        uploader.setText(Localization.localizeStreamCount(requireContext(),
                playlist.getStreamCount()));

        CoilHelper.INSTANCE.loadPlaylistThumbnail(thumbnail, playlist.getThumbnailUrl());
        card.setOnClickListener(v -> NavigationHelper.openLocalPlaylistFragment(getFM(),
                playlist.getUid(), playlistName));
        return card;
    }

    private View createEmptyCard(@DrawableRes final int iconRes,
                                 final String titleText,
                                 final String summaryText,
                                 final Runnable clickAction) {
        final LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(10), dp(10), dp(10), dp(10));
        card.setMinimumHeight(dp(104));
        card.setLayoutParams(previewCardLayoutParams());
        keepPagerFromStealingHorizontalScroll(card);
        card.setClickable(true);
        card.setFocusable(true);
        applySelectableItemBackground(card);
        card.setOnClickListener(v -> clickAction.run());

        final ImageView icon = new ImageView(requireContext());
        icon.setImageResource(iconRes);
        icon.setContentDescription(titleText);
        final LinearLayout.LayoutParams iconParams =
                new LinearLayout.LayoutParams(dp(28), dp(28));
        card.addView(icon, iconParams);

        final TextView title = new TextView(requireContext());
        title.setText(titleText);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setPadding(0, dp(8), 0, 0);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        card.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final TextView summary = new TextView(requireContext());
        summary.setText(summaryText);
        summary.setGravity(Gravity.CENTER);
        summary.setMaxLines(3);
        summary.setEllipsize(TextUtils.TruncateAt.END);
        summary.setPadding(0, dp(3), 0, 0);
        summary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        card.addView(summary, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        return card;
    }

    private LinearLayout.LayoutParams previewCardLayoutParams() {
        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
        final int availableWidth = Math.max(dp(1), screenWidth - dp(56));
        final int width = Math.max(dp(104), Math.min(dp(132), availableWidth / 3));
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                width, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMarginEnd(dp(8));
        return params;
    }

    private void showCreatePlaylistDialog() {
        final Context context = getContext();
        if (context == null || playlistManager == null) {
            return;
        }

        final EditText editText = new EditText(context);
        editText.setHint(R.string.name);
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setPadding(dp(24), dp(8), dp(24), dp(8));

        new AlertDialog.Builder(context)
                .setTitle(R.string.create_playlist)
                .setView(editText)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.create, (dialog, which) -> {
                    final String name = editText.getText().toString().trim();
                    if (TextUtils.isEmpty(name) || playlistManager == null) {
                        return;
                    }

                    disposables.add(playlistManager.createEmptyPlaylist(name)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(ignored -> showToast(R.string.playlist_creation_success),
                                    throwable -> showToast(R.string.general_error)));
                })
                .show();
    }

    private void showToast(final int stringRes) {
        final Context context = getContext();
        if (context != null) {
            Toast.makeText(context, stringRes, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfileSummary() {
        if (profileSummary != null) {
            profileSummary.setText(getString(R.string.library_profile_summary,
                    subscriptionCount, playlistCount));
        }
    }

    private void keepPagerFromStealingHorizontalScroll(final View scrollableView) {
        if (scrollableView == null) {
            return;
        }

        if (scrollableView instanceof HorizontalScrollView) {
            ((HorizontalScrollView) scrollableView).setSmoothScrollingEnabled(true);
        }

        scrollableView.setOnTouchListener((view, event) -> {
            final boolean disallow = event.getActionMasked() != MotionEvent.ACTION_UP
                    && event.getActionMasked() != MotionEvent.ACTION_CANCEL;
            requestDisallowParentIntercept(view, disallow);
            return false;
        });
    }

    private void requestDisallowParentIntercept(final View view, final boolean disallow) {
        ViewParent parent = view.getParent();
        while (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallow);
            parent = parent.getParent();
        }
    }

    private void applySelectableItemBackground(final View view) {
        final TypedValue outValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground, outValue, true);
        if (outValue.resourceId != 0) {
            view.setBackgroundResource(outValue.resourceId);
        }
    }

    private int dp(final int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }
}
