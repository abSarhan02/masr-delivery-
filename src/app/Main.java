package app;

import data.DemoData;
import service.Platform;

public class Main {
    public static void main(String[] args) {
        Platform platform = new Platform();

        // Load some data so the program can be tested immediately
        DemoData.load(platform);

        ConsoleApp console = new ConsoleApp(platform);
        console.start();
    }
}
