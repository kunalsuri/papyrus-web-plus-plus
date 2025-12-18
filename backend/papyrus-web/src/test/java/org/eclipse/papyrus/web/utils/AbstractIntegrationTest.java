/*****************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Obeo - Initial API and implementation
 *  Marcos Didonet Del Fabro (CEA LIST) - Issue 256
 *******************************************************************************/
package org.eclipse.papyrus.web.utils;

import org.eclipse.sirius.web.infrastructure.configuration.persistence.JDBCConfiguration;
import org.eclipse.sirius.web.starter.SiriusWebStarterConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Abstract test class for testing web UML features.
 *
 * @author Arthur Daussy
 * @author Marcos Didonet Del Fabro
 */
@SpringJUnitConfig(classes = { IntegrationTestConfiguration.class, SiriusWebStarterConfiguration.class, JDBCConfiguration.class })
public class AbstractIntegrationTest {

    // Emulate a POSTGRESQL database using testcontainers
    private static PostgreSQLContainer<?> postgreSQLContainer;

    @Value("${papyrusweb.integration.tests.datasource.url}")
    private String datasourceUrl;

    @Value("${papyrusweb.integration.tests.datasource.username}")
    private String datasourceUsername;

    @Value("${papyrusweb.integration.tests.datasource.password}")
    private String datasourcePassword;

    static {
        if (getTestContainersUse()) {
            postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest")).withReuse(true);
            postgreSQLContainer.start();
        } else {
            postgreSQLContainer = null;
        }
    }

    private static Boolean getTestContainersUse() {
        String testContainersUse = System.getProperty("papyrusweb.testcontainers.use");
        return (testContainersUse == null) || Boolean.valueOf(testContainersUse);
    }

    @DynamicPropertySource
    public static void registerProperties(DynamicPropertyRegistry registry) {
        if (postgreSQLContainer != null) {
            registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
            registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
            registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        } else {
            AbstractIntegrationTest integrationTestInstance = new AbstractIntegrationTest();
            registry.add("spring.datasource.url", integrationTestInstance::getDatasourceUrl);
            registry.add("spring.datasource.username", integrationTestInstance::getDatasourceUsername);
            registry.add("spring.datasource.password", integrationTestInstance::getDatasourcePassword);
        }
    }

    protected String getDatasourceUrl() {
        return this.datasourceUrl;
    }

    protected String getDatasourceUsername() {
        return this.datasourceUsername;
    }

    protected String getDatasourcePassword() {
        return this.datasourcePassword;
    }
}