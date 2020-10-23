package me.friwi.arterion.plugin.util.recording;

import me.friwi.recordable.RecordingCreator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RecordingManager {
    private CopyOnWriteArrayList<RecordingCreator> activeRecordings = new CopyOnWriteArrayList<>();

    public List<RecordingCreator> getActiveRecordings() {
        return activeRecordings;
    }
}
