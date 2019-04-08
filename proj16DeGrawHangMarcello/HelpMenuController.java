/*
 * File: HelpMenuController.java
 * Names: Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * Class: CS 361
 * Project 6/7/9
 * Date: October 26, 2018/ November 3, 2018/ November 20, 2018
 * ---------------------------
 * Edited By: Lucas DeGraw, Iris Lian
 * Project 11
 * Date: October 12, 2018
 */
package proj16DeGrawHangMarcello;

import javafx.scene.control.Alert;
import javafx.stage.Window;
import java.lang.reflect.Method;

/**
 * This is a controller file for help menu.
 * There is a method that opens URL with browser and two methods deal with two menuitems in help menu.
 * @author Danqing Zhao, Micheal Coyne
 * @author Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 */
public class HelpMenuController{
    /**
     * Handles the About button action.
     * Creates a dialog window that displays the authors' names.
     */
    public void handleAbout() {
        // create a information dialog window displaying the About text
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);

        // enable to close the window by clicking on the x on the top left corner of
        // the window
        Window window = dialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        // set the title and the content of the About window
        dialog.setTitle("About");
        dialog.setHeaderText(null);
        dialog.setContentText("Authors: \n" +
                "Project 11: Iris Lian, Lucas DeGraw\n" +
                "Project 6/7/9/10: Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou\n" +
                "Project 5: Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou\n" +
                "Project 4: Kevin Ahn, Matt Jones, Jackie Hang, Kevin Zhou\n" +
                "This application is a basic IDE with syntax highlighting.");

        // enable to resize the About window
        dialog.setResizable(true);
        dialog.showAndWait();
    }

    /**
     * the url link to the java tutorial
     */
    public void handleJavaTutorial(){
        openURL("https://docs.oracle.com/javase/tutorial/");
    }

    /**
     * try to open the given url from the system browser
     * @param url the url to be opened
     */
    private static void openURL(String url) {
        try {
            browse(url);
        } catch (Exception ignored) { }
    }

    /**
     * open the url via system browser
     * @param url the url to be opened
     * @throws Exception throws exception when there is no browser available
     */
    private static void browse(String url) throws Exception {
        String osName = System.getProperty("os.name", "");
        //if the user uses Mac OS
        if (osName.startsWith("Mac OS")) {
            Class fileMgr = Class.forName("com.apple.eio.FileManager");
            Method openURL = fileMgr.getDeclaredMethod("openURL");
            openURL.invoke( url);
        //if the user uses windows
        } else if (osName.startsWith("Windows")) {
            Runtime.getRuntime().exec(
                    "rundll32 url.dll,FileProtocolHandler " + url);
        } else {
            //a set of commonly used browsers in other OS
            String[] browsers = { "firefox", "opera", "mozilla"};
            String browser = null;
            for (int count = 0; count < browsers.length && browser == null; count++) {
                if (Runtime.getRuntime()
                        .exec(new String[] { "which", browsers[count] })
                        .waitFor() == 0)
                    browser = browsers[count];
                if (browser == null)
                    throw new Exception("Could not find web browser");
                else
                    Runtime.getRuntime().exec(new String[] { browser, url });
            }
        }
    }
}
