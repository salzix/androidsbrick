package it.ambient.androidsbrick;

import java.util.Map;

/**
 * Interface which must be implemented on application side to receive connected
 * SBrick instances and bluetooth permission requests.
 *
 * @author Tomasz Wegrowski <tomasz.wegrowski+github@gmail.com>
 */
public interface ConnectionCallback {

    void handleSBrickCollection(Map<String, SBrick> sBrickCollection);
    boolean handlePermissionRequests();
}
