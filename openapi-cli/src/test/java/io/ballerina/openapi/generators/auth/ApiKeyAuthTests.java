/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.generators.auth;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorUtils;
import io.ballerina.openapi.generators.client.BallerinaAuthConfigGenerator;
import io.ballerina.openapi.generators.common.TestConstants;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

/**
 * All the tests related to the auth related code snippet generation for api key auth mechanism.
 */
public class ApiKeyAuthTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client/").toAbsolutePath();
    BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator = new BallerinaAuthConfigGenerator(true, false);

    @Test(description = "Generate config record for openweathermap api", dataProvider = "apiKeyAuthIOProvider")
    public void testGetConfigRecord(String yamlFile) throws IOException, BallerinaOpenApiException {
        // generate ApiKeysConfig record
        Path definitionPath = RES_DIR.resolve("auth/scenarios/api_key/" + yamlFile);
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        String generatedConfigRecord = Objects.requireNonNull(
                ballerinaAuthConfigGenerator.getConfigRecord(openAPI)).toString();
        Assert.assertFalse(generatedConfigRecord.isBlank());
    }

    @Test(description = "Test the generation of ApiKey map local variable",
            dependsOnMethods = {"testGetConfigRecord"})
    public void testGetApiKeyMapClassVariable () {
        String expectedClassVariable = TestConstants.API_KEY_CONFIG_VAR;
        String generatedClassVariable = ballerinaAuthConfigGenerator.getApiKeyMapClassVariable().toString();
        generatedClassVariable = (generatedClassVariable.trim()).replaceAll("\\s+", "");
        expectedClassVariable = (expectedClassVariable.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(generatedClassVariable, expectedClassVariable);
    }

    @Test(description = "Test the generation of api key related parameters in class init function signature",
            dependsOnMethods = {"testGetApiKeyMapClassVariable"})
    public void testGetConfigParamForClassInit() {
        String expectedParams = TestConstants.API_KEY_CONFIG_PARAM;
        StringBuilder generatedParams = new StringBuilder();
        List<Node> generatedInitParamNodes = ballerinaAuthConfigGenerator.getConfigParamForClassInit(
                "https:localhost/8080");
        for (Node param: generatedInitParamNodes) {
            generatedParams.append(param.toString());
        }
        expectedParams = (expectedParams.trim()).replaceAll("\\s+", "");
        String generatedParamsStr = (generatedParams.toString().trim()).replaceAll("\\s+", "");
        Assert.assertEquals(generatedParamsStr, expectedParams);
    }

    @Test(description = "Test the generation of api key assignment node",
            dependsOnMethods = {"testGetConfigRecord"})
    public void testGetApiKeyAssignmentNode () {
        String expectedAssignmentNode = TestConstants.API_KEY_ASSIGNMENT;
        String generatedAssignmentNode = Objects.requireNonNull
                (ballerinaAuthConfigGenerator.getApiKeyAssignmentNode()).toString();
        generatedAssignmentNode = (generatedAssignmentNode.trim()).replaceAll("\\s+", "");
        expectedAssignmentNode = (expectedAssignmentNode.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(generatedAssignmentNode, expectedAssignmentNode);
    }

    @Test(description = "Test the generation of api key documentation comment when multiple api keys defined")
    public void testGetApiKeyDescriptionForMultipleApiKeys () throws IOException, BallerinaOpenApiException {
        BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator = new BallerinaAuthConfigGenerator(true, false);
        Path definitionPath = RES_DIR.resolve("auth/scenarios/api_key/multiple_apikey_descriptions.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        String expectedConfigRecord = TestConstants.MULTIPLE_API_KEY_RECORD;
        String generatedConfigRecord = Objects.requireNonNull(
                ballerinaAuthConfigGenerator.getConfigRecord(openAPI)).toString();
        generatedConfigRecord = (generatedConfigRecord.trim()).replaceAll("\\s+", "");
        expectedConfigRecord = (expectedConfigRecord.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(generatedConfigRecord, expectedConfigRecord);
    }

    @Test(description = "Test ApiKeysConfig record generation for multiple api keys")
    public void testConfigRecordGenForAPIKeyAuth() throws IOException, BallerinaOpenApiException {
        // generate ApiKeysConfig record
        BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator = new BallerinaAuthConfigGenerator(true, false);
        String expectedConfigRecord = TestConstants.API_KEYS_CONFIG_RECORD;
        Path definitionPath = RES_DIR.resolve("auth/scenarios/api_key/multiple_api_keys.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        String generatedConfigRecord = Objects.requireNonNull(
                ballerinaAuthConfigGenerator.getConfigRecord(openAPI)).toString();
        Assert.assertFalse(generatedConfigRecord.isBlank());
        generatedConfigRecord = (generatedConfigRecord.trim()).replaceAll("\\s+", "");
        expectedConfigRecord = (expectedConfigRecord.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(generatedConfigRecord, expectedConfigRecord);
    }

    @Test(description = "Test ApiKeysConfig record documentation generation for multiline descriptions")
    public void testConfigRecordDocumentationGen() throws IOException, BallerinaOpenApiException {
        // generate ApiKeysConfig record
        BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator = new BallerinaAuthConfigGenerator(true, false);
        String expectedConfigRecord = TestConstants.MULTI_LINE_API_KEY_DESC;
        Path definitionPath = RES_DIR.resolve("auth/scenarios/api_key/multiline_api_key_desc.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        String generatedConfigRecord = Objects.requireNonNull(
                ballerinaAuthConfigGenerator.getConfigRecord(openAPI)).toString();
        Assert.assertFalse(generatedConfigRecord.isBlank());
        generatedConfigRecord = (generatedConfigRecord.trim()).replaceAll("\\s+", "");
        expectedConfigRecord = (expectedConfigRecord.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(generatedConfigRecord, expectedConfigRecord);
    }

    @DataProvider(name = "apiKeyAuthIOProvider")
    public Object[] dataProvider() {
        return new Object[]{
                "header_api_key.yaml",
                "query_api_key.yaml",
                "multiple_api_keys.yaml",
                "header_api_key_only.yaml"
        };
    }
}
