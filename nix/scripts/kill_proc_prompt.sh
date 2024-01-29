#!/usr/bin/env bash

# Function to prompt user for terminating a process
kill_proc_prompt() {
    local pid=$1

    # Ask the user whether to terminate the process
    read -p "Do you want to terminate this process? (y/n): " choice
    if [[ $choice == "y" ]]; then
        sudo kill $pid
        echo "Process $pid terminated."
    else
        echo "Process not terminated. Please close it manually and retry."
        return 1
    fi
}
