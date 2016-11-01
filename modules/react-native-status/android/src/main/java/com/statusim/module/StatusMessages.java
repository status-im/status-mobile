package com.statusim.module;


public class StatusMessages {

    /**
     * Start the node
     */
    static final int MSG_START_NODE = 1;

    /**
     * Stop the node
     */
    static final int MSG_STOP_NODE = 2;

    /**
     * Unlock an account
     */
    static final int MSG_LOGIN = 3;

    /**
     * Create an account
     */
    static final int MSG_CREATE_ACCOUNT = 4;

    /**
     * Create an account
     */
    static final int MSG_RECOVER_ACCOUNT = 5;

    /**
     * Account complete transaction event
     */
    static final int MSG_COMPLETE_TRANSACTION = 6;

    /**
     * Geth event
     */
    public static final int MSG_GETH_EVENT = 7;

    /**
     * Initialize jail
     */
    public static final int MSG_JAIL_INIT = 8;


    /**
     * Parse js in jail
     */
    public static final int MSG_JAIL_PARSE = 9;

    /**
     * Parse js in jail
     */
    public static final int MSG_JAIL_CALL = 10;

    /**
     * Account discard transaction event
     */
    public static final int  MSG_DISCARD_TRANSACTION = 11;

}
