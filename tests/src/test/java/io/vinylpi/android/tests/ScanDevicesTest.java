package io.vinylpi.android.tests;

import org.testng.annotations.Test;

import io.selendroid.client.SelendroidDriver;
import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.standalone.SelendroidConfiguration;
import io.selendroid.standalone.SelendroidLauncher;

import org.testng.annotations.BeforeClass;
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
		verifyScanStarted();
	}
	
	@BeforeClass (alwaysRun=true)
	public void beforeClass() {
		SelendroidConfiguration config = new SelendroidConfiguration();
		// Add the selendroid-test-app to the standalone server
		config.addSupportedApp("../android/app/build/outputs/apk/app-debug.apk");
		SelendroidLauncher selendroidServer = new SelendroidLauncher(config);
		selendroidServer.launchSelendroid();
		
		SelendroidCapabilities capabilities = new SelendroidCapabilities("io.vinylpi.app:0.1.0");
		capabilities.setEmulator(true);;
		capabilities.setModel("Nexus 5");
		
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
	}
	
	private void verifyScanStarted() {
		final WebElement scanButton = driver.findElement(By.id("fab_scan"));
		final WebElement scanAnimation = driver.findElement(By.id("ll_scan_status"));
		
		Assert.assertTrue(scanButton.isDisplayed());
		scanButton.click();
		Assert.assertTrue(!scanButton.isDisplayed());
		Assert.assertTrue(scanAnimation.isDisplayed());
		
		// Wait for device scan to finish by checking if the scan animation is hidden. 
		// Timeout after 60 seconds.
		(new WebDriverWait(driver, 60)).until(new ExpectedCondition<Boolean>() {
	          public Boolean apply(WebDriver d) {
	        	  WebElement scanMessage = driver.findElement(By.id("txt_scan_message"));
	              return !scanAnimation.isDisplayed() && scanButton.isDisplayed() && scanMessage.isDisplayed();
	          }
	      });
	}

	@AfterClass
	public void afterClass() {
		driver.quit();
	}
}
