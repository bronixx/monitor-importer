package jobtask.importer;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import javax.persistence.EntityManager;

@Slf4j
public class EntityConsumer implements Subscriber<Model> {
    
    @Inject
    private EntityManager manager;
    
    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription s) {
        subscription = s;
        s.request(1);
    }

    @Override
    public void onNext(Model model) {
        manager.getTransaction().begin();
        try {
            manager.persist(model);
            manager.getTransaction().commit();
        } catch (Exception e) {
            manager.getTransaction().rollback();
        }
        subscription.request(1);
    }

    @Override
    public void onError(Throwable t) {
        log.error("Failed to load a model object", t);
        subscription.cancel();
    }

    @Override
    public void onComplete() {
    }
}
