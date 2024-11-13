package nocah.spacebattles.netevents;

public interface NetEvent {
    byte getEventID();

    // this is the size of all data excluding id and size
    short getDataByteSize();

    byte[] serialize();
}





