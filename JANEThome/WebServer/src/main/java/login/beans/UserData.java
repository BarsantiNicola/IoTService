package login.beans;

import login.interfaces.IUserInfoStorage;

import javax.ejb.Stateful;
import javax.inject.Named;

//////////////////////////////////////////////////[ UserLogin ]//////////////////////////////////////////////////////
//                                                                                                                 //
//  class designed for user information cache. It is used by the webpage to print the user information             //
//                                                                                                                 //
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Class designed as a data node for user information. The class is stored inside the users' sessions to made them
 * available to the service webPage constructions
 */
@Stateful
@Named("userInfo")
public class UserData implements IUserInfoStorage {

    private String name = null;
    private String surname = null;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public void setParameters( String name, String surname ) {
        this.name = name;
        this.surname = surname;
    }
}
