/*
 * Copyright (C) BobMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

import java.util.ArrayList;
import java.util.List;

public class RoomTest {

    public static void main(String[] args) {

        List<Room> rooms = new ArrayList<>();

        rooms.add(new Room(true));
        rooms.add(new Room(false));

        int count = (int) rooms.stream().filter(Room::isAlive).count();

        System.out.println(count);
    }

    public static class Room {

        private final boolean alive;

        public Room(boolean alive) {
            this.alive = alive;
        }

        public boolean isAlive() {
            return alive;
        }

        @Override
        public String toString() {
            return "Room{" +
                    "alive=" + alive +
                    '}';
        }
    }
}
