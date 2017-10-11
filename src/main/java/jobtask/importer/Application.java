package jobtask.importer;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.reactivex.Scheduler;
import io.reactivex.Scheduler.Worker;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

public class Application {
    
    public static final String PROPERTY_FILENAME = "config.properties";

    @Inject
    private SourceScanner scanner;
    
    @Inject
    private EntityConsumer consumer;
    
    private Scheduler scheduler = Schedulers.newThread();
    
    public static void main(String[] args) {
        Application app = new Application();
        Injector injector = Guice.createInjector(new MainModule());
        injector.injectMembers(app);
        app.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                app.done();
            }
        });
    }
    
    public void start() {
        Worker worker = scheduler.createWorker();
        worker.schedulePeriodically(() -> {
            scanner.scan().subscribe(consumer);
        }, 0, 10, TimeUnit.SECONDS);
    }
    
    public void done() {
        scheduler.shutdown();
    }
}
