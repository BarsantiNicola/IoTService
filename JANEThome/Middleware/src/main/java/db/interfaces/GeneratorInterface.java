package db.interfaces;

import javax.ejb.Remote;

/**
 * Service to generate dID and lID tokens basing on a local storage file
 */
@Remote
public interface GeneratorInterface {

    /**
     * Generates a new unique deviceID
     * @return A new dID(Stringed unsigned integer)
     */
    String generateDID();

    /**
     * Generates a new unique locationID
     * @return A new lID(Stringed unsigned integer)
     */
    String generateLID();

}
