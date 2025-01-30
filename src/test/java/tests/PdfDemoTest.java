package tests;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.MatchLevel;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.TestResultsSummary;
import com.applitools.eyes.fluent.BatchClose;
import com.applitools.eyes.images.Eyes;
import com.applitools.eyes.images.ImageRunner;
import com.applitools.eyes.images.Target;

public class PdfDemoTest {
    private static ImageRunner runner = new ImageRunner();
    private static BatchInfo batch = new BatchInfo("PDF Tests");
    private Eyes eyes;
    
    @BeforeClass
    public static void oneTimeSetUp() {
    	batch.setNotifyOnCompletion(true);
    }

    @Test
    public void test() throws IOException {
        Eyes eyes = new Eyes(runner);
        eyes.setBatch(batch);
        eyes.setMatchLevel(MatchLevel.STRICT); // Default match level for all tests

        // Set file to whatever you want to test. Next steps: parameterize?
        File file = new File("src/test/resources/INV12345.pdf");

        PDDocument doc = null;
        doc = PDDocument.load(file);
        PDFRenderer pdfRenderer = new PDFRenderer(doc);
        int maxPage = doc.getNumberOfPages();
        System.out.println("Numpages: " + doc.getNumberOfPages());

        if (maxPage == 0)
            throw new IOException("Error reading PDF document");
        try {

            for (int pageNum = 1; pageNum <= maxPage; pageNum++) {
                BufferedImage bim = pdfRenderer.renderImage(pageNum - 1);
                if (!eyes.getIsOpen())
                    eyes.open("Pdfs Java", "Test PDF", new RectangleSize(bim.getWidth(), bim.getHeight()));


                eyes.check(String.format("Page-%s", pageNum), Target.image(bim));
                bim.getGraphics().dispose();
                bim.flush();
            }

            // End visual UI testing; Close Eyes Test
            // Setting the shouldThrowException parameter to: true will
            // throw an exception when there are differences
            TestResults testResults = eyes.close(false);
            System.out.println(testResults.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (doc != null)
                    doc.close();
            } catch (Exception e) {
                //Do nothing
            }
            // If the test was aborted before eyes.close was called, ends the test as aborted.
            eyes.abortIfNotClosed();
        }
    }
    
    @AfterClass
    public static void oneTimeTearDown() {
        // Close the batch; which will send notifications
        BatchClose batchClose = new BatchClose();
        batchClose.setBatchId(Arrays.asList(new String[]{batch.getId()})).close();

        // Get the final test results, while closing the runner
        // Setting the shouldThrowException parameter to: true will
        // throw an exception when there are differences
        TestResultsSummary allTestResults = runner.getAllTestResults(false);

        // Print Runner Test Results
        System.out.println(allTestResults);
    }
}