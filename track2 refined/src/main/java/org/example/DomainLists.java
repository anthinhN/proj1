package org.example;

import java.util.List;

public class DomainLists {
    public List<String> expiredDomains;
    public List<String> closeToExpiredDomains;
    public List<String> validDomains;
    public List<String> warningDomains;

    DomainLists(List<String> expiredDomains, List<String> closeToExpiredDomains, List<String> validDomains, List<String> warningDomains) {
        this.expiredDomains = expiredDomains;
        this.closeToExpiredDomains = closeToExpiredDomains;
        this.validDomains = validDomains;
        this.warningDomains = warningDomains;
    }
}
