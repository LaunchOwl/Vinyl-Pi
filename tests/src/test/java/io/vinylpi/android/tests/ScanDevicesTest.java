package io.vinylpi.android.tests;

import org.testng.annotations.Test;

import io.selendroid.client.SelendroidDriver;
import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.standalone.SelendroidConfiguration;
import io.selendroid.standalone.SelendroidLauncher;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;

public class ScanDevicesTest {
	private WebDriver driver = null;

	@Test (groups = {"init", "scanDevices"})
	public void testUIElements() {
		verifyMainActivityElements();
	}
	
	@Test (groups = {"scanDevices"}, dependsOnMethods = {"testUIElements"})
	public void testScanning() {
		verifyScanCompleted();
	}
	
	@Test (groups = {"scanDevices"}, dependsOnMethods = {"testScanning"})
	@Parameters("deviceName")
	public void testResultSet(String deviceName) {
		verifyScanResultSet(deviceName);
	}
	
	@BeforeClass (alwaysRun=true)
	public void beforeClass() {
		SelendroidConfiguration config = new SelendroidConfiguration();
		// Add the selendroid-test-app to the standalone server
		config.addSupportedApp("../android/app/build/outputs/apk/app-debug.apk");
		SelendroidLauncher selendroidServer = new SelendroidLauncher(config);
		selendroidServer.launchSelendroid();
		
		SelendroidCapabilities capabilities = new SelendroidCapabilities("io.vinylpi.app:0.1.0");
		capabilities.setEmulator(false);
		//capabilities.setModel("Nexus 5");
		
		try {
			driver = new SelendroidDriver(capabilities);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void verifyMainActivityElements() {
		WebElement scanButton = driver.findElement(By.id("fab_scan"));
		Assert.assertTrue(scanButton.isDisplayed());

		WebElement scanMessage = driver.findElement(By.id("txt_scan_message"));
		Assert.assertTrue(scanMessage.isDisplayed());
		
		//final WebElement resultList = driver.findElement(By.id("lv_rpi_devices"));
		//Assert.assertTrue(resultList.isDisplayed());
	}
	
	//
	
	/**
	 * Test pressing the scan FAB button and make sure either the original screen displays
	 * or a result list displays.
	 */
	private void verifyScanCompleted() {
		final WebElement scanButton = driver.findElement(By.id("fab_scan"));
		final WebElement scanAnimation = driver.findElement(By.id("ll_scan_status"));
		//final WebElement resultList = driver.findElement(By.id("rv_rpi_devices"));
		
		Assert.assertTrue(scanButton.isDisplayed());
		scanButton.click();
		Assert.assertTrue(!scanButton.isDisplayed());
		Assert.assertTrue(scanAnimation.isDisplayed());
		
		// Wait for device scan to finish by checking if the scan animation is hidden. 
		// Timeout after 60 seconds.
		(new WebDriverWait(driver, 60)).until(new ExpectedCondition<Boolean>() {
	          public Boolean apply(WebDriver d) {
	        	  WebElement scanMessage = driver.findElement(By.id("txt_scan_message"));
	              return (!scanAnimation.isDisplayed() && scanButton.isDisplayed());
	          }
	      });
	}
	
	
	/**
	 * Test to ensure the result list has devices returned from the scan. If no devices were found, the
	 * result list shouldn't be visible or be empty.
	 */
	private void verifyScanResultSet(String deviceName) {
		final WebElement resultList = driver.findElement(By.id("rv_rpi_devices"));
		if (resultList.isDisplayed()) {
			//WebElement list = driver.findElement(By.xpath("//android.support.v7.widget.RecyclerView[@id='rv_rpi_devices']"));
			//List<WebElement> webElements = driver.findElements(By.xpath("//android.support.v7.widget.RecyclerView[@id='rv_rpi_devices'//android.widget.LinearLayout"));
			
			List<WebElement> webElements = resultList.findElements(By.className("android.widget.LinearLayout"));
			System.out.println("Count: " + webElements.size());
			Assert.assertTrue(webElements.size() > 0, "");
			
			WebElement listItem = webElements.get(0);
			WebElement deviceNameTextView = listItem.findElement(By.id("tv_device_name"));
			Assert.assertEquals(deviceNameTextView.getText(), deviceName);
		}
	}

	@AfterClass
	public void afterClass() {
		driver.quit();
	}
}
