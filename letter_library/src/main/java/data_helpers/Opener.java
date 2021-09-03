package data_helpers;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Opener {

    public static void openFile(File file) throws IOException {
        Desktop.getDesktop().open(file);
    }

}
