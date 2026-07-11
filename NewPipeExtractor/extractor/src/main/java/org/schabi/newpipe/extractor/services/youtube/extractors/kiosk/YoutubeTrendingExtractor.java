/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * YoutubeTrendingExtractor.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor. If not, see <https://www.gnu.org/licenses/>.
 */

package org.schabi.newpipe.extractor.services.youtube.extractors.kiosk;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextAtKey;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.VIDEOS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.getSearchParameter;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamInfoItemExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamInfoItemLockupExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class YoutubeTrendingExtractor extends KioskExtractor<StreamInfoItem> {

    public static final String KIOSK_ID = "Trending";
    private static final String HOME_QUERY = "popular videos";

    private JsonObject initialData;

    public YoutubeTrendingExtractor(final StreamingService service,
                                    final ListLinkHandler linkHandler,
                                    final String kioskId) {
        super(service, linkHandler, kioskId);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final Localization localization = getExtractorLocalization();

        // @formatter:off
        final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(localization,
                getExtractorContentCountry())
                .value("query", HOME_QUERY)
                .value("params", getSearchParameter(VIDEOS))
                .done())
                .getBytes(StandardCharsets.UTF_8);
        // @formatter:on

        initialData = getJsonPostResponse("search", body, localization);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final Localization localization = getExtractorLocalization();
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        // @formatter:off
        final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(localization,
                getExtractorContentCountry())
                .value("continuation", page.getId())
                .done())
                .getBytes(StandardCharsets.UTF_8);
        // @formatter:on

        final JsonObject ajaxJson = getJsonPostResponse("search", body, localization);
        final JsonArray continuationItems = ajaxJson.getArray("onResponseReceivedCommands")
                .getObject(0)
                .getObject("appendContinuationItemsAction")
                .getArray("continuationItems");

        for (final Object item : continuationItems) {
            final JsonObject itemJsonObject = (JsonObject) item;
            if (itemJsonObject.has("itemSectionRenderer")) {
                collectVideosFrom(collector, itemJsonObject
                        .getObject("itemSectionRenderer")
                        .getArray("contents"));
            }
        }

        return new InfoItemsPage<>(collector, getNextPageFrom(continuationItems));
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        final JsonObject header = initialData.getObject("header");
        String name = null;
        if (header.has("feedTabbedHeaderRenderer")) {
            name = getTextAtKey(header.getObject("feedTabbedHeaderRenderer"), "title");
        } else if (header.has("c4TabbedHeaderRenderer")) {
            name = getTextAtKey(header.getObject("c4TabbedHeaderRenderer"), "title");
        } else if (header.has("pageHeaderRenderer")) {
            name = getTextAtKey(header.getObject("pageHeaderRenderer"), "pageTitle");
        }

        if (isNullOrEmpty(name)) {
            return KIOSK_ID;
        }
        return name;
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() {
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        final JsonArray sections = initialData.getObject("contents")
                .getObject("twoColumnSearchResultsRenderer")
                .getObject("primaryContents")
                .getObject("sectionListRenderer")
                .getArray("contents");

        for (final Object section : sections) {
            final JsonObject sectionJsonObject = (JsonObject) section;
            if (sectionJsonObject.has("itemSectionRenderer")) {
                collectVideosFrom(collector, sectionJsonObject
                        .getObject("itemSectionRenderer")
                        .getArray("contents"));
            }
        }

        return new InfoItemsPage<>(collector, getNextPageFrom(sections));
    }

    private void collectVideosFrom(final StreamInfoItemsCollector collector,
                                   @Nonnull final JsonArray contents) {
        final TimeAgoParser timeAgoParser = getTimeAgoParser();

        for (final Object content : contents) {
            final JsonObject item = (JsonObject) content;
            if (item.has("videoRenderer")) {
                collector.commit(new YoutubeStreamInfoItemExtractor(
                        item.getObject("videoRenderer"), timeAgoParser));
            } else if (item.has("lockupViewModel")
                    && "LOCKUP_CONTENT_TYPE_VIDEO".equals(item
                    .getObject("lockupViewModel")
                    .getString("contentType"))) {
                collector.commit(new YoutubeStreamInfoItemLockupExtractor(
                        item.getObject("lockupViewModel"), timeAgoParser));
            }
        }
    }

    @Nullable
    private Page getNextPageFrom(final JsonArray items) {
        for (final Object item : items) {
            final JsonObject itemJsonObject = (JsonObject) item;
            if (itemJsonObject.has("continuationItemRenderer")) {
                final String token = itemJsonObject.getObject("continuationItemRenderer")
                        .getObject("continuationEndpoint")
                        .getObject("continuationCommand")
                        .getString("token");
                return isNullOrEmpty(token)
                        ? null
                        : new Page(YOUTUBEI_V1_URL + "search?"
                        + DISABLE_PRETTY_PRINT_PARAMETER, token);
            }
        }

        return null;
    }
}
