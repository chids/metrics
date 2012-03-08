package com.yammer.metrics.weld.tests;

import java.util.concurrent.TimeUnit;

import com.yammer.metrics.annotation.Metered;

public class InstrumentedWithMetered {

	@Metered(name = "things", eventType = "poops", rateUnit = TimeUnit.MINUTES)
    public String doAThing() {
        return "poop";
    }

    @Metered
    String doAThingWithDefaultScope() {
        return "defaultResult";
    }

    @Metered
    protected String doAThingWithProtectedScope() {
        return "defaultProtected";
    }

    @Metered(group="metered", type="t", name="n")
    protected String doAThingWithGroupTypeAndName() {
        return "newGroupTypeAndName";
    }
}
