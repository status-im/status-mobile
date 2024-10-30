(ns status-backend.config
  (:require [status-im.config]))

(def ^:private url
  (str "http://"
       status-im.config/STATUS_BACKEND_ADDRESS
       ":"
       status-im.config/STATUS_BACKEND_PORT))

(def ^:private data-dir-path
  (str status-im.config/STATUS_BACKEND_STORAGE_DIR "/data"))

(def ^:private public-storage-dir
  (str status-im.config/STATUS_BACKEND_STORAGE_DIR "/public"))

(def status-go-url (str url "/statusgo/"))
(def signals-url (str url "/signals"))
(def keystore-dir-path (str data-dir-path "/keystore"))
(def log-dir-path (str data-dir-path "/log"))
(def log-request-file-path (str public-storage-dir "/requests.log"))