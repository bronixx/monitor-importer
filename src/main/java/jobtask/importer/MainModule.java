package jobtask.importer;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.hibernate.validator.HibernateValidator;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Properties;

import static jobtask.importer.Application.PROPERTY_FILENAME;

class MainModule extends AbstractModule {

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
    
    @Provides
    @Singleton
    public Validator validatorProvider() {
        ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .buildValidatorFactory();
        return validatorFactory.getValidator();
    }
}
