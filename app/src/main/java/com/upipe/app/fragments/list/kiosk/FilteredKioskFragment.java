package com.upipe.app.fragments.list.kiosk;

import androidx.annotation.NonNull;

import com.evernote.android.state.State;

import com.upipe.app.R;
import com.upipe.app.error.ErrorInfo;
import com.upipe.app.error.UserAction;
import com.upipe.app.util.StreamTypeUtil;

import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskInfo;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.List;
import java.util.stream.Collectors;

public class FilteredKioskFragment extends KioskFragment {
    private static final String VIDEO_KIOSK_ID = "Trending";
    private static final String LIVE_KIOSK_ID = "live";

    private enum FilterMode {
        VIDEOS,
        LIVE
    }

    @State
    FilterMode filterMode = FilterMode.VIDEOS;

    public static FilteredKioskFragment newVideoFeed() {
        return newInstance(FilterMode.VIDEOS);
    }

    public static FilteredKioskFragment newLiveFeed() {
        return newInstance(FilterMode.LIVE);
    }

    private static FilteredKioskFragment newInstance(final FilterMode mode) {
        final FilteredKioskFragment instance = new FilteredKioskFragment();
        instance.filterMode = mode;
        try {
            instance.setUpYoutubeKiosk(mode);
        } catch (final ExtractionException ignored) {
            // The fragment will show the extractor error during the normal loading flow.
        }
        return instance;
    }

    private void setUpYoutubeKiosk(final FilterMode mode) throws ExtractionException {
        final StreamingService service = ServiceList.YouTube;
        final KioskList kioskList = NewPipe.getService(service.getServiceId()).getKioskList();
        final String preferredKioskId = mode == FilterMode.LIVE ? LIVE_KIOSK_ID : VIDEO_KIOSK_ID;
        final String selectedKioskId = kioskList.getAvailableKiosks().contains(preferredKioskId)
                ? preferredKioskId
                : kioskList.getDefaultKioskId();

        final ListLinkHandlerFactory factory =
                kioskList.getListLinkHandlerFactoryByType(selectedKioskId);
        setInitialData(service.getServiceId(), factory.fromId(selectedKioskId).getUrl(),
                getDisplayTitle());
        kioskId = selectedKioskId;
        kioskTranslatedName = getDisplayTitle();
    }

    @Override
    public void handleResult(@NonNull final KioskInfo result) {
        hideLoading();
        name = getDisplayTitle();
        setTitle(name);

        addFilteredItems(result.getRelatedItems());
        showListFooter(hasMoreItems());

        if (infoListAdapter.getItemsList().isEmpty()) {
            if (hasMoreItems()) {
                loadMoreItems();
            } else {
                infoListAdapter.clearStreamItemList();
                showEmptyState();
            }
        }

        showErrorsIfNeeded(result.getErrors(), "Start loading: " + url);
    }

    @Override
    public void handleNextItems(final ListExtractor.InfoItemsPage<StreamInfoItem> result) {
        currentNextPage = result.getNextPage();
        addFilteredItems(result.getItems());
        showListFooter(hasMoreItems());

        if (infoListAdapter.getItemsList().isEmpty()) {
            if (hasMoreItems()) {
                loadMoreItems();
            } else {
                infoListAdapter.clearStreamItemList();
                showEmptyState();
            }
        }

        showErrorsIfNeeded(result.getErrors(), "Loading more items: " + url);
    }

    @Override
    public void showEmptyState() {
        if (filterMode == FilterMode.LIVE) {
            setEmptyStateMessage(R.string.no_live_streams);
        } else {
            setEmptyStateMessage(R.string.no_videos);
        }
        super.showEmptyState();
    }

    private void addFilteredItems(final List<StreamInfoItem> items) {
        infoListAdapter.addInfoItemList(items.stream()
                .filter(this::shouldShowItem)
                .collect(Collectors.toList()));
    }

    private boolean shouldShowItem(final StreamInfoItem item) {
        if (filterMode == FilterMode.LIVE) {
            return StreamTypeUtil.isLiveStream(item.getStreamType());
        }

        return item.getStreamType() == StreamType.VIDEO_STREAM
                && !item.isShortFormContent();
    }

    private String getDisplayTitle() {
        if (getContext() == null) {
            return filterMode == FilterMode.LIVE ? "Live" : "Home";
        }
        return getString(filterMode == FilterMode.LIVE ? R.string.home_live_tab : R.string.tab_home);
    }

    private void showErrorsIfNeeded(final List<Throwable> errors, final String request) {
        if (errors.isEmpty()) {
            return;
        }

        final ErrorInfo errorInfo = new ErrorInfo(errors, UserAction.REQUESTED_KIOSK,
                request, serviceId, url);
        if (infoListAdapter.getItemsList().isEmpty()) {
            showError(errorInfo);
        } else {
            showSnackBarError(errorInfo);
        }
    }
}
