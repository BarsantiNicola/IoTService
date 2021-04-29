package jms.interfaces;

import javax.ejb.Remote;

@Remote
public interface ArchiveInterface {

    void addDestination(String... destination);
    void removeDestination(String... destination);
    boolean authorizedDestination(String destination);

}
