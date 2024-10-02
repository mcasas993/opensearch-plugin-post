/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.my.plugin;

import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.common.util.concurrent.ThreadContext;
import org.opensearch.common.xcontent.XContentFactory;
import org.opensearch.core.action.ActionListener;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.core.xcontent.ToXContent;
import org.opensearch.index.shard.IndexingOperationListener;
import org.opensearch.threadpool.ThreadPool;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class TestIndex  implements IndexingOperationListener {

    private static final Logger logger = LogManager.getLogger(TestIndex.class);

    private final Client client;
    /**
     * @param client
     */
    public TestIndex(Client client) {
        this.client = client;
    }

    public RestStatus create(TestModel testModel) throws ExecutionException, InterruptedException {
        CompletableFuture<IndexResponse> future = new CompletableFuture<>();
       // ExecutorService executor = threadPool.executor(ThreadPool.Names.WRITE);

        try {
            IndexRequest request = new IndexRequest()
                .index(TestPlugin.TEST_PLUGIN_INDEX_NAME)
                    .source(testModel.toXContent(XContentFactory.jsonBuilder(), ToXContent.EMPTY_PARAMS))
                    .id(testModel.getId())
                    .create(true);

            client.index(
                    request,
                    new ActionListener<>() {
                        @Override
                        public void onResponse(IndexResponse indexResponse) {
                            future.complete(indexResponse);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            logger.info("Could not process testModel: {}", testModel.getId(), e);
                            future.completeExceptionally(e);
                        }
                    }
            );
        } catch (IOException e) {
            logger.error("IOException occurred creating command details", e);
        }
        return future.get().status();
    }

    public CompletableFuture<RestStatus> asyncCreate(TestModel testModel, ThreadPool threadPool)  {
        CompletableFuture<RestStatus> future = new CompletableFuture<>();
        ExecutorService executor = threadPool.executor(ThreadPool.Names.WRITE);
        try {
            IndexRequest request = new IndexRequest()
                    .index(TestPlugin.TEST_PLUGIN_INDEX_NAME)
                    .source(testModel.toXContent(XContentFactory.jsonBuilder(), ToXContent.EMPTY_PARAMS))
                    .id(testModel.getId())
                    .create(true);
            executor.submit(
                    () -> {
                        try (ThreadContext.StoredContext ignored = threadPool.getThreadContext().stashContext()) {
                            RestStatus restStatus = client.index(request).actionGet().status();
                            future.complete(restStatus);
                        } catch (Exception e) {
                            future.completeExceptionally(e);
                        }
                    }
            );
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return future;
    }

}
