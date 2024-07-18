package com.sophie;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.Random;

import static com.sophie.Strongholds.updateCoordsList;

public class DatabaseHandler {
    private static String joinCode;
    private static DatabaseReference visitedListRef;
    private static boolean isValidatedJoinCode = false;
    private static ChildEventListener visitedListListener;

    static {
        //Initialize database
        try {
            FirebaseConfig.initializeFirebase();
        } catch (Exception ignored) {}
    }

    public static void createRoom() {
        //Create random 6-letter join code
        StringBuilder joinCode = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < 6; i++) {
            int letterChoiceOffset = rand.nextInt(26);
            joinCode.append((char) ((rand.nextBoolean() ? 'A' : 'a') + letterChoiceOffset));
        }
        setJoinCode(joinCode.toString(), true);
        //Initialize database update listening
        runDataListener();
    }

    private static void setJoinCode(String code, boolean isCreating) {
        joinCode = code;
        //Set up database node reference for visited strongholds list
        visitedListRef = FirebaseConfig.getDatabase().getReference(joinCode);
        if (isCreating) {
            //On room creation, clear existing values for that node and send verification message (-1)
            visitedListRef.removeValue((databaseError, databaseReference) -> {});
            sendData(-1);
        }
    }

    public static String getJoinCode() {
        return joinCode;
    }

    public static boolean isValidJoinCode(String code) throws InterruptedException {
        setJoinCode(code, false);
        runDataListener();
        Thread.sleep(2000);
        //If verification message wasn't received within the 2-second wait, abort the connection and return false
        if (!isValidatedJoinCode) {
            visitedListRef.removeEventListener(visitedListListener);
            return false;
        }
        return true;
    }

    public static void sendData(int data) {
        //Send data to the database at the previously defined node reference
        visitedListRef.push().setValue(data, (databaseError, databaseReference) -> {});
    }

    public static void runDataListener() {
        try {
            visitedListListener = visitedListRef.addChildEventListener(new ChildEventListener() {
                //When data is added to the watched node reference:
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    int strongholdNumber = dataSnapshot.getValue(Integer.class);
                    //Ensure that verification data doesn't get passed as a stronghold position index, avoiding an ArrayIndexOutOfBoundsException
                    if (strongholdNumber != -1) {
                        updateCoordsList(strongholdNumber);
                    } else {
                        //Communicate that the verification message has been received
                        isValidatedJoinCode = true;
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {}

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {}

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        } catch (Exception ignored) {}
    }
}
