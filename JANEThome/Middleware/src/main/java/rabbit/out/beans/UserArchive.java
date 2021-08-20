package rabbit.out.beans;

import rabbit.out.interfaces.ArchiveInterface;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import java.util.*;

@Singleton
public class UserArchive implements ArchiveInterface {

    private final Set<String> authorized = new HashSet<>();

    public UserArchive(){
        Set<String> registeredUsers = new HashSet<>();
        registeredUsers.add("barsantinicola9@hotmail.it");
        //  TODO Request to database to add all the registered users
        authorized.addAll(registeredUsers);
    }

    @Lock(LockType.WRITE)
    public void addDestination(String... destination){
        authorized.addAll(Arrays.asList(destination));
    }

    @Lock(LockType.WRITE)
    public void removeDestination(String... destination){
        authorized.removeAll(Arrays.asList(destination));
    }

    @Lock(LockType.READ)
    public boolean authorizedDestination(String destination){
        return authorized.contains(destination);
    }


}
