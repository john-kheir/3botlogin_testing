package appmain;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pageObjects.app.homePage;
import pageObjects.app.pinCodePage;
import pageObjects.app.settingsPage;
import utils.testsUtils;

import javax.mail.MessagingException;
import java.io.IOException;
import java.lang.reflect.Method;


public class settingsTests extends Base{

    AppiumDriver<MobileElement> appDriver;
    homePage homePage;
    settingsPage settingsPage;
    pinCodePage pinCodePage;
    testsUtils testsUtils;


    @BeforeClass
    public void settingsClassSetup() throws Exception {
        if (config.get("registeredUser").toString().isEmpty()){
            appiumService = startServer();
            appDriver = Capabilities(Boolean.TRUE, Boolean.FALSE);
            testsUtils = new testsUtils(appDriver);
            testsUtils.registeringUSerCommonSteps();
            testsUtils.verifyEmail();
            //saveconfig
            config.setProperty("registeredUser", testsUtils.getUserName());
            config.setProperty("accountPhrase", testsUtils.getPhrase());
            saveConfig();
            appiumService.stop();
        }
    }


    @BeforeMethod
    public void setUp(Method method) throws IOException, MessagingException {
        logger.info("Running Test : " + method.getName());
        appiumService = startServer();
        appDriver = Capabilities(Boolean.TRUE, Boolean.TRUE);
        homePage = new homePage(appDriver);
        settingsPage = new settingsPage(appDriver);
        pinCodePage = new pinCodePage(appDriver);
        testsUtils = new testsUtils(appDriver);
    }

    @Test
    public void test1_removeAccount(){

        logger.info("Go to settings page");
        homePage.settingsButton.click();

        logger.info("Press Advanced settings option then click on " +
                    "'remove account option', should succeed");
        settingsPage.advancedSettingsDropDown.click();
        settingsPage.removeAccountoption.click();

        logger.info("Press 'Cancel' button, should be redirected back to setting page");
        settingsPage.cancelButton.click();
        Assert.assertTrue(settingsPage.changePinCode.isDisplayed());

        logger.info("Click on 'remove account option', then Press 'Yes' button," +
                    " should be redirected back to home page");
        settingsPage.removeAccountoption.click();
        settingsPage.yesButton.click();
        Assert.assertTrue(homePage.registerNowButton.isDisplayed());
    }

    @Test
    public void test2_changePinCode(){

        logger.info("Go to settings page");
        homePage.settingsButton.click();

        logger.info("Press Advanced settings option then click on " +
                    "'remove account option', should succeed");

        logger.info("Press 'Change pincode', should be redirected to change pincode page");
        settingsPage.changePinCode.click();
        Assert.assertTrue(pinCodePage.enterOldPinText.isDisplayed());

        logger.info("Enter old wrong pin code, should still see message saying 'Enter old pincode'");
        for (int i=0; i<4; i++) {
            pinCodePage.eightButton.click();
        }
        pinCodePage.OKButton.click();
        Assert.assertTrue(pinCodePage.enterOldPinText.isDisplayed());

        logger.info("Enter old right pin code, should succeed");
        testsUtils.enterRightPinCode();
        Assert.assertTrue(pinCodePage.enterNewPinText.isDisplayed());

        logger.info("Enter new pincode, should succeed");
        for (int i=0; i<4; i++) {
            pinCodePage.eightButton.click();
        }
        pinCodePage.OKButton.click();
        Assert.assertTrue(pinCodePage.confirmNewPinText.isDisplayed());

        logger.info("Confirm new pincode by entering a wrong pincode. should fail");
        for (int i=0; i<4; i++) {
            pinCodePage.oneButton.click();
        }
        pinCodePage.OKButton.click();
        Assert.assertTrue(pinCodePage.confirmNewPinText.isDisplayed());

        logger.info("Confirm new pincode, should succeed");
        for (int i=0; i<4; i++) {
            pinCodePage.eightButton.click();
        }
        pinCodePage.OKButton.click();
        Assert.assertTrue(pinCodePage.pinChangedSuccessfullyText.isDisplayed());

        logger.info("Make sure pincode has actually changed by using the " +
                    "new pin, should succeed");
        appDriver.navigate().back();
        settingsPage.changePinCode.click();
        for (int i=0; i<4; i++) {
            pinCodePage.eightButton.click();
        }
        pinCodePage.OKButton.click();
        Assert.assertTrue(pinCodePage.enterNewPinText.isDisplayed());
        testsUtils.enterRightPinCode();
        testsUtils.confirmRightPin();

    }

    @Test
    public void test3_checkOnSettingsPageElements(){

        logger.info("Go to settings page");
        homePage.settingsButton.click();

        logger.info("Make sure user can see his username");
        String userName = settingsPage.settingViewElements.get(3).getText();
        Assert.assertEquals(userName,(String) config.get("registeredUser") + ".3bot");

        logger.info("Make sure user can see his email");
        String text = settingsPage.settingViewElements.get(4).getText();
        String [] words = text.split("\n");
        Assert.assertEquals(words[0], (String) config.get("email"));

        logger.info("Press 'Show Phrase' and provide wrong pin code, shouldn't show phrase");
        settingsPage.showPhrase.click();
        for (int i=0; i<4; i++) {
            pinCodePage.eightButton.click();
        }
        pinCodePage.OKButton.click();
        Assert.assertTrue(settingsPage.showPhrase.isDisplayed());

        logger.info("Press 'Show Phrase' and provide right pin code, phrase should be shown");
        settingsPage.showPhrase.click();
        testsUtils.enterRightPinCode();
        String phrase = settingsPage.phraseText.getText();
        Assert.assertEquals(phrase, (String) config.get("accountPhrase"));

        logger.info("Press 'Close' Button, should get back to settings page");
        settingsPage.closeButton.click();
        Assert.assertTrue(settingsPage.showPhrase.isDisplayed());

        logger.info("Make sure Version field exists");
        String versionText = settingsPage.settingViewElements.get(8).getText();
        String [] version = versionText.split(" ");
        Assert.assertEquals(version[0], "Version:");
    }


}