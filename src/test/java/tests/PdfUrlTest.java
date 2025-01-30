package tests;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.TestResultsSummary;
import com.applitools.eyes.fluent.BatchClose;
import com.applitools.eyes.images.Eyes;
import com.applitools.eyes.images.ImageRunner;
import com.applitools.eyes.images.Target;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.Arrays;

public class PdfUrlTest {

    @Test
    public void testPdfUrl() throws IOException, MalformedURLException {
    	ImageRunner runner = new ImageRunner();
    	
        BatchInfo batch = new BatchInfo("PDF Tests");
        batch.setNotifyOnCompletion(true);
        
        Eyes eyes = new Eyes(runner);
        eyes.setBatch(batch);

        // Set file to whatever PDF URL you want to test. Next steps: parameterize.
        InputStream is = new URL("https://github.com/TestAutomationU/automated-visual-testing/raw/master/resources/Invoice_PDFs/INV12345.pdf").openStream(); // will throw here if URL doesn't work
        PDDocument doc = PDDocument.load(is); // will throw here if PDF malformed or empty file
        
        PDFRenderer pdfRenderer = new PDFRenderer(doc);
        int maxPage = doc.getNumberOfPages();
        System.out.println("Numpages: " + doc.getNumberOfPages());

        if (maxPage == 0)
            throw new IOException("Error reading PDF document");
        try {

            for (int pageNum = 1; pageNum <= maxPage; pageNum++) {
                BufferedImage bim = pdfRenderer.renderImage(pageNum - 1);
                if (!eyes.getIsOpen())
                    eyes.open("Pdfs Java", "Test PDF from URL", new RectangleSize(bim.getWidth(), bim.getHeight()));

                eyes.check(String.format("Page-%s", pageNum), Target.image(bim));
                bim.getGraphics().dispose();
                bim.flush();
            }

            // End visual UI testing; Close Eyes Test
            // Setting the shouldThrowException parameter to: true will
            // throw an exception when there are differences
            TestResults testResults = eyes.close(false);
            System.out.println(testResults);
        } catch(Exception ex){
        	ex.printStackTrace();
        }
        finally {
            try {
                if (doc != null)
                    doc.close();
            } catch (Exception e) {
                //Do nothing
            }
            // If the test was aborted before eyes.close was called, ends the test as aborted.
            eyes.abortIfNotClosed();
            
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
}