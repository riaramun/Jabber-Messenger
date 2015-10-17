package ru.rian.riamessenger.riaevents.request;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Roman on 7/30/2015.
 */

@AllArgsConstructor
public class RoomEditEvent {

    @Getter
    String roomThreadId;

    @Getter
    HashSet<String> participantsArrayList;
}
