/*
 * File: ConsoleContextMenu.java
 * Names: Michael Li, Iris Lian, Zeb Keith-Hardy, Kevin Zhou
 * Class: CS 361
 * Project 6/7/9
 * Date: October 26, 2018/ November 3, 2018/ November 20, 2018
 */
package proj12DeGrawHangMarcello;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/**
 * This is the ConsoleContextMenu class. It includes implementations
 * needed to created a right-click popup menu in the console.
 * @author Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 */
public class ConsoleContextMenu extends ContextMenu {
    /**
     * This is the constructor of TabContextMenu which sets up the menu
     * @param controller the master controller
     */
    public ConsoleContextMenu(MasterController controller){
        super();
        MenuItem ClearMenuItem =new MenuItem();
        ClearMenuItem.setText("Clear");
        ClearMenuItem.setOnAction(event -> controller.handleClearConsole());
        this.getItems().addAll(ClearMenuItem);
    }
}
