package org.vinylpi.rpi.tests;

import org.testng.annotations.Test;
import org.vinylpi.rpi.pageobjects.StatPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class StatPageTest {
	private StatPage statPage;
	
	@Parameters({"stat-page-url", "stat-page-title" })
	@BeforeClass
	public void beforeClass(String statPageUrl, String statPageTitle) {
		statPage = new StatPage(statPageUrl, statPageTitle);
	}
	
	@Parameters({ "stream-name", "stream-class-name" })
	@Test
	public void testStreamAvailable(String streamName, String className) {
		Assert.assertTrue(statPage.streamAvailable(streamName, className), "Stream not available");
	}
	
	@Parameters({ "connection-name", "connection-class-name" })
	@Test
	public void testConnectionAvailable(String connectionName, String className) {
		Assert.assertTrue(statPage.connectionAvailable(connectionName, className), "Connection not available");
	}

	@AfterClass
	public void afterClass() {
		statPage.closeDriver();
	}

}
