package intro;


import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;

public class WireMockStubbingTest {


    @Test
    public void shouldReturnExpectedString() throws IOException {
        WireMockServer wireMockServer = new WireMockServer();
        wireMockServer.start();

        WireMock.configureFor("localhost", 8080);
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/baeldung")).
                willReturn(WireMock.aResponse().withBody("Welcome to Baeldung!!")));


        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet request = new HttpGet("http://localhost:8080/baeldung");
        CloseableHttpResponse response = httpClient.execute(request);
        String responseToString = convertResponseToString(response);

        verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/baeldung")));
        assertEquals("Welcome to Baeldung!!", responseToString);

        wireMockServer.stop();
    }

    private static String convertResponseToString(HttpResponse response) throws IOException {
        InputStream responseStream = response.getEntity().getContent();
        Scanner scanner = new Scanner(responseStream, "UTF-8");
        String responseString = scanner.useDelimiter("\\Z").next();
        scanner.close();
        return responseString;
    }
}
