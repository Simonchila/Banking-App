package GUIs;

import DB_OBJs.User;

import javax.swing.*;

public abstract class BaseFrame extends JFrame {
    protected User user;

    public BaseFrame(String title, User user){
        // initialize user
        this.user = user;

        intialize(title);
    }

    public BaseFrame(String title){
        intialize(title);
    }

    public void intialize(String title){
        // instantiate jframe properties and add a title to the bar
        setTitle(title);

        // set size (in pixels)
        setSize(420, 600);

        // terminate the program when the gui is closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // set layout to null to have absolute layout which allows us to manually
        // specify the size and position of each gui component
        setLayout(null);

        // prevent gui from being resized
        setResizable(false);

        // launch the gui in the center of the screen
        setLocationRelativeTo(null);

        // call on the the subsclass'  addGuiComponent()
        addGuiComponents();
    }

    // this method will need to be defined to by the subclass
    // when this being inherited from
    protected abstract void addGuiComponents();

}
