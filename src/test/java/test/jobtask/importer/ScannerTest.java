package test.jobtask.importer;

import com.google.inject.Inject;
import jobtask.importer.Model;
import jobtask.importer.SourceScanner;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Slf4j
public class ScannerTest extends TestBase {
    
    @Inject
    private SourceScanner scanner;
    
    @Test
    @SneakyThrows
    public void shouldScannerReturnValidModels() {
        List<Model> scannedModels = scanner.scan().collect(ArrayList<Model>::new, ArrayList::add).blockingGet();
        assertThat("Unexpected number of the models have been scanned", scannedModels.size(), is(3));
    }

    @Test
    @SneakyThrows
    public void shouldScannerMoveAllFilesToProcessedAndRejectedFolders() {
        scanner.scan().blockingLast();
        
        long filesLeft = Files.walk(scannerSourceDir)
                .filter(Files::isRegularFile).count();
        assertThat("Unexpected number of files not accepted by the scanner", filesLeft, is(0L));

        long filesProcessed = Files.walk(scannerProcessedDir)
                .filter(Files::isRegularFile).count();
        assertThat("Unexpected number of files moved to the processed files' folder", filesProcessed, is(3L));

        long filesRejected = Files.walk(scannerRejectedDir)
                .filter(Files::isRegularFile).count();
        assertThat("Unexpected number of files moved to the rejected files' folder", filesRejected, is(1L));
    }
}
