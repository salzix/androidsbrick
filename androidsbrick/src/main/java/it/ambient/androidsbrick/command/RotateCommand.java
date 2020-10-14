package it.ambient.androidsbrick.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RotateCommand implements SBrickCommand {
    private static final String TAG                     = "RotateCommand";
    public static final byte DIR_CLOCKWISE              = 0x00;
    public static final byte DIR_COUNTER_CLOCKWISE      = 0x01;

    private static final byte SBRICK_COMMAND            = 0x01;
    private Map<Byte, byte[]> channels                  = new HashMap<>();

    /**
     * Assigns rotate command to channel A. Set direction and power.
     *
     * @param direction RotateCommand.DIR_CLOCKWISE or RotateCommand.DIR_COUNTER_CLOCKWISE
     * @param power byte 0-255, eg. "(byte) 0xFF"
     * @return RotateCommand to allow chaining another channel control
     */
    public RotateCommand channelA(byte direction, byte power) {
        byte[] part = {SBrickCommand.CHANNEL_A, direction, power};
        channels.put(SBrickCommand.CHANNEL_A, part);
        return this;
    }

    /**
     * Assigns rotate command to channel B. Set direction and power.
     *
     * @param direction RotateCommand.DIR_CLOCKWISE or RotateCommand.DIR_COUNTER_CLOCKWISE
     * @param power byte 0-255, eg. "(byte) 0xFF"
     * @return RotateCommand to allow chaining another channel control
     */
    public RotateCommand channelB(byte direction, byte power) {
        byte[] part = {SBrickCommand.CHANNEL_B, direction, power};
        channels.put(SBrickCommand.CHANNEL_B, part);
        return this;
    }

    /**
     * Assigns rotate command to channel C. Set direction and power.
     *
     * @param direction RotateCommand.DIR_CLOCKWISE or RotateCommand.DIR_COUNTER_CLOCKWISE
     * @param power byte 0-255, eg. "(byte) 0xFF"
     * @return RotateCommand to allow chaining another channel control
     */
    public RotateCommand channelC(byte direction, byte power) {
        byte[] part = {SBrickCommand.CHANNEL_C, direction, power};
        channels.put(SBrickCommand.CHANNEL_C, part);
        return this;
    }

    /**
     * Assigns rotate command to channel D. Set direction and power.
     *
     * @param direction RotateCommand.DIR_CLOCKWISE or RotateCommand.DIR_COUNTER_CLOCKWISE
     * @param power byte 0-255, eg. "(byte) 0xFF"
     * @return RotateCommand to allow chaining another channel control
     */
    public RotateCommand channelD(byte direction, byte power) {
        byte[] part = {SBrickCommand.CHANNEL_D, direction, power};
        channels.put(SBrickCommand.CHANNEL_D, part);
        return this;
    }
    /**
     * Assigns this command to all channels. Sets direction and power.
     *
     * @param direction RotateCommand.DIR_CLOCKWISE or RotateCommand.DIR_COUNTER_CLOCKWISE
     * @param power byte 0-255, eg. "(byte) 0xFF"
     * @return RotateCommand to allow chaining specific channel control
     */
    public RotateCommand allChannels(byte direction, byte power) {
        for (byte channel = 0; channel <= 3; channel++) {
            byte[] part = {channel, direction, power};
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
