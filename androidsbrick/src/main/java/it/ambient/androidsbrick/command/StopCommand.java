package it.ambient.androidsbrick.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StopCommand implements SBrickCommand {
    private static final String TAG                     = "StopCommand";
    private static final byte SBRICK_COMMAND            = 0x00;
    private Map<Byte, byte[]> channels                  = new HashMap<>();

    /**
     * Assigns stop command to channel A.
     *
     * @return StopCommand to allow chaining another channel
     */
    public StopCommand channelA() {
        byte[] part = {SBrickCommand.CHANNEL_A};
        channels.put(SBrickCommand.CHANNEL_A, part);
        return this;
    }

    /**
     * Assigns stop command to channel B.
     *
     * @return StopCommand to allow chaining another channel
     */
    public StopCommand channelB() {
        byte[] part = {SBrickCommand.CHANNEL_B};
        channels.put(SBrickCommand.CHANNEL_B, part);
        return this;
    }

    /**
     * Assigns stop command to channel C.
     *
     * @return StopCommand to allow chaining another channel
     */
    public StopCommand channelC() {
        byte[] part = {SBrickCommand.CHANNEL_C};
        channels.put(SBrickCommand.CHANNEL_C, part);
        return this;
    }

    /**
     * Assigns stop command to channel D.
     *
     * @return StopCommand to allow chaining another channel
     */
    public StopCommand channelD() {
        byte[] part = {SBrickCommand.CHANNEL_D};
        channels.put(SBrickCommand.CHANNEL_D, part);
        return this;
    }

    /**
     * Assigns stop command to all channels.
     *
     * @return StopCommand to allow chaining specific channel
     */
    public StopCommand allChannels() {
        for (byte channel = 0; channel <= 3; channel++) {
            byte[] part = {channel};
            channels.put(channel, part);
        }
        return this;
    }

    public byte[] getPreparedStream() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(SBRICK_COMMAND);
        for (byte[] part : channels.values()) {
            outputStream.write(part);
        }
        return outputStream.toByteArray();
    }
}
