# Status App Logging and Sharing Flow

## Overview
The Status app maintains four log files to track different aspects of its operation. This document outlines the purpose of each log file, their configuration, storage, and the process for sharing logs.

## Log Files

- **api.log**  
  Records interactions between the frontend and the backend(status-go).

- **pre-login.log**  
  Captures logs generated before user login.

- **Status.log**  
  Stores logs output by the frontend.

- **Profile log**  
  Dynamically named log file that records user-specific logs after successful login. The log file name is generated based on the user's keyUID which is truncated to 8 characters. Each user profile has its own log file.

## Log Level Configuration

### pre-login.log  
- **Setting Method:** Shake the phone before login to open a dialog box.
- **Options:** Includes a log level setting option (e.g., DEBUG, INFO).
- **Behavior:** The log level setting is only available pre-login. After login, shaking the phone does not display this option.
- **Impact:** The configured log level is inherited by the Profile log for newly created users.

### Profile Log  
- **Setting Method:** Can be configured after user login through app advanced settings.
- **Note:** Inherits the log level from pre-login.log for new users.

## Log File Storage
- **Default Behavior:** All log files are stored in a protected folder named "logs" within the app to ensure privacy and security.
- **Exception:** On Android e2e build, logs are stored in the publicly accessible Download folder.

## Log setting storage
- pre-login setting is stored with [mmkv](https://github.com/mrousavy/react-native-mmkv), we have some pre-configured values for log level in files `.env.xxx` e.g `.env` / `.env.e2e` , corresponding field name is `LOG_LEVEL`
- profile log setting is stored in db with status-go

## Log Sharing
- **Method:**
  - Shake the phone to open a dialog box.
  - Select the "Share logs" option from the menu.
- **Note:** Be careful when sharing logs with others, as it may leak sensitive information.

