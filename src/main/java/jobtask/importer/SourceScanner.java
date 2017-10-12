package jobtask.importer;

import com.google.inject.Inject;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Validator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Collectors;

import static jobtask.importer.Application.*;

/**
 * Сканер исходного каталога. Делает следющее:
 * <ol>
 *     <li>Сканирует исходный каталог</li>
 *     <li>Считывает XML-файлы нужного формата</li>
 *     <li>Создаёт набор моделей из считанных файлов</li>
 *     <li>Переносит считанные файлы в другой каталог</li>
 * </ol>
 * Искодный каталог и каталог считанных файлов задаются в настройках build.properties перед сборкой.
 * <pre>{@code
 *  source.dir = /home/bronixx/workspace/monitor-importer/source;
 *  processed.dir = /home/bronixx/workspace/monitor-importer/processed}
 *  </pre>
 *  @author Bronislav Krivoruchko
 *  @since Java 1.8
 *  @see Flowable
 *  @see Model
 */
@Slf4j
public class SourceScanner {

    private final Path sourceDir;
    private final Path processedDir;
    private final Path rejectedDir;
    private final Unmarshaller unmarshaller;
    
    @Inject
    private Validator validator;
    
    @Inject
    @SneakyThrows
    public SourceScanner(Properties properties, JAXBContext context) {
        sourceDir = FileSystems.getDefault().getPath(properties.getProperty(SOURCE_DIR_PROPERTY));
        processedDir = FileSystems.getDefault().getPath(properties.getProperty(PROCESSED_DIR_PROPERTY));
        rejectedDir = FileSystems.getDefault().getPath(properties.getProperty(REJECTED_DIR_PROPERTY));
        unmarshaller = context.createUnmarshaller();
    }

    /**
     * @return возвращает поток {@link Flowable} со считанными объектами модели.
     * @see Model
     */
    public Flowable<Model> scan() {
        return findFiles()
                .subscribeOn(Schedulers.io())
                .map(this::readModel)
                .filter(model -> model != Model.EMPTY);
    }
    
    private Flowable<Path> findFiles() {
        FlowableOnSubscribe<Path> source = emitter -> {
            Deque<Path> files = Files.walk(sourceDir)
                    .filter(path -> Files.isRegularFile(path))
                    .collect(Collectors.toCollection(LinkedList::new));
            long counter = emitter.requested();
            while (!files.isEmpty() && !emitter.isCancelled()) {
                if (counter == 0) {
                    Thread.sleep(100);
                    counter = emitter.requested();
                    continue;
                }
                emitter.onNext(files.poll());
                counter--;
            }
            emitter.onComplete();
        };
        return Flowable.create(source, BackpressureStrategy.BUFFER);
    }
    
    private Model readModel(Path path) throws IOException {
        Model model;
        try {
            model = (Model) unmarshaller.unmarshal(path.toFile());
        } catch (JAXBException e) {
            model = Model.EMPTY;
            
        }
        if (model != Model.EMPTY && validator.validate(model).isEmpty()) {
            moveFileToProcessed(path);
        } else {
            moveFileToRejected(path);
            model = Model.EMPTY;
        }
        return model;
    }
    
    private void moveFileToProcessed(Path path) throws IOException {
        Files.move(path, processedDir.resolve(path.getFileName()));
    }
              
    private void moveFileToRejected(Path path) throws IOException {
        Files.move(path, rejectedDir.resolve(path.getFileName()));
    }
              
}
