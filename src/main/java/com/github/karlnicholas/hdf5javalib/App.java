package com.github.karlnicholas.hdf5javalib;

import java.io.File;
import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        try {
            File hdfFile = new File(App.class.getResource("/ExportedNodeShips.h5").getFile());
            HdfReader reader = new HdfReader(hdfFile);
            reader.readFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
