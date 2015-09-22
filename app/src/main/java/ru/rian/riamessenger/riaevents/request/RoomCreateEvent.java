package ru.rian.riamessenger.riaevents.request;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Roman on 7/8/2015.
 */

@AllArgsConstructor
public class RoomCreateEvent {

    @Getter
    String roomName;

    @Getter
    ArrayList<String> participantsArrayList;
}
