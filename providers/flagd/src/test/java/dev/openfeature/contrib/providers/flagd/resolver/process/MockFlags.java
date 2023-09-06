package dev.openfeature.contrib.providers.flagd.resolver.process;

import dev.openfeature.contrib.providers.flagd.resolver.process.model.FeatureFlag;

import java.util.HashMap;
import java.util.Map;

public class MockFlags {

    static final Map<String, Object> booleanVariant;
    static final Map<String, Object> stringVariants;
    static final Map<String, Object> objectVariants;

    static {
        booleanVariant = new HashMap<>();
        booleanVariant.put("on", true);
        booleanVariant.put("off", false);

        stringVariants = new HashMap<>();
        stringVariants.put("loop", "loopAlg");
        stringVariants.put("binet", "binetAlg");

        Map<String, Object> typeA = new HashMap<>();
        typeA.put("key", "0165");
        typeA.put("date", "01.01.2000");

        Map<String, Object> typeB = new HashMap<>();
        typeB.put("key", "0166");
        typeB.put("date", "01.01.2010");

        objectVariants = new HashMap<>();
        objectVariants.put("typeA", typeA);
        objectVariants.put("typeB", typeB);
    }

    // correct flag
    static final FeatureFlag BOOLEAN_FLAG = new FeatureFlag("ENABLED", "on", booleanVariant, null);

    // correct flag
    static final FeatureFlag OBJECT_FLAG = new FeatureFlag("ENABLED", "typeA", objectVariants, null);

    // flag in disabled state
    static final FeatureFlag DISABLED_FLAG = new FeatureFlag("DISABLED", "on", booleanVariant, null);

    // incorrect flag - variant mismatch
    static final FeatureFlag VARIANT_MISMATCH_FLAG = new FeatureFlag("ENABLED", "true", stringVariants, null);

    // flag with targeting rule
    static final FeatureFlag FLAG_WIH_IF_IN_TARGET = new FeatureFlag("ENABLED", "loop", stringVariants,
            "{\"if\":[{\"in\":[\"@faas.com\",{\"var\":[\"email\"]}]},\"binet\",null]}");

    // flag with incorrect targeting rule
    static final FeatureFlag FLAG_WIH_INVALID_TARGET = new FeatureFlag("ENABLED", "loop", stringVariants,
            "{if this, then that}");
}
