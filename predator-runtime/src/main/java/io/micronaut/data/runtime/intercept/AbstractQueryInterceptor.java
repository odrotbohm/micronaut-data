package io.micronaut.data.runtime.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.intercept.PredatorInterceptor;
import io.micronaut.data.intercept.annotation.PredatorMethod;
import io.micronaut.data.store.Datastore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract interceptor that executes a {@link io.micronaut.data.annotation.Query}.
 * @param <T> The declaring type
 * @param <R> The return type
 * @since 1.0
 * @author graemerocher
 */
abstract class AbstractQueryInterceptor<T, R> implements PredatorInterceptor<T, R> {
    protected final Datastore datastore;

    AbstractQueryInterceptor(@Nonnull Datastore datastore) {
        ArgumentUtils.requireNonNull("datastore", datastore);
        this.datastore = datastore;
    }

    /**
     * Prepares a query for the given context.
     * @param context The context
     * @return The query
     */
    protected final PreparedQuery prepareQuery(MethodInvocationContext<T, R> context) {
        String query = context.getValue(Query.class, String.class).orElseThrow(() ->
                new IllegalStateException("No query present in method")
        );
        AnnotationValue<PredatorMethod> annotation = context.getAnnotation(PredatorMethod.class);
        if (annotation == null) {
            // this should never happen
            throw new IllegalStateException("No predator method configured");
        }

        Class rootEntity = annotation.get("rootEntity", Class.class)
                .orElseThrow(() -> new IllegalStateException("No root entity present in method"));

        List<AnnotationValue<Property>> parameterData = annotation.getAnnotations("parameterBinding", Property.class);
        Map<String, Object> parameterValues;
        if (CollectionUtils.isNotEmpty(parameterData)) {
            Map<String, Object> parameterValueMap = context.getParameterValueMap();
            parameterValues = new HashMap<>(parameterData.size());
            for (AnnotationValue<Property> annotationValue : parameterData) {
                String name = annotationValue.get("name", String.class).orElse(null);
                String argument = annotationValue.get("value", String.class).orElse(null);
                if (name != null && argument != null) {
                    parameterValues.put(name, parameterValueMap.get(argument));
                }
            }
        } else {
            parameterValues = Collections.emptyMap();
        }

        return new PreparedQuery(rootEntity, query, parameterValues);
    }

    /**
     * Represents a prepared query.
     */
    protected final class PreparedQuery {
        private final @Nonnull Class rootEntity;
        private final @Nonnull String query;
        private final @Nonnull Map<String, Object> parameterValues;

        /**
         * The default constructor.
         * @param rootEntity The root entity of the query
         * @param query The query itself
         * @param parameterValues The parameter values
         */
        public PreparedQuery(
                @Nonnull Class rootEntity,
                @Nonnull String query,
                @Nullable Map<String, Object> parameterValues) {
            this.rootEntity = rootEntity;
            this.query = query;
            this.parameterValues = parameterValues == null ? Collections.emptyMap() : parameterValues;
        }

        @Nonnull
        public Class<?> getRootEntity() {
            return rootEntity;
        }

        @Nonnull
        public String getQuery() {
            return query;
        }

        @Nonnull
        public Map<String, Object> getParameterValues() {
            return parameterValues;
        }
    }
}