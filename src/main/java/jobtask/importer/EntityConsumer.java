package jobtask.importer;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
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
    @Transactional
    public void onNext(Model model) {
        manager.persist(model);
        subscription.request(1);
    }

    @Override
    public void onError(Throwable t) {
        log.error("Faile to process a model object");
    }

    @Override
    public void onComplete() {
        manager.close();
    }
}
