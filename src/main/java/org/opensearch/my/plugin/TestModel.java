/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.my.plugin;

import org.opensearch.core.xcontent.ToXContentObject;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.core.xcontent.XContentParser;
import reactor.util.annotation.NonNull;

import java.io.IOException;

public class TestModel implements ToXContentObject {
    public static final String NAME = "testModel";
    public static final String DESCRIPTION = "descriptionTest";
    private final String id;
    private final String description;

    public TestModel(
            @NonNull String id,
            @NonNull String description) {
        this.id = id;
        this.description = description;
    }

    public static TestModel parse(XContentParser parser) throws IOException {
        String id = null;
        String description = null;

        while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
            String fieldName = parser.currentName();

            parser.nextToken();
            switch (fieldName) {
                case NAME:
                    id = parser.text();
                    break;
                case DESCRIPTION:
                    description = parser.text();
                    break;
                default:
                    break;

            }
        }
        return new TestModel(id, description);
    }

    /**
     * @return Document's ID
     */
    public String getId() {
        return this.id;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        builder.field(NAME, this.id);
        builder.field(DESCRIPTION, this.description);
        return builder.endObject();
    }
}
