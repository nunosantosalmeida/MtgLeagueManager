package org.example;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;


@QuarkusMain
public class Main {

    public static void main(String... args) {
        Quarkus.run(CommanderPreconLeague.class, args);
    }

    public static class CommanderPreconLeague implements QuarkusApplication {

        @Override
        public int run(String... args) {
            System.out.println("Starting up MTG League Manager");
            Quarkus.waitForExit();
            System.out.println("Shutting down MTG League Manager");
            return 0;
        }
    }
}