package me.friwi.arterion.client.network;

import java.nio.ByteBuffer;

import com.google.common.base.Charsets;

public abstract class ModPacket {
    public abstract byte getId();
    public abstract void writeData(ByteBuffer buffer);
    public abstract void readData(ByteBuffer buffer);
    public String readString(ByteBuffer buffer){
        short length = buffer.getShort();
        int offs = buffer.position();
        buffer.position(offs+length);
        return new String(buffer.array(), offs, length, Charsets.ISO_8859_1);
    }
    public void writeString(ByteBuffer buffer, String s){
        byte[] data = s.getBytes(Charsets.ISO_8859_1);
        buffer.putShort((short) data.length);
        buffer.put(data);
    }
}
