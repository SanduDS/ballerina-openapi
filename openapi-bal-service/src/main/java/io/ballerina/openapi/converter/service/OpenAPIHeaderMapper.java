/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.openapi.converter.service;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.converter.Constants;
import io.ballerina.openapi.converter.utils.ConverterCommonUtils;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.openapi.converter.utils.ConverterCommonUtils.getAnnotationNodesFromServiceNode;

/**
 * This class for the mapping ballerina headers with OAS header parameter sections.
 *
 * @since 2.0.0
 */
public class OpenAPIHeaderMapper {
    /**
     * Handle header parameters in ballerina data type.
     *
     * @param headerParam    -  {@link RequiredParameterNode} type header parameter node
     */
    public List<Parameter> setHeaderParameter(RequiredParameterNode headerParam) {
        List<Parameter> parameters = new ArrayList<>();
        String headerName = headerParam.paramName().get().text().replaceAll("\\\\", "");
        HeaderParameter headerParameter = new HeaderParameter();
        Node node = headerParam.typeName();
        StringSchema stringSchema = new StringSchema();
        NodeList<AnnotationNode> annotations = getAnnotationNodesFromServiceNode(headerParam);
        String isOptional = Constants.TRUE;
        if (!annotations.isEmpty()) {
            Optional<String> values = ConverterCommonUtils.extractServiceAnnotationDetails(annotations,
                    "http:ServiceConfig", "treatNilableAsOptional");
            if (values.isPresent()) {
                isOptional = values.get();
            }
        }
        enableHeaderRequiredOption(headerParameter, node, stringSchema, isOptional);
        completeHeaderParameter(parameters, headerName, headerParameter, stringSchema, headerParam.annotations(),
                headerParam.typeName());
        return parameters;
    }

    /**
     * Handle header parameters in ballerina data type.
     *
     * @param headerParam    -  {@link DefaultableParameterNode} type header parameter node
     */
    public List<Parameter> setHeaderParameter(DefaultableParameterNode headerParam) {
        List<Parameter> parameters = new ArrayList<>();
        String headerName = headerParam.paramName().get().text().replaceAll("\\\\", "");
        HeaderParameter headerParameter = new HeaderParameter();
        StringSchema stringSchema = new StringSchema();
        if (headerParam.expression().kind() == SyntaxKind.STRING_LITERAL) {
            stringSchema.setDefault(headerParam.expression().toString().replaceAll("\"", ""));
        }
        if (headerParam.typeName().kind() == SyntaxKind.OPTIONAL_TYPE_DESC) {
            stringSchema.setNullable(true);
        }
        completeHeaderParameter(parameters, headerName, headerParameter, stringSchema, headerParam.annotations(),
                headerParam.typeName());
        return parameters;
    }

    /**
     * Assign header values to OAS header parameter.
     */
    private void completeHeaderParameter(List<Parameter> parameters, String headerName, HeaderParameter headerParameter,
                                         StringSchema stringSchema, NodeList<AnnotationNode> annotations, Node node) {

        if (!annotations.isEmpty()) {
            AnnotationNode annotationNode = annotations.get(0);
            headerName = getHeaderName(headerName, annotationNode);
        }
        if (node instanceof ArrayTypeDescriptorNode) {
            ArrayTypeDescriptorNode arrayNode = (ArrayTypeDescriptorNode) node;
            if (arrayNode.memberTypeDesc().kind() == SyntaxKind.STRING_TYPE_DESC) {
                ArraySchema arraySchema = new ArraySchema();
                arraySchema.setItems(stringSchema);
                headerParameter.schema(arraySchema);
                headerParameter.setName(headerName);
                parameters.add(headerParameter);
            }
        } else {
            headerParameter.schema(stringSchema);
            headerParameter.setName(headerName);
            parameters.add(headerParameter);
        }
    }

    private void enableHeaderRequiredOption(HeaderParameter headerParameter, Node node, StringSchema stringSchema,
                                            String isOptional) {
        if (node.kind() == SyntaxKind.OPTIONAL_TYPE_DESC) {
            stringSchema.setNullable(true);
            if (isOptional.equals(Constants.FALSE)) {
                headerParameter.setRequired(true);
            }
        } else {
            headerParameter.setRequired(true);
        }
    }

    /**
     * Extract header name from header annotation value.
     *
     * @param headerName        - Header name
     * @param annotationNode    - Related annotation for extract details
     * @return                  - Updated header name
     */
    private String getHeaderName(String headerName, AnnotationNode annotationNode) {
        if (annotationNode.annotValue().isPresent()) {
            MappingConstructorExpressionNode fieldNode = annotationNode.annotValue().get();
            SeparatedNodeList<MappingFieldNode> fields = fieldNode.fields();
            for (MappingFieldNode field: fields) {
                SpecificFieldNode sField = (SpecificFieldNode) field;
                if (sField.fieldName().toString().trim().equals("name") && sField.valueExpr().isPresent()) {
                    return sField.valueExpr().get().toString().trim().replaceAll("\"", "");
                }
            }
        }
        return headerName;
    }
}
