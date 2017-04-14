package tests;


import org.junit.*;



public class InitialRunTest extends AbstractTest {


    @Test
    public void canRunAppTest()
    {
        app.ChatScreen().continueForRootedDevice();
        app.ChatScreen().verifyPasswordRequestIsVisible();
    }

    @Test
    public void canCreatePasswordTest()
    {

         app.ChatScreen().continueForRootedDevice();
         app.ChatScreen().createPassword("password");
         app.ChatScreen().verifyPasswordIsSet();
    }


}