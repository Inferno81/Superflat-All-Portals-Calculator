package com.sophie;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.google.firebase.database.utilities.Pair;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

public class F3CReader implements NativeKeyListener {
    private static boolean f3Pressed = false;
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    public static void init() {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            System.err.println("Error registering F3+C detection!");
            System.exit(1);
        }

        //Initialize the keyboard listener
        GlobalScreen.addNativeKeyListener(new F3CReader());
    }

    private Pair<BPos, String> parseF3C() {
        try {
            //Compensate for delay between F3+C and clipboard load
            Thread.sleep(100);
            Transferable transferable = clipboard.getContents(null);
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                //Read clipboard
                String rawLocationData = (String) transferable.getTransferData(DataFlavor.stringFlavor); //EXAMPLE: /execute in minecraft:overworld run tp @s 35.05 73.31 -28.88 -215.55 42.01
                //Ensure that the program actually received an F3+C
                if (!rawLocationData.contains("/execute")) {
                    return null;
                }
                //Parse F3+C data
                String[] splitLocationData = rawLocationData.split(" ");
                BPos pos = new BPos(Math.round(Float.parseFloat(splitLocationData[6])), Math.round(Float.parseFloat(splitLocationData[8])));
                String dimension = splitLocationData[2];
                return new Pair<>(pos, dimension);
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());
        if (!f3Pressed && keyText.equals("F3")) {
            //Store F3 press
            f3Pressed = true;
        }
        if (f3Pressed && keyText.equals("C")) {
            Pair<BPos, String> locationData = parseF3C();
            //If any error occurred, ignore F3+C
            if (locationData != null) {
                UI.updateClosestStrongholds(locationData);
            }
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());
        if (keyText.equals("F3")) {
            //Remove stored F3 press
            f3Pressed = false;
        }
    }
}
