package test.jobtask.importer;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import jobtask.importer.Model;
import jobtask.importer.SourceScanner;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.HibernateValidator;
import org.junit.After;
import org.junit.Before;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Properties;

import static jobtask.importer.Application.*;
import static org.mockito.Mockito.*;

@Slf4j
public abstract class TestBase extends AbstractModule {

    private final Properties properties;

    @Inject
    @Named(SOURCE_DIR_PROPERTY)
    Path scannerSourceDir;

    @Inject
    @Named(PROCESSED_DIR_PROPERTY)
    Path scannerProcessedDir;

    @Inject
    @Named(REJECTED_DIR_PROPERTY)
    Path scannerRejectedDir;

    @SneakyThrows
    TestBase() {
        properties = mock(Properties.class);
        when(properties.getProperty(SOURCE_DIR_PROPERTY)).thenReturn(Files.createTempDirectory("importer-source").toString());
        when(properties.getProperty(PROCESSED_DIR_PROPERTY)).thenReturn(Files.createTempDirectory("importer-processed").toString());
        when(properties.getProperty(REJECTED_DIR_PROPERTY)).thenReturn(Files.createTempDirectory("importer-rejected").toString());
        Injector injector = Guice.createInjector(this);
        injector.injectMembers(this);
    }

    @Before
    public void startUp() throws IOException {
        copyResource("/test-files/first-entry.xml", scannerSourceDir);
        copyResource("/test-files/second-one.xml", scannerSourceDir);
        copyResource("/test-files/third-last.xml", scannerSourceDir);
        copyResource("/test-files/unsuitable.xml", scannerSourceDir);
        copyResource("/test-files/sometext.txt", scannerSourceDir);
    }

    @After
    public void tearDown() throws IOException {
        Files.walk(scannerSourceDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        Files.walk(scannerProcessedDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        Files.walk(scannerRejectedDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Override
    protected void configure() {
        bind(Properties.class).toInstance(properties);
        bind(SourceScanner.class).in(Singleton.class);
        bind(Path.class).annotatedWith(Names.named(SOURCE_DIR_PROPERTY))
                .toInstance(Paths.get(properties.getProperty(SOURCE_DIR_PROPERTY)));
        bind(Path.class).annotatedWith(Names.named(PROCESSED_DIR_PROPERTY))
                .toInstance(Paths.get(properties.getProperty(PROCESSED_DIR_PROPERTY)));
        bind(Path.class).annotatedWith(Names.named(REJECTED_DIR_PROPERTY))
                .toInstance(Paths.get(properties.getProperty(REJECTED_DIR_PROPERTY)));
    }
    
    @Provides
    @Singleton
    public JAXBContext jaxbContextProvider() throws JAXBException {
        return JAXBContext.newInstance(Model.class);
    }

    @Provides
    @Singleton
    public Validator validatorProvider() {
        ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .buildValidatorFactory();
        return validatorFactory.getValidator();
    }

    private void copyResource(String resourceName, Path dest) throws IOException {
        Path resourceFileName = Paths.get(resourceName).getFileName();
        try (InputStream is = getClass().getResourceAsStream(resourceName)) {
            Files.copy(is, dest.resolve(resourceFileName), StandardCopyOption.REPLACE_EXISTING);
            log.info("The resource {} is copied to {}", resourceName, dest);
        }
    }
}
