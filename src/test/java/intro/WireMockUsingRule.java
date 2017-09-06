package intro;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class WireMockUsingRule {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);

    @Test
    public void should_MatchingWithUrl() throws IOException {
        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/baeldung/.*")).
                willReturn(WireMock.aResponse().
                        withStatus(200).
                        withHeader("Content-Type", "application/json").
                        withBody("\"testing-library\": \"WireMock\"")));


        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet("http://localhost:8080/baeldung/wiremock");
        CloseableHttpResponse response = httpClient.execute(request);

        String responseString = convertHttpResponseToString(response);

        verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/baeldung/wiremock")));
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("application/json", response.getFirstHeader("Content-Type").getValue());
        assertEquals("\"testing-library\": \"WireMock\"", responseString);
    }


    @Test
    public void should_ContainBody() throws IOException {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/baeldung/wiremock")).
                withHeader("Content-Type", WireMock.equalTo("application/json")).
                withRequestBody(WireMock.containing("\"testing-library\": \"WireMock\"")).
                willReturn(aResponse().withStatus(200)));


        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("wiremock_intro.json");
        String responseAsString = convertInputStreamToString(resourceAsStream);
        StringEntity entity = new StringEntity(responseAsString);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost request = new HttpPost("http://localhost:8080/baeldung/wiremock");
        request.addHeader("Content-Type", "application/json");
        request.setEntity(entity);

        CloseableHttpResponse response = httpClient.execute(request);

        verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/baeldung/wiremock")).
                withHeader("Content-Type", equalTo("application/json")));
        assertEquals(200, response.getStatusLine().getStatusCode());

    }

    private String convertHttpResponseToString(HttpResponse httpResponse) throws IOException {
        InputStream inputStream = httpResponse.getEntity().getContent();
        return convertInputStreamToString(inputStream);
    }

    private String convertInputStreamToString(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream, "UTF-8");
        String string = scanner.useDelimiter("\\Z").next();
        scanner.close();
        return string;
    }
}
