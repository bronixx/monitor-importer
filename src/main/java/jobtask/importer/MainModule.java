package jobtask.importer;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Properties;

import static jobtask.importer.Application.PROPERTY_FILENAME;

public class MainModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SourceScanner.class).in(Singleton.class);
        bind(EntityConsumer.class).in(Singleton.class);
    }
    
    @Provides
    @Singleton
    public JAXBContext jaxbContextProvider() throws JAXBException {
        return JAXBContext.newInstance(Model.class);
    }
    
    @Provides
    @Singleton
    public Properties applicationPropertiesProvider() throws IOException {
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/" + PROPERTY_FILENAME));
        return props;
    }
}
