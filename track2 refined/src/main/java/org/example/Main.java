package org.example;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        new Main().Test1();
    }

    public BufferedReader createBufferedReader(String filePath) throws IOException {
        return new BufferedReader(new FileReader(filePath));
    }

    public EmailSender createEmailSender() {
        return new EmailSender();
    }

    public URL createURL(String domain) throws IOException {
        return new URL(domain);
    }

    public void Test1() {
        List<String> emailRecipients = List.of(
                "", "",
                "", "huynhtl@vng.com.vn"//add more email as needed
        );
        String EMAIL_SUBJECT = "SSL Certificate Expired Notice";
        String domainFilePath = "src/domains.txt";

        try {
            List<String> domains = readDomainsFromFile(domainFilePath);
            DomainLists domainLists = checkSSLCertificates(domains);

            if (!domainLists.expiredDomains.isEmpty() || !domainLists.closeToExpiredDomains.isEmpty() || !domainLists.warningDomains.isEmpty()) {
                createEmailSender().sendEmail(emailRecipients, EMAIL_SUBJECT, buildEmail(domainLists.expiredDomains, domainLists.warningDomains, domainLists.closeToExpiredDomains));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> readDomainsFromFile(String filePath) throws IOException {
        List<String> domains = new ArrayList<>();
        try (BufferedReader br = createBufferedReader(filePath)) {
            String domain;
            while ((domain = br.readLine()) != null) {
                domains.add(domain.trim());
            }
        }
        return domains;
    }

    public DomainLists checkSSLCertificates(List<String> domains) {
        List<String> expiredDomains = new ArrayList<>();
        List<String> closeToExpiredDomains = new ArrayList<>();
        List<String> validDomains = new ArrayList<>();
        List<String> warningDomains = new ArrayList<>();

        for (String domain : domains) {
            try {
                checkSSLCertificate(domain, validDomains, warningDomains, closeToExpiredDomains, expiredDomains);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new DomainLists(expiredDomains, closeToExpiredDomains, validDomains, warningDomains);
    }

    public void checkSSLCertificate(String domain, List<String> notExpired, List<String> warningExpired,
                                    List<String> closeExpired, List<String> expired) throws IOException {
        try {
            String transformedDomain = domain.replace("https://", "");

            URL url = createURL(domain);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.connect();
            Certificate[] certificates = connection.getServerCertificates();
            X509Certificate sslCertificate = (X509Certificate) certificates[0];

            System.out.println(domain);
            System.out.println(sslCertificate.getNotAfter());

            LocalDate expiredDate = sslCertificate.getNotAfter().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();

            LocalDate now = LocalDate.now();

            long dayleft = ChronoUnit.DAYS.between(now, expiredDate);
            System.out.println(dayleft);

            String daysleft = Long.toString(dayleft);
            String Body = "Your SSL certificate will expire in " + daysleft;

            if (dayleft <= 0) {
                expired.add(transformedDomain + "   Expired");
            } else if (dayleft <= 15) {
                closeExpired.add(transformedDomain + "   Expires in " + daysleft + "days");
            } else if (dayleft <= 30) {
                warningExpired.add(transformedDomain + "   Expires in " + daysleft + " days");
            } else {
                notExpired.add(transformedDomain + " - Valid for " + daysleft + " days");
            }
        } catch (Exception e) {
            System.out.println("Failed to check SSL certificate for domain: " + domain);
            e.printStackTrace();
        }
    }

    public String buildEmail(List<String> expiredDomain, List<String> warningDomain,
                             List<String> closeexpiredDomain) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("SSL Certificate Report: \n");

        if (!expiredDomain.isEmpty()) {
            emailBody.append("Expired Domains:\n");
            for (String expiredDomains : expiredDomain) {
                emailBody.append(expiredDomains).append("\n");
            }
            emailBody.append("\n");
        }

        if (!warningDomain.isEmpty()) {
            emailBody.append("Warning: These domains are going to expire in less than 30 days: \n");
            for (String warningDomains : warningDomain) {
                emailBody.append(warningDomains).append("\n");
            }
            emailBody.append("\n");
        }

        if (!closeexpiredDomain.isEmpty()) {
            emailBody.append("Critical: These domains are going to expire in less than 15 days: \n");
            for (String closeexpiredDomains : closeexpiredDomain) {
                emailBody.append(closeexpiredDomains).append("\n");
            }
            emailBody.append("\n");
        }

        return emailBody.toString();
    }
}
