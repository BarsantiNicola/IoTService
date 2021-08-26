package db.interfaces;

import iot.SmarthomeManager;

import javax.ejb.Remote;

@Remote
public interface DBinterface {

    boolean login( String email, String password );
    boolean addUser( String name, String surname, String email, String password );
    boolean emailPresent( String email );

    String getUserFirstName( String email );
    String getUserLastName( String email );

    String generateNewSmartID();
    SmarthomeManager getSmarthome( String email );
    boolean changePassword( String email, String new_password );

}
