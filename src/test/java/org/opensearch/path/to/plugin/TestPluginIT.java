/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.path.to.plugin;

import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
//import org.apache.hc.core5.http.ParseException;
//import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.my.plugin.TestPlugin;
import org.opensearch.plugins.Plugin;
import org.opensearch.test.OpenSearchIntegTestCase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;

@ThreadLeakScope(ThreadLeakScope.Scope.NONE)
@OpenSearchIntegTestCase.ClusterScope(scope = OpenSearchIntegTestCase.Scope.SUITE)
public class TestPluginIT extends OpenSearchIntegTestCase {

    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Collections.singletonList(TestPlugin.class);
    }

    public void testPluginInstalled() throws IOException, ParseException {
        Request request = new Request("POST", "/_plugins/_testplugin");
        String jsonString = "{\"testModel\": \"2024-10-02mcasas182265892186654\", \"descriptionTest\": \"probando\"}";
        request.setJsonEntity(jsonString);
        getRestClient().performRequest(request);
        Response response = getRestClient().performRequest(request);
        String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

        logger.info("response body: {}", body);
        assertThat(body, containsString("rename"));
    }
}
