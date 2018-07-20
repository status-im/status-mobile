(ns status-im.utils.keychain.core
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.react-native.js-dependencies :as rn]
            [status-im.utils.platform :as platform]))

(def key-bytes 64)
(def username "status-im.encryptionkey")

(defn- bytes->js-array [b]
  (.from js/Array b))

(defn- string->js-array [s]
  (.parse js/JSON (.-password s)))

;; Smoke test key to make sure is ok, we noticed some non-random keys on
;; some IOS devices. We check naively that there are no more than key-bytes/2
;; identical characters.
(defn validate
  [encryption-key]
  (cond
    (or (not encryption-key)
        (not= (.-length encryption-key) key-bytes))
    (.reject js/Promise {:error :invalid-key
                         :key    encryption-key})

    (>= (/ key-bytes 2)
        (count (keys (group-by identity encryption-key))))
    (.reject js/Promise {:error :weak-key
                         :key   encryption-key})

    :else encryption-key))

(defn store [encryption-key]
  (log/debug "storing encryption key")
  (-> (.setGenericPassword
       rn/keychain
       username
       (.stringify js/JSON encryption-key))
      (.then (constantly encryption-key))))

(defn create []
  (log/debug "no key exists, creating...")
  (.. (rn/secure-random key-bytes)
      (then bytes->js-array)))

(defn handle-not-found []
  (.. (create)
      (then validate)
      (then store)))

(def handle-found
  (comp validate
        string->js-array))

(defn get-encryption-key []
  (log/debug "initializing realm encryption key...")
  (.. (.getGenericPassword rn/keychain)
      (then
       (fn [res]
         (if res
           (handle-found res)
           (handle-not-found))))))

(defn safe-get-encryption-key
  "Return encryption key or empty string in case invalid/empty"
  []
  (log/debug "initializing realm encryption key...")
  (.. (get-encryption-key)
      (catch (fn [{:keys [_ key]}]
               (log/warn "key is invalid, continuing")
               (or key "")))))

(defn reset []
  (log/debug "resetting key...")
  (.resetGenericPassword rn/keychain))

(defn set-username []
  (when platform/desktop? (.setUsername rn/keychain username)))
