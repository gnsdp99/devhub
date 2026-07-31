package com.devhub.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import({ContainerConfig.class, TestAddressPolicyConfig.class})
public abstract class AbstractIntegrationTest extends IntegrationTestSupport {
}