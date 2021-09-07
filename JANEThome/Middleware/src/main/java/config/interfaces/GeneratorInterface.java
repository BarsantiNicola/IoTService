package config.interfaces;

import javax.ejb.Remote;

@Remote
public interface GeneratorInterface {

    String generateDID();
    String generateLID();

}
