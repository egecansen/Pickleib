package pickleib.utilities;

import context.ContextStore;
import properties.PropertyUtilities;
import utils.Printer;
import utils.reflection.ReflectionUtilities;

import java.util.Properties;

public class PropertyLoader {

    public static void load(){
        Properties pickleibProperties = PropertyUtilities.loadPropertyFile("pickleib.properties");
        boolean loaded = ContextStore.items().containsAll(pickleibProperties.keySet());

        if (!loaded){
            new Printer(PropertyLoader.class).important(".properties loaded by " + ReflectionUtilities.getPreviousMethodName());
            Properties properties = PropertyUtilities.getProperties();
            ContextStore.merge(pickleibProperties);
            if (!properties.isEmpty()){
                for (Object key:pickleibProperties.keySet())
                    properties.putIfAbsent(key, pickleibProperties.get(key));
            }
            else properties = pickleibProperties;

            PropertyUtilities.properties.putAll(properties);
        }
    }
}
