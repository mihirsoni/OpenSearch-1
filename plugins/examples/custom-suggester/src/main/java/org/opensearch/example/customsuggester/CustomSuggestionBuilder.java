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
 *    http://www.apache.org/licenses/LICENSE-2.0
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

package org.opensearch.example.customsuggester;

import org.opensearch.common.ParseField;
import org.opensearch.common.ParsingException;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.common.lucene.BytesRefs;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.common.xcontent.XContentParser;
import org.opensearch.index.query.QueryShardContext;
import org.opensearch.search.suggest.SuggestionBuilder;
import org.opensearch.search.suggest.SuggestionSearchContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CustomSuggestionBuilder extends SuggestionBuilder<CustomSuggestionBuilder> {

    public static final String SUGGESTION_NAME = "custom";

    protected static final ParseField RANDOM_SUFFIX_FIELD = new ParseField("suffix");

    private String randomSuffix;

    public CustomSuggestionBuilder(String randomField, String randomSuffix) {
        super(randomField);
        this.randomSuffix = randomSuffix;
    }

    /**
     * Read from a stream.
     */
    public CustomSuggestionBuilder(StreamInput in) throws IOException {
        super(in);
        this.randomSuffix = in.readString();
    }

    @Override
    public void doWriteTo(StreamOutput out) throws IOException {
        out.writeString(randomSuffix);
    }

    @Override
    protected XContentBuilder innerToXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field(RANDOM_SUFFIX_FIELD.getPreferredName(), randomSuffix);
        return builder;
    }

    @Override
    public String getWriteableName() {
        return SUGGESTION_NAME;
    }

    @Override
    protected boolean doEquals(CustomSuggestionBuilder other) {
        return Objects.equals(randomSuffix, other.randomSuffix);
    }

    @Override
    protected int doHashCode() {
        return Objects.hash(randomSuffix);
    }

    public static CustomSuggestionBuilder fromXContent(XContentParser parser) throws IOException {
        XContentParser.Token token;
        String currentFieldName = null;
        String fieldname = null;
        String suffix = null;
        String analyzer = null;
        int sizeField = -1;
        int shardSize = -1;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token.isValue()) {
                if (SuggestionBuilder.ANALYZER_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    analyzer = parser.text();
                } else if (SuggestionBuilder.FIELDNAME_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    fieldname = parser.text();
                } else if (SuggestionBuilder.SIZE_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    sizeField = parser.intValue();
                } else if (SuggestionBuilder.SHARDSIZE_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    shardSize = parser.intValue();
                } else if (RANDOM_SUFFIX_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    suffix = parser.text();
                }
            } else {
                throw new ParsingException(parser.getTokenLocation(),
                    "suggester[custom] doesn't support field [" + currentFieldName + "]");
            }
        }

        // now we should have field name, check and copy fields over to the suggestion builder we return
        if (fieldname == null) {
            throw new ParsingException(parser.getTokenLocation(), "the required field option is missing");
        }
        CustomSuggestionBuilder builder = new CustomSuggestionBuilder(fieldname, suffix);
        if (analyzer != null) {
            builder.analyzer(analyzer);
        }
        if (sizeField != -1) {
            builder.size(sizeField);
        }
        if (shardSize != -1) {
            builder.shardSize(shardSize);
        }
        return builder;
    }

    @Override
    public SuggestionSearchContext.SuggestionContext build(QueryShardContext context) throws IOException {
        Map<String, Object> options = new HashMap<>();
        options.put(FIELDNAME_FIELD.getPreferredName(), field());
        options.put(RANDOM_SUFFIX_FIELD.getPreferredName(), randomSuffix);
        CustomSuggestionContext customSuggestionsContext = new CustomSuggestionContext(context, options);
        customSuggestionsContext.setField(field());
        assert text != null;
        customSuggestionsContext.setText(BytesRefs.toBytesRef(text));
        return customSuggestionsContext;
    }

}
