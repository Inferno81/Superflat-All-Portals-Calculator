package com.sophie;

import com.google.firebase.database.utilities.Pair;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseWheelListener;
import java.util.List;
import java.util.Objects;

import static com.sophie.Strongholds.updateCoordsList;

public class UI extends JFrame {
    private static JTable closestStrongholdsTable;
    public static final JLabel strongholdsVisitedLabel = new JLabel("Strongholds visited: 0/128 (0.0%)");

    private static final JLabel joinCodeLabel = new JLabel();
    private static final JButton copyJoinCodeButton = new JButton("📋");
    private static final JLabel copiedLabel = new JLabel("️✔️");

    private static final JButton createButton = new JButton("Create Room");
    private static final JButton joinButton = new JButton("Join Room");

    private static boolean inCoopMode = false;

    public UI() {
        //Initialize panels
        JPanel mainPanel = new JPanel(null);
        mainPanel.setBounds(0, 0, 500, 200);
        JPanel closestStrongholdsPanel = new JPanel(null);
        closestStrongholdsPanel.setBounds(10, 10, 480, 135);
        JPanel coopPanel = new JPanel(null);
        coopPanel.setBounds(10, 155, 480, 45);

        //Initialize closest strongholds table with initial menu data
        String[] colNames = { "Location", "Distance", "Nether", "Angle" };
        String[][] data = {
                { "Waiting for F3+C...", "", "", "" },
                { "Waiting for F3+C...", "", "", "" },
                { "Waiting for F3+C...", "", "", "" }
        };
        closestStrongholdsTable = new JTable(data, colNames);
        closestStrongholdsTable.setRowHeight(25);
        //Center text
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < 4; i++) {
            closestStrongholdsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        closestStrongholdsTable.getTableHeader().setBackground(new Color(64, 67, 70));
        closestStrongholdsTable.getTableHeader().setEnabled(false);
        closestStrongholdsTable.setShowGrid(true);
        closestStrongholdsTable.setEnabled(false);

        //Initialize scroll pane for storing table with no scrollbar
        JScrollPane sp = new JScrollPane(closestStrongholdsTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //Disable scrolling
        for (MouseWheelListener listener : sp.getMouseWheelListeners()) {
            sp.removeMouseWheelListener(listener);
        }
        sp.setBorder(new LineBorder(new Color(58, 61, 65)));
        sp.setEnabled(false);
        sp.setBounds(0, 0, 480, 100);

        strongholdsVisitedLabel.setFont(strongholdsVisitedLabel.getFont().deriveFont(11f));
        strongholdsVisitedLabel.setBounds(5, 105, 200, 20);

        closestStrongholdsPanel.add(sp);
        closestStrongholdsPanel.add(strongholdsVisitedLabel);


        //Join code and co-op UI initialization
        joinCodeLabel.setBounds(5, 0, 200, 20);

        copyJoinCodeButton.setFocusPainted(false);
        copyJoinCodeButton.setVisible(false);
        copyJoinCodeButton.addActionListener(e -> copyJoinCode());

        copyJoinCodeButton.setBounds(115, 0, 20, 20);

        copiedLabel.setForeground(Color.GREEN);
        copiedLabel.setVisible(false);
        copiedLabel.setBounds(140, 0, 40, 20);

        createButton.setFocusPainted(false);
        createButton.addActionListener(e -> createRoom());
        createButton.setBounds(0, 25, 248, 20);

        joinButton.addActionListener(e -> showJoinRoomMenu());
        joinButton.setFocusPainted(false);
        joinButton.setBounds(252, 25, 248, 20);

        coopPanel.add(joinCodeLabel); coopPanel.add(copyJoinCodeButton); coopPanel.add(copiedLabel);
        coopPanel.add(createButton); coopPanel.add(joinButton);

        mainPanel.add(closestStrongholdsPanel);
        mainPanel.add(coopPanel);

        add(mainPanel);

        //Initialize frame parameters
        setResizable(false);
        setAlwaysOnTop(true);
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(UI.class.getResource("/icon.png")));
        setIconImage(icon.getImage());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        setTitle("Superflat All Portals Calculator");
        setSize(516, 248);
        setLocation(-7, 0);
        setVisible(true);
    }

    public static void updateClosestStrongholds(Pair<BPos, String> locationData) {
        //Get the stronghold the player is in if there is one
        Integer strongholdInRadius = Strongholds.getStrongholdInRadius(locationData);
        //Ignore if in the end
        if (strongholdInRadius == null) {
            return;
        }
        //If the player is in a stronghold
        if (strongholdInRadius != -1) {
            if (inCoopMode) {
                //Send stronghold index to database to mark as visited (updating the list is not required here, as the update listener will detect writes from the user as well)
                DatabaseHandler.sendData(strongholdInRadius);
            } else {
                //Mark stronghold as visited
                updateCoordsList(strongholdInRadius);
            }
        }
        List<Integer> closestStrongholds = Strongholds.getClosestStrongholds();
        //Clear table to avoid display issues with less than 3 strongholds remaining
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                closestStrongholdsTable.getModel().setValueAt("", i, j);
            }
        }
        int i = 0;
        //Iterate through the closest strongholds (not for-i because there can be less than 3 left)
        for (Integer strongholdIndex : closestStrongholds) {
            //Update table with data
            closestStrongholdsTable.getModel().setValueAt(Strongholds.strongholdPositions[strongholdIndex].x() + ", " + Strongholds.strongholdPositions[strongholdIndex].z(), i, 0);
            closestStrongholdsTable.getModel().setValueAt(String.valueOf(Math.round((locationData.getSecond().equals("minecraft:overworld") ? Strongholds.strongholdPositions[strongholdIndex] : Strongholds.strongholdPositions[strongholdIndex].toNetherPos()).distanceTo(locationData.getFirst()))), i, 1);
            closestStrongholdsTable.getModel().setValueAt("(" + Strongholds.strongholdPositions[strongholdIndex].toNetherPos().x() + ", " + Strongholds.strongholdPositions[strongholdIndex].toNetherPos().z() + ")", i, 2);
            closestStrongholdsTable.getModel().setValueAt("["+ Strongholds.getAngle(locationData.getSecond().equals("minecraft:overworld") ? locationData.getFirst() : locationData.getFirst().toOverworldPos(), strongholdIndex) + "]", i, 3);
            i++;
        }
    }

    private static void initCoop() {
        //Initialize co-op variable state and UI
        inCoopMode = true;
        createButton.setEnabled(false);
        joinButton.setEnabled(false);
        copyJoinCodeButton.setVisible(true);
        joinCodeLabel.setText("Join code: " + DatabaseHandler.getJoinCode());
    }

    private void createRoom() {
        //Create room, initialize co-op, and notify user
        DatabaseHandler.createRoom();
        initCoop();
        JOptionPane.showMessageDialog(this, "Room created successfully!", "Room Created", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showJoinRoomMenu() {
        //Loop until a valid room code is entered or the menu is closed
        while (true) {
            //Query join code
            String joinCode = JOptionPane.showInputDialog(this, "Join code:", "Join Code", JOptionPane.QUESTION_MESSAGE);
            //If the menu is closed, return
            if (joinCode == null) {
                return;
            }
            try {
                //Check for a valid join code
                if (DatabaseHandler.isValidJoinCode(joinCode)) {
                    //If the join code is valid, update the UI accordingly and notify the user
                    initCoop();
                    JOptionPane.showMessageDialog(this, "Room joined successfully!", "Room Joined", JOptionPane.INFORMATION_MESSAGE);
                    return;
                } else {
                    //Warn the user in the case of an invalid join code
                    JOptionPane.showMessageDialog(this, "Invalid join code!", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            } catch (InterruptedException ignored) {}
        }
    }

    private static void copyJoinCode() {
        //Copy the join code to the clipboard
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(DatabaseHandler.getJoinCode()), null);
        copiedLabel.setVisible(true);
    }
}
