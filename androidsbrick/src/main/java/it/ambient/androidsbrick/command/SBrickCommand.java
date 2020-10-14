package it.ambient.androidsbrick.command;

import java.io.IOException;

public interface SBrickCommand {
    static final byte CHANNEL_A  = 0x00;
    static final byte CHANNEL_B  = 0x01;
    static final byte CHANNEL_C  = 0x02;
    static final byte CHANNEL_D  = 0x03;
    byte[] getPreparedStream() throws IOException;
}
