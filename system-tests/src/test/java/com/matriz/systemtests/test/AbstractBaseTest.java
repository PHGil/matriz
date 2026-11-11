package com.matriz.systemtests.test;

import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import com.matriz.systemtests.database.EnvironmentHelper;

/** Base class of all system tests: every test starts from an empty database with the demo users registered. */
public abstract class AbstractBaseTest {

    private static final Logger LOG = Logger.getLogger(AbstractBaseTest.class.getName());

    protected final EnvironmentHelper environmentHelper = new EnvironmentHelper();

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        LOG.info(() -> "Running " + testInfo.getDisplayName());
        environmentHelper.reset();
    }
}
