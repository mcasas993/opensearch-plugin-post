/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.my.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.client.node.NodeClient;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.core.xcontent.XContentParser;
import org.opensearch.rest.BaseRestHandler;
import org.opensearch.rest.BytesRestResponse;
import org.opensearch.rest.RestRequest;
import static org.opensearch.core.xcontent.XContentParserUtils.ensureExpectedToken;

import org.opensearch.threadpool.ThreadPool;


import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.opensearch.rest.RestRequest.Method.POST;

public class RestPostTestAction extends BaseRestHandler {
    public static final String POST_TEST_ACTION_REQUEST_DETAILS = "post_test_action_request_details";
    private static final Logger logger = LogManager.getLogger(RestPostTestAction.class);
    private final TestIndex testIndex;
    private final ThreadPool threadPool;
    /**
     * Default constructor
     *
     * @param testIndex persistence layer
     * @param threadPool
     */
    public RestPostTestAction(TestIndex testIndex, ThreadPool threadPool) {
        this.testIndex = testIndex;
        this.threadPool = threadPool;
    }
    public String getName() {
        return POST_TEST_ACTION_REQUEST_DETAILS;
    }
    @Override
    public List<Route> routes() {
        return Collections.singletonList(
                new Route(
                        POST,
                        String.format(
                                Locale.ROOT,
                                "%s",
                                TestPlugin.TEST_PLUGIN_BASE_URI
                        )
                )
        );
    }

    @Override
    protected RestChannelConsumer prepareRequest(
            final RestRequest restRequest,
            final NodeClient client
    ) throws IOException {
        // Get request details
        XContentParser parser = restRequest.contentParser();
        ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.nextToken(), parser);
        TestModel testModel = TestModel.parse(parser);
        // Send response
        return channel -> {
            testIndex.asyncCreate(testModel, this.threadPool)
                    .thenAccept(restStatus -> {
                        try (XContentBuilder builder = channel.newBuilder()) {
                            builder.startObject();
                            builder.field("_index", TestPlugin.TEST_PLUGIN_INDEX_NAME);
                            builder.field("_id", testModel.getId());
                            builder.field("result", restStatus.name());
                            builder.endObject();
                            channel.sendResponse(new BytesRestResponse(restStatus, builder));
                        } catch (Exception e) {
                            logger.error("Error indexing command: ",e);
                        }
                    }).exceptionally(e -> {
                        channel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
                        return null;
                    });
        };
    }
}
