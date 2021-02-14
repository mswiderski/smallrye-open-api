package io.smallrye.openapi.runtime.scanner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.json.JsonObject;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;

import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterStyle;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Encoding;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexWriter;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.annotations.providers.multipart.PartType;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestMatrix;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

/**
 * @author Michael Edgar {@literal <michael@xlate.io>}
 */
class ParameterScanTests extends IndexScannerTestBase {

    private static void test(String expectedResource, Class<?>... classes) throws IOException, JSONException {
        Index index = indexOf(classes);
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals(expectedResource, result);
    }

    @Test
    void testIgnoredMpOpenApiHeaders() throws IOException, JSONException {
        test("params.ignored-mp-openapi-headers.json",
                IgnoredMpOpenApiHeaderArgsTestResource.class,
                Widget.class);
    }

    @Test
    void testParameterOnMethod() throws IOException, JSONException {
        test("params.parameter-on-method.json",
                ParameterOnMethodTestResource.class,
                Widget.class);
    }

    @Test
    void testParameterOnField() throws IOException, JSONException {
        test("params.parameter-on-field.json",
                ResourcePathParamTestResource.class,
                Widget.class);
    }

    @Test
    void testParameterInBeanFromField() throws IOException, JSONException {
        test("params.parameter-in-bean-from-field.json",
                ParameterInBeanFromFieldTestResource.class,
                ParameterInBeanFromFieldTestResource.Bean.class,
                Widget.class);
    }

    @Test
    void testParameterInBeanFromSetter() throws IOException, JSONException {
        test("params.parameter-in-bean-from-setter.json",
                ParameterInBeanFromSetterTestResource.class,
                ParameterInBeanFromSetterTestResource.Bean.class,
                Widget.class);
    }

    @Test
    void testPathParamWithFormParams() throws IOException, JSONException {
        test("params.path-param-with-form-params.json",
                PathParamWithFormParamsTestResource.class,
                Widget.class);
    }

    @Test
    void testMultipleContentTypesWithFormParams() throws IOException, JSONException {
        test("params.multiple-content-types-with-form-params.json",
                MultipleContentTypesWithFormParamsTestResource.class,
                Widget.class);
    }

    @Test
    void testParametersInConstructor() throws IOException, JSONException {
        test("params.parameters-in-constructor.json",
                ParametersInConstructorTestResource.class,
                ParametersInConstructorTestResource.Bean.class,
                Widget.class);
    }

    @Test
    void testMatrixParamsOnResourceMethodArgs() throws IOException, JSONException {
        test("params.matrix-params-on-resource-method-args.json",
                MatrixParamsOnResourceMethodArgsTestResource.class,
                Widget.class);
    }

    @Test
    void testMatrixParamsOnResourceMethodCustomName() throws IOException, JSONException {
        test("params.matrix-params-on-resource-method-custom-name.json",
                MatrixParamsOnResourceMethodCustomNameTestResource.class,
                Widget.class);
    }

    @Test
    void testMatrixParamsOnMethodAndFieldArgs() throws IOException, JSONException {
        test("params.matrix-params-on-method-and-field-args.json",
                MatrixParamsOnMethodAndFieldArgsTestResource.class,
                Widget.class);
    }

    @Test
    void testAllTheParams() throws IOException, JSONException {
        test("params.all-the-params.json",
                AllTheParamsTestResource.class,
                AllTheParamsTestResource.Bean.class,
                Widget.class);
        test("params.all-the-params.json",
                RestEasyReactiveAllTheParamsTestResource.class,
                RestEasyReactiveAllTheParamsTestResource.Bean.class,
                Widget.class);
    }

    @Test
    void testMultipartForm() throws IOException, JSONException {
        test("params.multipart-form.json",
                MultipartFormTestResource.class,
                MultipartFormTestResource.Bean.class,
                Widget.class,
                InputStream.class);
    }

    @Test
    void testEnumQueryParam() throws IOException, JSONException {
        test("params.enum-form-param.json",
                EnumQueryParamTestResource.class,
                EnumQueryParamTestResource.TestEnum.class,
                EnumQueryParamTestResource.TestEnumWithSchema.class);
    }

    @Test
    void testUUIDQueryParam() throws IOException, JSONException {
        test("params.uuid-params-responses.json",
                UUIDQueryParamTestResource.class,
                UUIDQueryParamTestResource.WrappedUUID.class);
    }

    @Test
    void testRestEasyFieldsAndSetters() throws IOException, JSONException {
        test("params.resteasy-fields-and-setters.json",
                RestEasyFieldsAndSettersTestResource.class);
    }

    @Test
    void testCharSequenceArrayParam() throws IOException, JSONException {
        test("params.char-sequence-arrays.json",
                CharSequenceArrayParamTestResource.class,
                CharSequenceArrayParamTestResource.EchoResult.class);
    }

    @Test
    void testOptionalParam() throws IOException, JSONException {
        test("params.optional-types.json",
                OptionalParamTestResource.class,
                OptionalParamTestResource.Bean.class,
                OptionalParamTestResource.NestedBean.class,
                OptionalParamTestResource.OptionalWrapper.class,
                Optional.class,
                OptionalDouble.class,
                OptionalLong.class);
    }

    @Test
    void testPathParamTemplateRegex() throws IOException, JSONException {
        test("params.path-param-templates.json",
                PathParamTemplateRegexTestResource.class);
    }

    @Test
    void testPathSegmentMatrix() throws IOException, JSONException {
        test("params.path-segment-param.json",
                PathSegmentMatrixTestResource.class);
    }

    @Test
    void testParamNameOverride() throws IOException, JSONException {
        test("params.param-name-override.json",
                ParamNameOverrideTestResource.class);
    }

    @Test
    void testCommonTargetMethodParameter() throws IOException, JSONException {
        test("params.common-annotation-target-method.json", CommonTargetMethodParameterResource.class);
    }

    @Test
    void testRestEasyReactivePathParamOmitted() throws IOException, JSONException {
        test("params.resteasy-reactive-missing-restpath.json", RestEasyReactivePathParamOmittedTestResource.class,
                Widget.class);
    }

    @Test
    void testSerializedIndexParameterAnnotations() throws IOException, JSONException {
        Index i1 = indexOf(GreetResource.class, GreetResource.GreetingMessage.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IndexWriter writer = new IndexWriter(out);
        writer.write(i1);

        Index index = new IndexReader(new ByteArrayInputStream(out.toByteArray())).read();
        OpenApiAnnotationScanner scanner = new OpenApiAnnotationScanner(emptyConfig(), index);
        OpenAPI result = scanner.scan();
        printToConsole(result);
        assertJsonEquals("params.serialized-annotation-index.json", result);
    }

    @Test
    void testParameterRefOnly() throws IOException, JSONException {
        test("params.parameter-ref-property.json", ParameterRefTestApplication.class, ParameterRefTestResource.class);
    }

    @Test
    void testDefaultEnumValue() throws IOException, JSONException {
        test("params.local-schema-attributes.json", DefaultEnumTestResource.class, DefaultEnumTestResource.MyEnum.class);
    }

    @Test
    void testGenericTypeVariableResource() throws IOException, JSONException {
        test("params.generic-type-variables.json", BaseGenericResource.class, BaseGenericResource.GenericBean.class,
                IntegerStringUUIDResource.class);
    }

    /***************** Test models and resources below. ***********************/

    public static class Widget {
        String id;
        String name;
    }

    @Path("ignored-headers")
    static class IgnoredMpOpenApiHeaderArgsTestResource {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @SuppressWarnings("unused")
        public Widget get(@HeaderParam("Authorization") String auth,
                @HeaderParam("Content-Type") String contentType,
                @HeaderParam("Accept") String accept,
                @HeaderParam("X-Custom-Header") String custom) {
            return null;
        }
    }

    @Path("parameter-on-method/{id}")
    static class ParameterOnMethodTestResource {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @SuppressWarnings("unused")
        @Parameter(name = "X-Custom-Header", in = ParameterIn.HEADER, required = true)
        @Parameter(name = "id", in = ParameterIn.PATH)
        public Widget get(@HeaderParam("X-Custom-Header") String custom,
                @PathParam("id") @DefaultValue("000") String id) {
            return null;
        }
    }

    @Path("/parameter-on-field/{id}")
    static class ResourcePathParamTestResource {
        @PathParam("id")
        @DefaultValue("ABC")
        String id;

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Widget get() {
            return null;
        }
    }

    @Path("/parameter-in-bean-from-field/{id}")
    static class ParameterInBeanFromFieldTestResource {
        static class Bean {
            @PathParam("id")
            @DefaultValue("BEAN")
            String id;
        }

        @BeanParam
        private Bean param;

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Widget get() {
            return null;
        }
    }

    @Path("/parameter-in-bean-from-setter/{id}/{id2}")
    @SuppressWarnings("unused")
    static class ParameterInBeanFromSetterTestResource {
        static class Bean {
            @PathParam("id")
            @DefaultValue("BEAN-FROM-SETTER")
            String id;
        }

        private Bean param;

        @BeanParam
        public void setParam(Bean param) {
            this.param = param;
        }

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Widget get(@PathParam("id2") String id2) {
            return null;
        }
    }

    @Path("/path-param-with-form-params/{id}")
    @SuppressWarnings("unused")
    static class PathParamWithFormParamsTestResource {
        @PathParam("id")
        @DefaultValue("12345")
        @NotNull
        @Size(min = 1, max = 12)
        String id;

        @FormParam("form-param1")
        // Not supported on resource fields and parameters
        private String formParam1;

        @POST
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Produces(MediaType.APPLICATION_JSON)
        public Widget update(@FormParam("form-param2") @Size(max = 10) String formParam2,
                @FormParam("qualifiers") java.util.SortedSet<String> qualifiers) {
            return null;
        }
    }

    @Path("/multiple-content-types-with-form-params")
    @SuppressWarnings("unused")
    static class MultipleContentTypesWithFormParamsTestResource {
        @POST
        @Path("/widgets/create")
        @Consumes(MediaType.APPLICATION_JSON)
        @Operation(operationId = "createWidget")
        public void createWidget(
                @RequestBody(required = true, content = @Content(schema = @Schema(implementation = Widget.class))) final Widget w) {
        }

        @POST
        @Path("/widgets/create")
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Operation(operationId = "createWidget")
        public void createWidget(@FormParam("id") String id, @FormParam("name") String name) {
        }
    }

    @Path("/parameters-in-constructor/{id}/{p1}")
    @SuppressWarnings("unused")
    static class ParametersInConstructorTestResource {
        static class Bean {
            @PathParam("id")
            @DefaultValue("BEAN")
            String id;
        }

        private Bean param;

        public ParametersInConstructorTestResource(
                @Parameter(name = "h1", in = ParameterIn.HEADER, description = "Description of h1") @HeaderParam("h1") @Deprecated String h1,
                @Parameter(name = "h2", in = ParameterIn.HEADER, hidden = true) @HeaderParam("h2") String h2,
                @Parameter(name = "q1", deprecated = true) @QueryParam("q1") String q1,
                @NotNull @CookieParam("c1") String c1,
                @PathParam("p1") String p1,
                @BeanParam Bean b1) {
        }

        @DELETE
        public void deleteWidget() {
        }
    }

    @Path("/matrix-params-on-resource-method-args/{id}")
    @SuppressWarnings("unused")
    static class MatrixParamsOnResourceMethodArgsTestResource {
        @PathParam("id")
        @NotNull
        @Size(max = 10)
        String id;

        @GET
        @Path("/anotherpathsegment/reloaded/")
        @Produces(MediaType.APPLICATION_JSON)
        public Widget get(@MatrixParam("m1") @DefaultValue("default-m1") String m1,
                @MatrixParam("m2") @Size(min = 20) String m2) {
            return null;
        }
    }

    @Path("/matrix-params-on-resource-method-custom-name/{id}")
    @SuppressWarnings("unused")
    static class MatrixParamsOnResourceMethodCustomNameTestResource {
        @PathParam("id")
        @Size(max = 10)
        String id;

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Parameter(name = "id", in = ParameterIn.PATH, style = ParameterStyle.MATRIX, description = "Additional information for id2")
        public Widget get(@MatrixParam("m1") @DefaultValue("default-m1") String m1,
                @MatrixParam("m2") @Size(min = 20) String m2) {
            return null;
        }
    }

    @Path("/matrix-params-on-method-and-field-args/{id}")
    @SuppressWarnings("unused")
    static class MatrixParamsOnMethodAndFieldArgsTestResource {
        @PathParam("id")
        @Size(max = 10)
        String id;

        @MatrixParam("c1")
        @Schema(type = SchemaType.STRING, format = "custom-but-ignored")
        String c1;

        @MatrixParam("c2")
        String c2;

        @GET
        @Path("/seg1/seg2/resourceA")
        @Produces(MediaType.APPLICATION_JSON)
        @Parameter(in = ParameterIn.PATH, name = "resourceA", style = ParameterStyle.MATRIX)
        public Widget get(@MatrixParam("m1") @DefaultValue("default-m1") int m1,
                @MatrixParam("m2") @DefaultValue("100") @Max(200) int m2) {
            return null;
        }
    }

    @Path("/all/the/params/{id1}/{id2}")
    @SuppressWarnings("unused")
    static class AllTheParamsTestResource {
        public AllTheParamsTestResource(@PathParam("id1") int id1,
                @org.jboss.resteasy.annotations.jaxrs.PathParam String id2) {
        }

        static class Bean {
            @org.jboss.resteasy.annotations.jaxrs.MatrixParam
            @DefaultValue("BEAN1")
            String matrixF1;

            @MatrixParam("matrixF2")
            @DefaultValue("BEAN2")
            String matrixF2;

            @org.jboss.resteasy.annotations.jaxrs.CookieParam
            @DefaultValue("COOKIE1")
            @Deprecated
            String cookieF1;
        }

        @Parameter(in = ParameterIn.PATH, style = ParameterStyle.MATRIX, name = "id2")
        @BeanParam
        private Bean param;

        @POST
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Produces(MediaType.APPLICATION_JSON)
        public CompletionStage<Widget> upd(@FormParam("f1") @DefaultValue("42") int f1,
                @org.jboss.resteasy.annotations.jaxrs.FormParam @DefaultValue("f2-default") @NotNull String f2,
                @HeaderParam("h1") @Deprecated int h1,
                @org.jboss.resteasy.annotations.jaxrs.HeaderParam("h2") String notH2) {
            return null;
        }

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Widget get(@QueryParam("q1") @Deprecated long q1,
                @org.jboss.resteasy.annotations.jaxrs.QueryParam("q2") String notQ2) {
            return null;
        }
    }

    @Path("/multipart/{id1}/{id2}")
    @SuppressWarnings("unused")
    static class MultipartFormTestResource {
        public MultipartFormTestResource(@PathParam("id1") int id1,
                @org.jboss.resteasy.annotations.jaxrs.PathParam String id2) {
        }

        static class Bean {
            @org.jboss.resteasy.annotations.jaxrs.FormParam
            @DefaultValue("f1-default")
            String formField1;

            @FormParam("f2")
            @DefaultValue("default2")
            @PartType("text/plain")
            String formField2;

            @FormParam("data")
            private InputPart data;

            @FormParam("binaryData")
            @PartType(MediaType.APPLICATION_OCTET_STREAM)
            @Schema(type = SchemaType.STRING, format = "binary")
            private byte[] binaryData;

            @FormParam("file")
            @PartType(MediaType.APPLICATION_OCTET_STREAM)
            @Schema(type = SchemaType.STRING, format = "binary")
            private InputStream file;

            @FormParam("undocumentedFile")
            @PartType(MediaType.APPLICATION_OCTET_STREAM)
            @Schema(hidden = true)
            private InputStream undocumentedFile;
        }

        @POST
        @Consumes(MediaType.MULTIPART_FORM_DATA)
        @Produces(MediaType.APPLICATION_JSON)
        @RequestBody(content = @Content(schema = @Schema(requiredProperties = { "f3" }), encoding = {
                @Encoding(name = "formField1", contentType = "text/x-custom-type") }))
        public CompletableFuture<Widget> upd(@MultipartForm Bean form,
                @FormParam("f3") @DefaultValue("3") int formField3,
                @org.jboss.resteasy.annotations.jaxrs.FormParam @NotNull String formField4) {
            return null;
        }
    }

    @Path("/enum/formparam")
    static class EnumQueryParamTestResource {
        static enum TestEnum {
            VAL1,
            VAL2,
            VAL3;
        }

        @Schema(name = "RestrictedEnum", title = "Restricted enum with fewer values", enumeration = { "VAL1",
                "VAL3" }, externalDocs = @ExternalDocumentation(description = "When to use RestrictedEnum?", url = "http://example.com/RestrictedEnum/info.html"))
        static enum TestEnumWithSchema {
            VAL1,
            VAL2,
            VAL3;
        }

        @SuppressWarnings("unused")
        @POST
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Produces(MediaType.TEXT_PLAIN)
        public TestEnum postData(@QueryParam("val") TestEnum value, @QueryParam("restr") TestEnumWithSchema restr) {
            return null;
        }
    }

    @Path("/uuid")
    static class UUIDQueryParamTestResource {
        static class WrappedUUID {
            @Schema(format = "uuid", description = "test")
            UUID theUUID;
        }

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public WrappedUUID[] echoWrappedUUID(@QueryParam("val") UUID value) {
            WrappedUUID result = new WrappedUUID();
            result.theUUID = value;
            return new WrappedUUID[] { result };
        }

        @POST
        @Consumes(MediaType.TEXT_PLAIN)
        @Produces(MediaType.TEXT_PLAIN)
        public UUID echoPostedUUID(UUID value) {
            return value;
        }
    }

    /**
     * Test class from example in issue #197.
     * 
     * https://github.com/smallrye/smallrye-open-api/issues/197
     * 
     */
    @SuppressWarnings("unused")
    @Path("/test")
    public class RestEasyFieldsAndSettersTestResource {

        @GET
        public String getTest() {
            return "TEST";
        }

        // make sure these don't break the build when fields
        @org.jboss.resteasy.annotations.jaxrs.PathParam
        String pathField;
        @org.jboss.resteasy.annotations.jaxrs.FormParam
        String formField;
        @org.jboss.resteasy.annotations.jaxrs.CookieParam
        String cookieField;
        @org.jboss.resteasy.annotations.jaxrs.HeaderParam
        String headerField;
        @org.jboss.resteasy.annotations.jaxrs.MatrixParam
        String matrixField;
        @org.jboss.resteasy.annotations.jaxrs.QueryParam
        String queryField;
        String unusedfield;

        // make sure these don't break the build when properties
        public String getPathProperty() {
            return null;
        }

        @org.jboss.resteasy.annotations.jaxrs.PathParam
        public void setPathProperty(String p) {
        }

        public String getFormProperty() {
            return null;
        }

        @org.jboss.resteasy.annotations.jaxrs.FormParam
        public void setFormProperty(String p) {
        }

        public String getCookieProperty() {
            return null;
        }

        @org.jboss.resteasy.annotations.jaxrs.CookieParam
        public void setCookieProperty(String p) {
        }

        public String getHeaderProperty() {
            return null;
        }

        @org.jboss.resteasy.annotations.jaxrs.HeaderParam
        public void setHeaderProperty(String p) {
        }

        // This annotation is not considered for processing
        @org.jboss.resteasy.annotations.jaxrs.MatrixParam
        public String getMatrixProperty() {
            return null;
        }

        @org.jboss.resteasy.annotations.jaxrs.MatrixParam
        public void setMatrixProperty(String p) {
        }

        public String getQueryProperty() {
            return null;
        }

        @org.jboss.resteasy.annotations.jaxrs.QueryParam
        public void setQueryProperty(String p) {
        }

        @org.jboss.resteasy.annotations.jaxrs.QueryParam
        public void queryProperty2(String p) {
        }

        // This annotation is not considered for processing
        @org.jboss.resteasy.annotations.jaxrs.CookieParam
        public String getUnusedField() {
            return null;
        }

        // This annotation is not considered for processing
        @Parameter(name = "unusedField2")
        public String getUnusedField2() {
            return null;
        }

        // This annotation is not considered for processing (too many arguments)
        @org.jboss.resteasy.annotations.jaxrs.QueryParam
        public void setQueryProperty(String p, String p2) {
        }
    }

    @Path("/char/sequence")
    static class CharSequenceArrayParamTestResource {
        static class EchoResult {
            // The 'implementation' specifies one less array dimensions since the type implies one dimension
            @Schema(type = SchemaType.ARRAY, implementation = CharSequence[][].class)
            CharSequence[][][] resultWithSchema;

            CharSequence[][][] resultNoSchema;

            EchoResult result(CharSequence[][][] result) {
                this.resultWithSchema = result;
                this.resultNoSchema = result;
                return this;
            }
        }

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public EchoResult echo(@QueryParam("val") CharSequence[][][] value) {
            return new EchoResult().result(value);
        }
    }

    @Path("/optional")
    static class OptionalParamTestResource {
        static class OptionalWrapper {
            Optional<String> value;
        }

        static class NestedBean {
            @NotNull
            Optional<String> title;
        }

        @Schema(name = "multipurpose-bean")
        static class Bean {
            @QueryParam("name6")
            Optional<String> name;

            @QueryParam("age6")
            OptionalDouble age;

            Optional<NestedBean> nested;
        }

        @GET
        @Path("/n1")
        @Produces(MediaType.TEXT_PLAIN)
        public String helloName(@QueryParam("name") Optional<String> name) {
            return "hello " + name.orElse("SmallRye!");
        }

        @GET
        @Path("/n2")
        @Produces(MediaType.TEXT_PLAIN)
        @Parameter(name = "name2", required = true)
        public String helloName2(@QueryParam("name2") Optional<String> name) {
            return "hello " + name.orElse("SmallRye!");
        }

        @GET
        @Path("/n3")
        @Produces(MediaType.TEXT_PLAIN)
        @Parameter(name = "name3", required = false)
        public Optional<String> helloName3(@QueryParam("name3") Optional<String> name) {
            return Optional.of("hello " + name.orElse("SmallRye!"));
        }

        @POST
        @Path("/n4")
        @Consumes(MediaType.TEXT_PLAIN)
        @Produces(MediaType.TEXT_PLAIN)
        @Parameter(name = "name4")
        public OptionalWrapper helloName4(@FormParam("name4") Optional<String> name) {
            OptionalWrapper r = new OptionalWrapper();
            r.value = Optional.of("hello " + name.orElse("SmallRye!"));
            return r;
        }

        @POST
        @Path("/n5")
        @Consumes(MediaType.TEXT_PLAIN)
        @Produces(MediaType.TEXT_PLAIN)
        public Optional<String> helloName5(Optional<String> name, @CookieParam("age5") OptionalLong age) {
            return Optional.of("hello " + name.orElse("SmallRye!") + ' ' + age.orElse(-1));
        }

        @SuppressWarnings("unused")
        @GET
        @Path("/n6")
        @Produces(MediaType.TEXT_PLAIN)
        public Optional<String> helloName6(@BeanParam Optional<Bean> bean) {
            return null;
        }

        @GET
        @Path("/n7/{name}")
        @Produces(MediaType.TEXT_PLAIN)
        public String helloName7(@PathParam("name") Optional<String> name) {
            return "hello " + name.orElse("SmallRye!");
        }

        @SuppressWarnings("unused")
        @POST
        @Path("/n8")
        @Consumes(MediaType.TEXT_PLAIN)
        @Produces(MediaType.TEXT_PLAIN)
        public Optional<String> helloName8(@RequestBody Optional<Bean> bean) {
            return null;
        }

        @GET
        @Path("/n9")
        @Produces(MediaType.TEXT_PLAIN)
        public String helloName9(@QueryParam("name9") @NotNull Optional<String> name) {
            return "hello " + name.orElse("SmallRye!");
        }
    }

    @Path("/template")
    static class PathParamTemplateRegexTestResource {
        @GET
        @Path("{id:\\d+}/{name: [A-Z]+    }/{  nickname :[a-zA-Z]+}/{age: [0-9]{1,3}}")
        @Produces(MediaType.TEXT_PLAIN)
        public String echo(@PathParam("id") Integer id, @PathParam("name") String name, @PathParam("nickname") String nickname,
                @PathParam("age") String age) {
            return String.valueOf(id) + ' ' + name + ' ' + nickname + ' ' + age;
        }
    }

    @Path("/segments")
    static class PathSegmentMatrixTestResource {
        @GET
        @Path("seg1")
        @Produces(MediaType.TEXT_PLAIN)
        @Parameter(name = "segments", description = "Test", style = ParameterStyle.MATRIX, in = ParameterIn.PATH)
        public String echo(@PathParam("segments") PathSegment segmentsMatrix) {
            return segmentsMatrix.getPath();
        }
    }

    @Path("/override")
    static class ParamNameOverrideTestResource {
        @HeaderParam("h1")
        @Parameter(name = "X-Header1", in = ParameterIn.HEADER, content = {})
        String h1;
        @CookieParam("c1")
        @Parameter(name = "Cookie1", in = ParameterIn.COOKIE, content = @Content(mediaType = MediaType.TEXT_PLAIN))
        String c1;
        @QueryParam("q1")
        @Parameter(name = "query1", in = ParameterIn.QUERY, content = {
                @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(description = "A JSON query parameter", type = SchemaType.OBJECT)),
                @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(description = "A plain text query parameter", type = SchemaType.STRING)),
        })
        String q1;

        @GET
        @Path("{p1}")
        @Produces(MediaType.TEXT_PLAIN)
        public String echo(
                @Parameter(name = "Path1", in = ParameterIn.PATH, style = ParameterStyle.SIMPLE, description = "The name 'Path1' will not be used instead of 'p1'") @PathParam("p1") String p1) {
            return p1;
        }
    }

    @Path("/common-target-method")
    static class CommonTargetMethodParameterResource {
        @DefaultValue("10")
        @QueryParam("limit")
        @Parameter(description = "Description of the limit query parameter")
        public void setLimit(int limit) {
        }

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String[] getRecords() {
            return null;
        }
    }

    @Path("/all/the/params/{id1}/{id2}")
    @SuppressWarnings("unused")
    static class RestEasyReactiveAllTheParamsTestResource {
        public RestEasyReactiveAllTheParamsTestResource(@RestPath("id1") int id1,
                @RestPath String id2) {
        }

        static class Bean {
            @RestMatrix
            @DefaultValue("BEAN1")
            String matrixF1;

            @RestMatrix("matrixF2")
            @DefaultValue("BEAN2")
            String matrixF2;

            @RestCookie
            @DefaultValue("COOKIE1")
            @Deprecated
            String cookieF1;
        }

        @Parameter(in = ParameterIn.PATH, style = ParameterStyle.MATRIX, name = "id2")
        @BeanParam
        private Bean param;

        @POST
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Produces(MediaType.APPLICATION_JSON)
        public CompletionStage<Widget> upd(@RestForm("f1") @DefaultValue("42") int f1,
                @RestForm @DefaultValue("f2-default") @NotNull String f2,
                @RestHeader("h1") @Deprecated int h1,
                @RestHeader("h2") String notH2) {
            return null;
        }

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Widget get(@RestQuery("q1") @Deprecated long q1,
                @RestQuery("q2") String notQ2) {
            return null;
        }
    }

    @Path("/path/{param1}")
    static class RestEasyReactivePathParamOmittedTestResource {

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Path("/params/{param2}")
        public Widget get1(@Min(100) int param1, @RestPath String param2) {
            return null;
        }

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Path("/params/{param3}")
        public Widget get2(@Extension(name = "custom-info", value = "value for param3") String param3, @Context Request param1,
                int paramOne) {
            return null;
        }
    }

    @Path("/greet")
    static class GreetResource {
        @Path("/{name}")
        @GET
        @Operation(summary = "Returns a personalized greeting")
        @APIResponse(description = "Simple JSON containing the greeting", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GreetingMessage.class)))
        @Produces(MediaType.APPLICATION_JSON)
        public JsonObject getMessage(
                @Parameter(description = "The greeting name") @PathParam("name") String name) {
            return null;
        }

        public static class GreetingMessage {
            String message;
        }
    }

    @OpenAPIDefinition(info = @Info(title = "title", version = "1"), components = @Components(parameters = {
            @Parameter(name = "queryParam1", in = ParameterIn.QUERY),
            @Parameter(name = "pathParam2", in = ParameterIn.PATH, description = "`pathParam2` with info in components") }))
    static class ParameterRefTestApplication extends Application {
    }

    @Path("/{pathParam1}/{pathParam2}")
    static class ParameterRefTestResource {
        @GET
        @Path("one")
        @Parameter(ref = "queryParam1")
        String exampleEndpoint1(@PathParam("pathParam1") String pathParam1,
                @PathParam("pathParam2") String pathParam2) {
            return null;
        }

        @GET
        @Path("/two")
        @Parameter(name = "pathParam1", style = ParameterStyle.SIMPLE)
        @Parameter(ref = "pathParam2")
        // `name` on `queryParam1` ref ignored
        @Parameter(ref = "queryParam1", name = "queryParamOne")
        @Parameter(in = ParameterIn.COOKIE, description = "Ignored: missing key attributes")
        @Parameter(in = ParameterIn.DEFAULT, description = "Ignored: missing key attributes")
        @Parameter(in = ParameterIn.HEADER, description = "Ignored: missing key attributes")
        @Parameter(in = ParameterIn.PATH, description = "Ignored: missing key attributes")
        String exampleEndpoint2(@PathParam("pathParam1") String pathParam1,
                @Parameter(hidden = true) @PathParam("pathParam2") String pathParam2) {
            return null;
        }
    }

    @Path("/enum-default-param")
    static class DefaultEnumTestResource {
        public enum MyEnum {
            CAT,
            DOG,
            BAR,
            FOO
        }

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String hello(@QueryParam("q0") String q0,
                @Parameter(required = true) @QueryParam(value = "q1") @Size(min = 3, max = 3) @DefaultValue("DOG") Optional<MyEnum> q1) {
            return "myEnum = " + q1;
        }
    }

    static class BaseGenericResource<T1, T2, T3> {
        protected static class GenericBean<G> {
            @QueryParam("g1")
            G g1;
        }

        @GET
        @Path("typevar")
        public T1 test(@QueryParam("q1") T2 q1) {
            return null;
        }

        @GET
        @Path("map")
        public Map<T2, T3> getMap(T1 filter) {
            return null;
        }
    }

    @Path("/integer-string")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    static class IntegerStringUUIDResource extends BaseGenericResource<Integer, String, UUID> {
        @POST
        @Path("save")
        public Integer update(Integer value, @BeanParam GenericBean<String> gbean) {
            return null;
        }
    }
}
