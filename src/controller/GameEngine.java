package controller;

import model.*;
import view.ConsoleUtil;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

// Author: Imri Tull
// Class: GameEngine
// Date: 11/16/2025
// ITEC 3860 – Group Project (Escaping The Red Cross)

public class GameEngine {

    public Player player;
    private Scanner scanner = new Scanner(System.in);

    public GameEngine(Player player) {
        this.player = player;
        player.setRoom(1);
    }

    public Scanner getScanner() {
        return scanner;
    }

    public Player getPlayer() {
        return player;
    }

    public void run() {
        System.out.println("\nWelcome to Escaping The Red Cross!");
        System.out.println("Type 'tutorial' if this is your first time, or 'help' for commands.");
        System.out.println();

        describeCurrentRoom();

        while (true) {
            System.out.print("\n> ");
            String cmd = scanner.nextLine();

            EnhancedCommandParser.parse(cmd, this);
        }
    }

    public void describeCurrentRoom() {
        Room room = DatabaseManager.getRoom(player.getRoom());

        if (room == null) {
            System.out.println("\n[Room " + player.getRoom() + "] Unknown Room");
            System.out.println("This room does not exist in the database.");
            return;
        }

        int floor = FloorManager.getFloor(player.getRoom());
        String ANSI_CYAN = "\u001B[36m";
        String ANSI_DIM = "\u001B[90m";
        String ANSI_RESET = "\u001B[0m";

        System.out.println();
        System.out.println(ANSI_CYAN + "[Room " + player.getRoom() + "] "
                + room.name + " (Floor " + floor + ")" + ANSI_RESET);
        System.out.println(ANSI_DIM
                + "============================================================"
                + ANSI_RESET);
        System.out.println();
        System.out.println(ConsoleUtil.wrapText(room.description));

        EnhancedCommandParser.updateLastRoomDescription(this);

        System.out.println("\nAvailable directions:");
        if (room.north != -1) System.out.println("  - north (to room " + room.north + ")");
        if (room.south != -1) System.out.println("  - south (to room " + room.south + ")");
        if (room.east  != -1) System.out.println("  - east (to room " + room.east + ")");
        if (room.west  != -1) System.out.println("  - west (to room " + room.west + ")");

        List<Item> itemsInRoom = room.getItems();
        itemsInRoom.removeIf(Objects::isNull);
        if (itemsInRoom.isEmpty()) {
            System.out.println("\nThis room has no items.");
        } else {
            System.out.println("\nItems in this room:");
            for (Item item : itemsInRoom) {
                System.out.println("  - " + item.getName());
            }
        }

        if (room.puzzle != -1) {
            System.out.println("\n💡 There is a puzzle here. Type 'use puzzle' to attempt it.");
        }

        if (room.monster != -1) {
            DatabaseManager.MonsterData monster = DatabaseManager.getMonster(room.monster);
            if (monster != null) {
                System.out.println("\n⚠️  A monster is here: " + monster.name + ". Type 'fight' to battle.");
                System.out.println(ConsoleUtil.wrapText(monster.description));
            }
        }

        if (player.getRoom() == 1 && DatabaseManager.canFightBiggieCheese()) {
            System.out.println("\nA strange energy fills the room…");
            System.out.println("Type 'unlock secret' to investigate.");
        }
    }

    public void handleSecretBoss(Player player) {
        // Must be in Room 1
        if (player.getRoom() != 1) {
            System.out.println("You feel nothing special here.");
            return;
        }

        // Must have all puzzles completed
        if (!DatabaseManager.canFightBiggieCheese()) {
            System.out.println("The air is still. Nothing happens... maybe you're missing something.");
            return;
        }

        System.out.println("\nA loud rumble shakes the closet...");
        System.out.println("A shadow rises…");
        System.out.println("BIGGIE CHEESE CHALLENGES YOU!");
        ConsoleUtil.printAscii("ascii-art.txt");

        DatabaseManager.MonsterData data = DatabaseManager.getMonster(5);
        if (data == null) {
            System.out.println("ERROR: Biggie Cheese data missing!");
            return;
        }

        Battle.startBattle(player, data, new java.util.Scanner(System.in));

        if (data.hp <= 0) {
            System.out.println("\n*** SECRET ENDING UNLOCKED ***");
            triggerSecretEnding();
        }
    }

    private void triggerSecretEnding() {
        System.out.println("\n=====================================");
        System.out.println("           SECRET ENDING");
        System.out.println("=====================================");
        System.out.println("Biggie Cheese collapses…");
        System.out.println("A soft voice whispers: 'You weren't supposed to win…'");
        System.out.println("The hospital around you begins to fade.");
        System.out.println("You feel weightless.");
        System.out.println("You have escaped the Red Cross… but at what cost?");
        System.out.println("\n*** THANK YOU FOR PLAYING ***");
        System.exit(0);
    }
}
