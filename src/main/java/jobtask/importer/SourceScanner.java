package jobtask.importer;

import com.google.inject.Inject;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class SourceScanner {

    private final Path sourceDir;
    private final Path processedDir;
    private final Unmarshaller unmarshaller;
    
    @Inject
    @SneakyThrows
    public SourceScanner(Properties properties, JAXBContext context) {
        sourceDir = FileSystems.getDefault().getPath(properties.getProperty("source.dir"));
        processedDir = FileSystems.getDefault().getPath(properties.getProperty("processed.dir"));
        unmarshaller = context.createUnmarshaller();
    }
    
    public Flowable<Model> scan() {
        
        return findFiles().subscribeOn(Schedulers.io())
                .map(this::processFile)
                .onErrorReturn(throwable -> {
                    if (!(throwable instanceof UnmarshalException)) {
                        log.error("Failed to parse XML", throwable);
                    }
                    return null;
                });
    }
    
    private Flowable<Path> findFiles() {
        return Flowable.create(e -> {
            try {
                Deque<Path> files = Files.find(sourceDir, 3, 
                        (path, attrs) -> attrs.isRegularFile() && path.endsWith(".xml"))
                        .collect(Collectors.toCollection(LinkedList::new));
                while (!files.isEmpty() && !e.isCancelled()) {
                    if (e.requested() == 0) {
                        Thread.sleep(100);
                        continue;
                    }
                    long counter = e.requested();
                    while (counter > 0 && !e.isCancelled()) {
                        e.onNext(files.poll());
                    }
                }
                e.onComplete();
            } catch (IOException ex) {
                e.onError(ex);
            }
        }, BackpressureStrategy.BUFFER);
    }
    
    private Model processFile(Path path) throws JAXBException, IOException {
        Model model = readModel(path);
        moveFileToProcessed(path);
        return model;
    }
    
    private Model readModel(Path path) throws JAXBException {
        JAXBElement<Model> element = unmarshaller.unmarshal(new StreamSource(path.toFile()), Model.class);
        return element.getValue();
    }
    
    private void moveFileToProcessed(Path path) throws IOException {
        Files.move(path, processedDir);
    }
    
}
