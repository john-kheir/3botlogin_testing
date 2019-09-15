package appmain;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import static io.appium.java_client.touch.LongPressOptions.longPressOptions;
import static io.appium.java_client.touch.offset.ElementOption.element;
import static java.time.Duration.ofSeconds;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pageObjects.app.settingsPage;
import pageObjects.web.loginPage;
import utils.Email;
import pageObjects.app.pinCodePage;
import pageObjects.app.recoverAccountPage;
import pageObjects.app.homePage;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.lang.reflect.Method;
import utils.testsUtils;



public class recoverAccountTests extends Base{

    AppiumDriver<MobileElement> appDriver;
    AppiumDriver<MobileElement> webDriver;
    pinCodePage pinCodePage;
    homePage homePage;
    recoverAccountPage recoverAccountPage;
    loginPage loginPage;
    settingsPage settingsPage;
    testsUtils testsUtils;
    Email gmail;
    String email;

    @BeforeClass
    public void registerTestsSetup() throws MessagingException {
        email = (String) config.get("email");
        String email_password = (String) config.get("email_password");
        gmail = new Email(email, email_password, "smtp.gmail.com", Email.EmailFolder.INBOX);
    }

    @BeforeMethod
    public void setUp(Method method) throws IOException, MessagingException {
        logger.info("Running Test : " + method.getName());
        appiumService = startServer();
        appDriver = Capabilities(Boolean.TRUE, Boolean.FALSE);
        pinCodePage = new pinCodePage(appDriver);
        homePage = new homePage(appDriver);
        settingsPage = new settingsPage(appDriver);
        recoverAccountPage = new recoverAccountPage(appDriver);
        testsUtils = new testsUtils(appDriver, gmail);

    }

    @AfterMethod
    public void tearDown(Method method) {
        logger.info("End of Test : " + method.getName());
        appiumService.stop();
    }


    public void recoverAccount() throws Exception {

        logger.info("Open the app without a registered user");
        logger.info("Press 'Recover Account', should be directed to recover account page");
        homePage.recoverAccountButton.click();

        logger.info("Enter 3botName");
        recoverAccountPage.doubleNameField.click();
        Actions a = new Actions(appDriver);
        a.sendKeys((String) config.get("registeredUser"));
        a.perform();

        logger.info("Enter user's email");
        recoverAccountPage.emailField.click();
        a.sendKeys((String) config.get("email"));
        a.perform();

        logger.info("Copy, then paste user's phrase, should succeed");
        String phrase = (String) config.get("accountPhrase");
        ((AndroidDriver) appDriver).setClipboardText(phrase);
        TouchAction t = new TouchAction(appDriver);
        t.longPress(longPressOptions().withElement(element(recoverAccountPage.phraseField)).
                withDuration(ofSeconds(2))).release().perform();
        Assert.assertTrue(recoverAccountPage.pasteButton.isDisplayed());
        recoverAccountPage.pasteButton.click();

        logger.info("Press 'Recover Account', should be redirected to pin page");
        recoverAccountPage.recoverAccountButton.click();

        logger.info("Enter the pin and confirm it");
        int emails_num = gmail.getNumberOfMessages();
        testsUtils.enterAndConfirmPinCode();

        logger.info("Wait for the email to be received within 30 seconds");
        Boolean email_received = gmail.waitForNewMessage(emails_num);
        Assert.assertTrue(email_received, "Verification mail hasn't been received");

        logger.info("Open the email and click the verification link, email should be verified");
        Message lastEmailMessage = gmail.getLatestMessage();
        String verificationLink = gmail.getURl(gmail.getMessageContent(lastEmailMessage));
        webDriver = Capabilities(Boolean.FALSE, Boolean.TRUE);
        loginPage = new loginPage(webDriver);
        webDriver.get(verificationLink);
        waitTillTextBePresent(loginPage.emailValidatedText, "Email validated");

    }

    @Test
    public void test1_recoverAccountWithRightData() throws Exception {

        recoverAccount();

        logger.info("Switch to the app and make sure the email is verified");
        String appPackage = (String) config.get("appPackage");
        String appActivity = (String) config.get("appActivity");
        ((AndroidDriver) appDriver).startActivity(new Activity(appPackage,appActivity));
        homePage.settingsButton.click();
        appDriver.navigate().back();
        homePage.settingsButton.click();
        String text = settingsPage.settingViewElements.get(4).getText();
        String [] words = text.split("\n");
        String lastWord = words[words.length - 1];
        Assert.assertEquals(lastWord, "Verified");
    }
}