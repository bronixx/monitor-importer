package test.jobtask.importer;

import com.google.inject.Inject;
import jobtask.importer.EntityConsumer;
import jobtask.importer.Model;
import jobtask.importer.SourceScanner;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ConsumerTest extends TestBase {
    
    @Inject
    private SourceScanner scanner;

    @Inject
    private EntityConsumer consumer;
    
    @Inject
    private EntityManager entityManager;
    
    @Captor
    private ArgumentCaptor<Model> modelCaptor; 

    @Test
    @SuppressWarnings("unchecked")
    public void shouldConsumerPersistAllScannedObjects() throws ParseException {
        scanner.scan().blockingSubscribe(consumer);
        verify(entityManager, times(3)).persist(modelCaptor.capture());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        assertThat(modelCaptor.getAllValues(), containsInAnyOrder(
                allOf(hasProperty("content", equalTo("This is the fisrt entry. Have a nice day!")), 
                        hasProperty("creationDate", equalTo(dateFormat.parse("2017-10-11 12:00:00")))),
                allOf(hasProperty("content", equalTo("The second entry comes next. Have a nice evening!")),
                        hasProperty("creationDate", equalTo(dateFormat.parse("2017-10-11 18:00:00")))),
                allOf(hasProperty("content", equalTo("At last the last. Good night!")),
                        hasProperty("creationDate", equalTo(dateFormat.parse("2017-10-11 23:00:00"))))
        ));
    }

    @Override
    protected void configure() {
        super.configure();
        bind(EntityConsumer.class).toInstance(spy(new EntityConsumer()));
        bind(EntityManager.class).toInstance(mock(EntityManager.class));
    }
}
