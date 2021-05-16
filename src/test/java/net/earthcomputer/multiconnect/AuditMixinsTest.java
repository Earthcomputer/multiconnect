package net.earthcomputer.multiconnect;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class AuditMixinsTest {
    @BeforeAll
    static void beforeAll() {
        TestUtil.callBootstrap();
    }

    @Test
    public void auditMixins() {
        MixinEnvironment.getDefaultEnvironment().audit();
    }
}
