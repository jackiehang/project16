/*
 * File: TabContextMenu.java
 * Names: Michael Li, Iris Lian, Zeb Keith-Hardy, Kevin Zhou
 * Class: CS 361
 * Project 6/7/9
 * Date: October 26, 2018/ November 3, 2018/ November 20, 2018
 */
package proj16DeGrawHangMarcello;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/**
 * This class contains the setup for the context menu in the tabs in the IDE.
 * The TabContextMenu has three menu items - Close, CloseAll, New.
 *
 * @author Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 */
public class TabContextMenu extends ContextMenu {
    /**
     * This is the constructor of TabContextMenu which sets up the menu
     * @param controller the master controller
     */
    public TabContextMenu(MasterController controller){
        super();
        MenuItem CloseMenuItem =new MenuItem();
        MenuItem CloseAllMenuItem = new MenuItem();
        MenuItem NewMenuItem= new MenuItem();

        CloseMenuItem.setText("Close");
        CloseAllMenuItem.setText("Close All");
        NewMenuItem.setText("New Tab");

        CloseMenuItem.setOnAction(event ->controller.handleClose(event));
        CloseAllMenuItem.setOnAction(event -> controller.handleCloseAll(event));
        NewMenuItem.setOnAction(event -> controller.handleNew());
        this.getItems().addAll(CloseMenuItem, CloseAllMenuItem, NewMenuItem);

    }
}
