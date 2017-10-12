package jobtask.importer;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;
import io.reactivex.Scheduler;
import io.reactivex.Scheduler.Worker;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

public class Application {
    
    public static final String PROPERTY_FILENAME = "config.properties";
    public static final String SOURCE_DIR_PROPERTY = "source.dir";
    public static final String PROCESSED_DIR_PROPERTY = "processed.dir";
    public static final String REJECTED_DIR_PROPERTY = "rejected.dir";

    @Inject
    private SourceScanner scanner;
    
    @Inject
    private EntityConsumer consumer;
    
    private PersistService persistService;
    
    private final Scheduler scheduler = Schedulers.newThread();
    
    public static void main(String[] args) {
        Application app = new Application();
        Injector injector = Guice.createInjector(new MainModule(), new JpaPersistModule("importer-unit"));
        app.persistService = injector.getInstance(PersistService.class);
        app.persistService.start();
        injector.injectMembers(app);
        app.start();
        Runtime.getRuntime().addShutdownHook(new Thread(app::done));
    }
    
    private void start() {
        persistService.start();
        Worker worker = scheduler.createWorker();
        worker.schedulePeriodically(() -> scanner.scan().subscribe(consumer), 0, 10, TimeUnit.SECONDS);
    }
    
    private void done() {
        scheduler.shutdown();
        persistService.stop();
    }
}
