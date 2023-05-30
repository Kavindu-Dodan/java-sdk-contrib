package dev.openfeature.contrib.hooks.otel;

import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FlagEvaluationDetails;
import dev.openfeature.sdk.Hook;
import dev.openfeature.sdk.HookContext;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;

import java.util.Map;
import java.util.Optional;

import static dev.openfeature.contrib.hooks.otel.OtelCommons.ERROR_KEY;
import static dev.openfeature.contrib.hooks.otel.OtelCommons.REASON_KEY;
import static dev.openfeature.contrib.hooks.otel.OtelCommons.flagKeyAttributeKey;
import static dev.openfeature.contrib.hooks.otel.OtelCommons.providerNameAttributeKey;
import static dev.openfeature.contrib.hooks.otel.OtelCommons.variantAttributeKey;


/**
 * OpenTelemetry metric hook records metrics at different {@link Hook} stages.
 */
@SuppressWarnings("PMD.TooManyStaticImports")
public class OpenTelemetryMetricHook implements Hook {

    private static final String METER_NAME = "go.openfeature.dev";
    private static final String EVALUATION_ACTIVE_COUNT = "feature_flag.evaluation_active_count";
    private static final String EVALUATION_REQUESTS_TOTAL = "feature_flag.evaluation_requests_total";
    private static final String FLAG_EVALUATION_SUCCESS_TOTAL = "feature_flag.evaluation_success_total";
    private static final String FLAG_EVALUATION_ERROR_TOTAL = "feature_flag.evaluation_error_total";

    private final LongUpDownCounter activeFlagEvaluationsCounter;
    private final LongCounter evaluationRequestCounter;
    private final LongCounter evaluationSuccessCounter;
    private final LongCounter evaluationErrorCounter;

    /**
     * Construct a metric hook by providing an {@link OpenTelemetry} instance.
     */
    public OpenTelemetryMetricHook(final OpenTelemetry openTelemetry) {
        final Meter meter = openTelemetry.getMeter(METER_NAME);

        activeFlagEvaluationsCounter =
                meter.upDownCounterBuilder(EVALUATION_ACTIVE_COUNT)
                        .setDescription("active flag evaluations counter")
                        .build();

        evaluationRequestCounter = meter.counterBuilder(EVALUATION_REQUESTS_TOTAL)
                .setDescription("feature flag evaluation request counter")
                .build();

        evaluationSuccessCounter = meter.counterBuilder(FLAG_EVALUATION_SUCCESS_TOTAL)
                .setDescription("feature flag evaluation success counter")
                .build();

        evaluationErrorCounter = meter.counterBuilder(FLAG_EVALUATION_ERROR_TOTAL)
                .setDescription("feature flag evaluation error counter")
                .build();
    }


    @Override
    public Optional<EvaluationContext> before(HookContext ctx, Map hints) {
        activeFlagEvaluationsCounter.add(+1, Attributes.of(flagKeyAttributeKey, ctx.getFlagKey()));

        evaluationRequestCounter.add(+1, Attributes.of(
                flagKeyAttributeKey, ctx.getFlagKey(),
                providerNameAttributeKey, ctx.getProviderMetadata().getName()));
        return Optional.empty();
    }

    @Override
    public void after(HookContext ctx, FlagEvaluationDetails details, Map hints) {
        final AttributesBuilder attributesBuilder = Attributes.builder();

        attributesBuilder.put(flagKeyAttributeKey, ctx.getFlagKey());
        attributesBuilder.put(providerNameAttributeKey, ctx.getProviderMetadata().getName());

        if (details.getReason() != null) {
            attributesBuilder.put(REASON_KEY, details.getReason());
        }

        if (details.getVariant() != null) {
            attributesBuilder.put(variantAttributeKey, details.getVariant());
        } else {
            attributesBuilder.put(variantAttributeKey, String.valueOf(details.getValue()));
        }

        evaluationSuccessCounter.add(+1, attributesBuilder.build());
    }

    @Override
    public void error(HookContext ctx, Exception error, Map hints) {
        final AttributesBuilder attributesBuilder = Attributes.builder();

        attributesBuilder.put(flagKeyAttributeKey, ctx.getFlagKey());
        attributesBuilder.put(providerNameAttributeKey, ctx.getProviderMetadata().getName());

        if (error.getMessage() != null) {
            attributesBuilder.put(ERROR_KEY, error.getMessage());
        }

        evaluationErrorCounter.add(+1, attributesBuilder.build());
    }

    @Override
    public void finallyAfter(HookContext ctx, Map hints) {
        activeFlagEvaluationsCounter.add(-1, Attributes.of(flagKeyAttributeKey, ctx.getFlagKey()));
    }
}