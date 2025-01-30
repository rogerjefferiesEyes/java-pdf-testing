package tests;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.TestResultsSummary;
import com.applitools.eyes.fluent.BatchClose;
import com.applitools.eyes.images.Eyes;
import com.applitools.eyes.images.ImageRunner;
import com.applitools.eyes.images.Target;

public class PdfWebLinkTest {
	private WebDriver driver;
	private static ImageRunner runner = new ImageRunner();
	private static BatchInfo batch = new BatchInfo("PDF Tests");
    private Eyes eyes;
    private String pdfFilePath = "";
    private boolean isDeletePdfFileAfterTest = false;
	
	@Before
	public void setUp() {
        ChromeOptions options = new ChromeOptions();
		Map<String, Object> prefs = new HashMap<String, Object>();
		prefs.put("download.prompt_for_download", false);
		prefs.put("download.default_directory",System.getProperty("user.home")); 
		options.setExperimentalOption("prefs", prefs);
		options.addArguments("--test-type");
		options.addArguments("--disable-extensions");
		driver = new ChromeDriver(options);
		
		eyes = new Eyes(runner);
		batch.setNotifyOnCompletion(true);
        eyes.setBatch(batch);
	}

	@Test
	public void testPdfWebLink() throws MalformedURLException, IOException {
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    	driver.get("https://www.learningcontainer.com/download/sample-pdf-file-for-testing/");
    	
    	String pdfUrl = driver.findElement(By.xpath("//*[@id=\"post-1566\"]/div/div/div/div[2]/div/a")).getAttribute("data-downloadurl");
    	
    	System.out.println(pdfUrl);
    	
    	driver.get(pdfUrl);
    	
        // Set file to the downloaded
    	pdfFilePath = System.getProperty("user.home") + "/sample-pdf-file.pdf";
    	File file = new File(pdfFilePath);
        PDDocument doc = PDDocument.load(file); // will throw here if PDF malformed or empty file
        
        PDFRenderer pdfRenderer = new PDFRenderer(doc);
        int maxPage = doc.getNumberOfPages();
        System.out.println("Numpages: " + doc.getNumberOfPages());

        if (maxPage == 0)
            throw new IOException("Error reading PDF document");
        try {

            for (int pageNum = 1; pageNum <= maxPage; pageNum++) {
                BufferedImage bim = pdfRenderer.renderImage(pageNum - 1);
                if (!eyes.getIsOpen())
                    eyes.open("Pdf QA Sample", "Sample PDF File for Testing", new RectangleSize(bim.getWidth(), bim.getHeight()));

                eyes.check(String.format("Page-%s", pageNum), Target.image(bim));
                bim.getGraphics().dispose();
                bim.flush();
            }
            
            // End visual UI testing.
            // Close Eyes Test
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
        }
	}
	
	@After
	public void tearDown() throws IOException {
		if(isDeletePdfFileAfterTest) {
			Path filePath = Paths.get(pdfFilePath);
            Files.delete(filePath);
		}
		driver.quit();
	}
	
	@AfterClass
	public static void tearDownEyes() {
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
