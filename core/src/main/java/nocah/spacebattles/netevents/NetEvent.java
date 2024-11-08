package nocah.spacebattles.netevents;

public interface NetEvent {
    int getEventID();

    // this is the size of all data excluding id and size
    int getDataByteSize();
}





