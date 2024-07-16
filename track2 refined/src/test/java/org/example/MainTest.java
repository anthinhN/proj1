package org.example;

import org.example.DomainLists;
import org.example.EmailSender;
import org.example.Main;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MainTest {

    private Main main;
    private EmailSender emailSenderMock;

    @BeforeEach
    void setUp() {
        main = new Main();
        emailSenderMock = mock(EmailSender.class);
    }

    @Test
    void testReadDomainsFromFile() throws IOException {
        // Given
        String testFilePath = "testDomains.txt";
        List<String> expectedDomains = Arrays.asList("https://example.com", "https://example.org");
        createTestFile(testFilePath, "https://example.com\nhttps://example.org\n");

        // When
        List<String> domains = main.readDomainsFromFile(testFilePath);

        // Then
        assertEquals(expectedDomains, domains);

        // Cleanup
        new File(testFilePath).delete();
    }

    @Test
    void testCheckSSLCertificatesWarning() throws Exception {
        // Given
        List<String> domains = Arrays.asList("https://example.com", "https://example.org");

        // Mocking SSL certificate details
        HttpsURLConnection mockConnection = mock(HttpsURLConnection.class);
        URL mockURL = mock(URL.class);
        Certificate[] mockCertificates = new Certificate[1];
        X509Certificate mockX509Certificate = mock(X509Certificate.class);
        mockCertificates[0] = mockX509Certificate;

        // Mock behavior
        when(mockURL.openConnection()).thenReturn(mockConnection);
        when(mockConnection.getServerCertificates()).thenReturn(mockCertificates);
        when(mockX509Certificate.getNotAfter()).thenReturn(java.util.Date.from(LocalDate.now().
                plusDays(30).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));

        // When
        Main spyMain = spy(main);
        doReturn(mockURL).when(spyMain).createURL(anyString());

        DomainLists domainLists = spyMain.checkSSLCertificates(domains);

        // Then
        assertTrue(domainLists.warningDomains.size() > 0);
        assertTrue(domainLists.validDomains.isEmpty());
        assertTrue(domainLists.expiredDomains.isEmpty());
        assertTrue(domainLists.closeToExpiredDomains.isEmpty());
    }

    //Test domain ClosetoExpired
    @Test
    void testCheckSSLCertificateClosetoExpired()throws Exception{
        //Given
        List<String> domains = Arrays.asList("https://example.com");

        //Mock SSL Certificate details
        HttpsURLConnection mockConnection = mock(HttpsURLConnection.class);
        URL mockURL = mock(URL.class);
        Certificate[] mockCertificates = new Certificate[1];
        X509Certificate mockX509Certificate = mock(X509Certificate.class);
        mockCertificates[0] = mockX509Certificate;

        // Mock behavior
        when(mockURL.openConnection()).thenReturn(mockConnection);
        when(mockConnection.getServerCertificates()).thenReturn(mockCertificates);
        when(mockX509Certificate.getNotAfter()).thenReturn(java.util.Date.from(LocalDate.now().
                plusDays(15).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));

        // When
        Main spyMain = spy(main);
        doReturn(mockURL).when(spyMain).createURL(anyString());

        DomainLists domainLists = spyMain.checkSSLCertificates(domains);

        // Then
        assertTrue(domainLists.closeToExpiredDomains.size() > 0);
        assertTrue(domainLists.validDomains.isEmpty());
        assertTrue(domainLists.expiredDomains.isEmpty());
        assertTrue(domainLists.warningDomains.isEmpty());
    }

    @Test
    void testCheckSSLCertificateExpired()throws Exception{
        //Given
        List<String> domains = Arrays.asList("https://example.com");

        //Mock SSL Certificate details
        HttpsURLConnection mockConnection = mock(HttpsURLConnection.class);
        URL mockURL = mock(URL.class);
        Certificate[] mockCertificates = new Certificate[1];
        X509Certificate mockX509Certificate = mock(X509Certificate.class);
        mockCertificates[0] = mockX509Certificate;

        // Mock behavior
        when(mockURL.openConnection()).thenReturn(mockConnection);
        when(mockConnection.getServerCertificates()).thenReturn(mockCertificates);
        when(mockX509Certificate.getNotAfter()).thenReturn(java.util.Date.from(LocalDate.now().
                minusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));

        // When
        Main spyMain = spy(main);
        doReturn(mockURL).when(spyMain).createURL(anyString());

        DomainLists domainLists = spyMain.checkSSLCertificates(domains);

        // Then
        assertTrue(domainLists.expiredDomains.size() > 0);
        assertTrue(domainLists.validDomains.isEmpty());
        assertTrue(domainLists.closeToExpiredDomains.isEmpty());
        assertTrue(domainLists.warningDomains.isEmpty());
    }

    @Test
    void testBuildEmail() {
        // Given
        List<String> expiredDomains = List.of("example.com Expired");
        List<String> warningDomains = List.of("example.org Expires in 20 days");
        List<String> closeExpiredDomains = List.of("example.net Expires in 10 days");

        String expectedEmailBody = "SSL Certificate Report: \n" +
                "Expired Domains:\n" +
                "example.com Expired\n\n" +
                "Warning: These domains are going to expire in less than 30 days: \n" +
                "example.org Expires in 20 days\n\n" +
                "Critical: These domains are going to expire in less than 15 days: \n" +
                "example.net Expires in 10 days\n\n";

        // When
        String emailBody = main.buildEmail(expiredDomains, warningDomains, closeExpiredDomains);

        // Then
        assertEquals(expectedEmailBody, emailBody);
    }

    private void createTestFile(String filePath, String content) throws IOException {
        FileWriter writer = new FileWriter(filePath);
        writer.write(content);
        writer.close();
    }
}
