package io.vinylpi.rpi.pageobjects;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.Reporter;

public class StatPage {
	private WebDriver driver;

	public StatPage(String statPageUrl, String statPageTitle) {
		// Initiate web driver
		driver = new FirefoxDriver();

		// Open ffserver status page
		driver.get(statPageUrl);

		// Make sure this is the status page based on the title
		if (!driver.getTitle().equals(statPageTitle)) {
			// Close the browser
			Reporter.log("This is not the status page " + driver.getCurrentUrl());
			throw new IllegalStateException("Not the status page " + driver.getCurrentUrl());
		}
	}

	/**
	 * Make sure the stream is available
	 * 
	 * @param streamName
	 *            expected name of ffmpeg stream specified in the ffserver conf
	 *            file
	 * @param className
	 *            css class name for available stream paths
	 */
	public boolean streamAvailable(String streamName, String className) {
		boolean streamFound = false;
		
		// Find the text element by its class and check for the stream name
		List<WebElement> streams = driver.findElements(By.className(className));
		for (WebElement stream : streams) {
			if (stream.getText().toLowerCase().equals(streamName)) {
				streamFound = true;
				break;
			}
		}

		return streamFound;
	}
	
	public boolean connectionAvailable(String connectionName, String className) {
		boolean connectionFound = false;
		
		// Find the text element by its class and check for the connection name
		List<WebElement> streams = driver.findElements(By.className(className));
		for (WebElement stream : streams) {
			if (stream.getText().toLowerCase().equals(connectionName)) {
				connectionFound = true;
				break;
			}
		}
		
		return connectionFound;
	}

	/**
	 * Close the web driver
	 */
	public void closeDriver() {
		driver.quit();
	}
}
