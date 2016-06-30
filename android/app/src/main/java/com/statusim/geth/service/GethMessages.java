package com.statusim.geth.service;


public class GethMessages {

    /**
     * Start the node
     */
    public static final int MSG_START_NODE = 1;

    /**
     * Node started event
     */
    public static final int MSG_NODE_STARTED = 2;

    /**
     * Stop the node
     */
    public static final int MSG_STOP_NODE = 3;

    /**
     * Node stopped event
     */
    public static final int MSG_NODE_STOPPED = 4;

    /**
     * Unlock an account
     */
    public static final int MSG_LOGIN = 5;

    /**
     * Account unlocked event
     */
    public static final int MSG_LOGGED_IN = 6;

    /**
     * Create an account
     */
    public static final int MSG_CREATE_ACCOUNT = 7;

    /**
     * Account created event
     */
    public static final int MSG_ACCOUNT_CREATED = 8;

    /**
     * Add an account
     */
    public static final int MSG_ADD_ACCOUNT = 9;

    /**
     * Account added event
     */
    public static final int MSG_ACCOUNT_ADDED = 10;

}
