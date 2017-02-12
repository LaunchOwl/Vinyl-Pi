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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;

public class ScanDevicesTest {
	private WebDriver driver = null;

	@Test(groups = { "init", "scanDevices" })
	public void testUIElements() {
		verifyMainActivityElements();
	}

	@Test(groups = { "scanDevices" }, dependsOnMethods = { "testUIElements" })
	public void testScanning() {
		verifyScanCompleted();
	}

	@Test(groups = { "scanDevices" }, dependsOnMethods = { "testScanning" })
	public void testResultSet() {
		verifyScanResultSet();
	}

	@Test(groups = { "scanDevices" }, dependsOnMethods = { "testResultSet" })
	@Parameters({ "deviceName", "connectionCount", "maxConnectionCount" })
	public void testResultSetUIElements(String deviceName, String connectionCount, String maxConnectionCount) {
		verifyScanResultSetUIElements(deviceName, connectionCount, maxConnectionCount);
	}

	@Test(groups = { "scanDevices" }, dependsOnMethods = { "testResultSetUIElements" })
	public void testOpenDeviceActivity() {
		verifyDeviceActivityOpened();
	}

	@Test(groups = { "scanDevices" }, dependsOnMethods = { "testOpenDeviceActivity" })
	@Parameters({ "deviceName", "connectionCount", "maxConnectionCount" })
	public void testDeviceActivityUIElements(String deviceName, String connectionCount, String maxConnectionCount) {
		verifyDeviceActivityUIElements(deviceName, connectionCount, maxConnectionCount);
	}

	@Test(groups = { "scanDevices", "playback" }, dependsOnMethods = { "testDeviceActivityUIElements" })
	@Parameters({ "postConnectionCount" })
	public void testPlayback(String connectionCount) {
		verifyPlayback(connectionCount);
	}
	
	@Test(groups = { "scanDevices", "playback" }, dependsOnMethods = { "testPlayback" })
	@Parameters({ "connectionCount" })
	public void testPause(String connectionCount) {
		verifyPause(connectionCount);
	}

	@BeforeClass(alwaysRun = true)
	public void beforeClass() {
		SelendroidConfiguration config = new SelendroidConfiguration();
		// Add the selendroid-test-app to the standalone server
		config.addSupportedApp("../android/app/build/outputs/apk/app-debug.apk");
		SelendroidLauncher selendroidServer = new SelendroidLauncher(config);
		selendroidServer.launchSelendroid();

		SelendroidCapabilities capabilities = new SelendroidCapabilities("io.vinylpi.app:0.1.0");
		capabilities.setEmulator(false);
		// capabilities.setModel("Nexus 5");

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

		// WebElement scanMessage =
		// driver.findElement(By.id("txt_scan_message"));
		// Assert.assertTrue(scanMessage.isDisplayed());

		WebElement vinylImage = driver.findElement(By.id("iv_vinyl_background"));
		Assert.assertTrue(vinylImage.isDisplayed());

		// final WebElement resultList =
		// driver.findElement(By.id("lv_rpi_devices"));
		// Assert.assertTrue(resultList.isDisplayed());
	}

	//

	/**
	 * Test pressing the scan FAB button and make sure either the original
	 * screen displays or a result list displays.
	 */
	private void verifyScanCompleted() {
		final WebElement scanButton = driver.findElement(By.id("fab_scan"));
		final WebElement scanAnimation = driver.findElement(By.id("ll_scan_status"));
		// final WebElement resultList =
		// driver.findElement(By.id("rv_rpi_devices"));

		Assert.assertTrue(scanButton.isDisplayed());
		scanButton.click();
		Assert.assertTrue(!scanButton.isDisplayed());
		Assert.assertTrue(scanAnimation.isDisplayed());

		// Wait for device scan to finish by checking if the scan animation is
		// hidden.
		// Timeout after 60 seconds.
		(new WebDriverWait(driver, 60)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return (!scanAnimation.isDisplayed() && scanButton.isDisplayed());
			}
		});
	}

	/**
	 * Ensure the result list has devices returned from the scan. If no devices
	 * were found, the result list shouldn't be visible or be empty.
	 */
	private void verifyScanResultSet() {
		(new WebDriverWait(driver, 20)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {

				final WebElement resultList = driver.findElement(By.id("rv_rpi_devices"));
				if (resultList.isDisplayed()) {
					// WebElement list =
					// driver.findElement(By.xpath("//android.support.v7.widget.RecyclerView[@id='rv_rpi_devices']"));
					// List<WebElement> webElements =
					// driver.findElements(By.xpath("//android.support.v7.widget.RecyclerView[@id='rv_rpi_devices'//android.widget.LinearLayout"));

					List<WebElement> webElements = resultList.findElements(By.className("android.widget.LinearLayout"));
					System.out.println("Count: " + webElements.size());
					Assert.assertTrue(webElements.size() > 0, "");
				} else {
					WebElement vinylImage = driver.findElement(By.id("iv_vinyl_background"));
					Assert.assertTrue(vinylImage.isDisplayed());
				}
				return true;

			}
		});

	}

	/**
	 * Ensure the list item in the result set has expected UI elements.
	 */
	private void verifyScanResultSetUIElements(String deviceName, String connectionCount, String maxConnectionCount) {
		final WebElement resultList = driver.findElement(By.id("rv_rpi_devices"));
		if (resultList.isDisplayed()) {
			// WebElement list =
			// driver.findElement(By.xpath("//android.support.v7.widget.RecyclerView[@id='rv_rpi_devices']"));
			// List<WebElement> webElements =
			// driver.findElements(By.xpath("//android.support.v7.widget.RecyclerView[@id='rv_rpi_devices'//android.widget.LinearLayout"));

			List<WebElement> webElements = resultList.findElements(By.className("android.widget.LinearLayout"));
			System.out.println("Count: " + webElements.size());
			Assert.assertTrue(webElements.size() > 0, "");

			WebElement listItem = webElements.get(0);
			WebElement deviceNameTextView = listItem.findElement(By.id("tv_device_name"));
			Assert.assertEquals(deviceNameTextView.getText(), deviceName);

			WebElement connectionsCountTextView = listItem.findElement(By.id("tv_device_count"));
			Assert.assertEquals(connectionsCountTextView.getText(), connectionCount);

			WebElement connectionsMaxTextView = listItem.findElement(By.id("tv_device_max"));
			Assert.assertEquals(connectionsMaxTextView.getText(), maxConnectionCount);

			WebElement vinylIconImageView = listItem.findElement(By.id("iv_vinyl_icon"));
			WebElement groupIconImageView = listItem.findElement(By.id("iv_group_icon"));
		}
	}

	/**
	 * Ensure the activity for playback opens when item in result set is tapped.
	 */
	private void verifyDeviceActivityOpened() {
		final WebElement resultList = driver.findElement(By.id("rv_rpi_devices"));
		if (resultList.isDisplayed()) {
			// WebElement list =
			// driver.findElement(By.xpath("//android.support.v7.widget.RecyclerView[@id='rv_rpi_devices']"));
			// List<WebElement> webElements =
			// driver.findElements(By.xpath("//android.support.v7.widget.RecyclerView[@id='rv_rpi_devices'//android.widget.LinearLayout"));

			List<WebElement> webElements = resultList.findElements(By.className("android.widget.LinearLayout"));
			System.out.println("Count: " + webElements.size());
			Assert.assertTrue(webElements.size() > 0, "");

			WebElement listItem = webElements.get(0);
			listItem.click();

			(new WebDriverWait(driver, 2)).until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver d) {
					WebElement deviceActivityLayout = driver.findElement(By.id("rv_device"));
					return (deviceActivityLayout.isDisplayed());
				}
			});
		}
	}

	/**
	 * Ensure UI elements display in activity used for playback.
	 */
	private void verifyDeviceActivityUIElements(String deviceName, String connectionCount, String maxConnectionCount) {
		driver.findElement(By.id("iv_vinyl_icon"));
		driver.findElement(By.id("iv_play"));

		WebElement connectionsCountTextView = driver.findElement(By.id("tv_device_count"));
		Assert.assertEquals(connectionsCountTextView.getText(), connectionCount);

		WebElement connectionsMaxTextView = driver.findElement(By.id("tv_device_max"));
		Assert.assertEquals(connectionsMaxTextView.getText(), maxConnectionCount);
	}

	/**
	 * Ensure playback begins when the play button is pressed and expected UI
	 * elements display during/after connection is attempted.
	 */
	private void verifyPlayback(final String connectionCount) {
		WebElement playButton = driver.findElement(By.id("iv_play"));
		playButton.click();

		driver.findElement(By.id("pb_connecting"));
		new WebDriverWait(driver, 30)
				.until(ExpectedConditions.visibilityOf(driver.findElement(By.id("iv_pause"))));
		
		
		(new WebDriverWait(driver, 5)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				WebElement connectionsCountTextView = driver.findElement(By.id("tv_device_count"));
				return (connectionsCountTextView.getText().equals(connectionCount));		
			}
		});
		;
	}
	
	private void verifyPause(final String connectionCount) {
		WebElement pauseButton = driver.findElement(By.id("iv_pause"));
		pauseButton.click();
		Assert.assertTrue(!pauseButton.isDisplayed());
		
		WebElement playButton = driver.findElement(By.id("iv_play"));
		Assert.assertTrue(playButton.isDisplayed());
		
		(new WebDriverWait(driver, 5)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				WebElement connectionsCountTextView = driver.findElement(By.id("tv_device_count"));
				return (connectionsCountTextView.getText().equals(connectionCount));		
			}
		});
		;
	}

	@AfterClass
	public void afterClass() {
		driver.quit();
	}
}
