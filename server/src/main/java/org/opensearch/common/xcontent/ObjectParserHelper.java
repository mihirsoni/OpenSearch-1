/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.common.xcontent;

import org.opensearch.common.CheckedFunction;
import org.opensearch.common.ParseField;
import org.opensearch.common.bytes.BytesReference;
import org.opensearch.common.xcontent.ObjectParser.ValueType;
import org.opensearch.common.xcontent.json.JsonXContent;

import java.io.IOException;
import java.util.function.BiConsumer;

/**
 * This class provides helpers for {@link ObjectParser} that allow dealing with
 * classes outside of the xcontent dependencies.
 */
public final class ObjectParserHelper<Value, Context> {

    /**
     * Helper to declare an object that will be parsed into a {@link BytesReference}
     */
    public void declareRawObject(final AbstractObjectParser<Value, Context> parser,
                                 final BiConsumer<Value, BytesReference> consumer,
                                 final ParseField field) {
        final CheckedFunction<XContentParser, BytesReference, IOException> bytesParser = p -> {
            try (XContentBuilder builder = JsonXContent.contentBuilder()) {
                builder.copyCurrentStructure(p);
                return BytesReference.bytes(builder);
            }
        };
        parser.declareField(consumer, bytesParser, field, ValueType.OBJECT);
    }

}
