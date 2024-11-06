//
//  Game log - this is a singleton class that is used to store all the log messages
//

package edu.ramapo.rpodziel.pente.model;

import java.util.Vector;

public class GameLog {
    /* Class Constants */

    public static final String MESSAGE_FORMAT = "\n";
    public static final String SECTION_FORMAT = "===================================\n";

    /* Private members */

    // Holds the game logs, i.e. what is happening in the game, as a static vector of strings
    private static final Vector<String> m_log = new Vector<String>();

    /* Public Utility Functions */

    /**
     * Adds a message to the log
     * @param a_message a String, the message to add
     */
    public static void AddMessage(String a_message) {
        m_log.add(a_message + MESSAGE_FORMAT);
    }

    /**
     * Gets a copy of the game log
     * @return a Vector<String>, the copy of the log
     */
    public static Vector<String> GetLog() {
        return new Vector<String>(m_log);
    }

    /**
     * Clears the log of all messages
     */
    public static void ClearLog() {
        m_log.clear();
    }

    /**
     * Formats the log to be displayed
     * @return a String, the formatted log in one string
     */
    public static String FormatLog() {
        StringBuilder formattedLog = new StringBuilder(SECTION_FORMAT);

        for (String message : m_log) {
            formattedLog.append(message);
        }

        return formattedLog.toString();
    }

}