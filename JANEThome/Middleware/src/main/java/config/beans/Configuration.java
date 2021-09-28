package config.beans;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import config.interfaces.ConfigurationInterface;
import javax.ejb.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;

@Singleton
public class Configuration implements ConfigurationInterface {

    private final HashMap<String,HashMap<String,String>> conf = new HashMap<>();
    private static final String PATH_CONF = "src/main/resources/META-INF";

    public Configuration(){
        Gson gson = new Gson();

        try (Stream<Path> walk = Files.walk(Paths.get(PATH_CONF))) {
            List<Path> result = walk.filter(Files::isRegularFile).filter(f->f.getFileName().toString().endsWith(".conf"))
                    .collect(Collectors.toList());
            result.forEach(System.out::println);

            for( Path path : result ) {
                Path rootPath = path.toAbsolutePath();
                if (path != null)
                    conf.put(FilenameUtils.removeExtension(path.getFileName().toString()),
                            gson.fromJson(new String(Files.readAllBytes(rootPath)), new TypeToken<HashMap<String, String>>() {
                    }.getType()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getParameter(String conf_file, String parameter) {
        if( this.conf.containsKey(conf_file))
            return this.conf.get(conf_file).get(parameter);
        return null;
    }

    public HashMap<String,String> getConfiguration(String conf_file){
        if( this.conf.containsKey(conf_file))
            return this.conf.get(conf_file);
        return null;
    }

}
