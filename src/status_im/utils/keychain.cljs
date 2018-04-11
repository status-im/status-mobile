(ns status-im.utils.keychain
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.react-native.js-dependencies :as rn]))

(def key-bytes 64)
(def username "status-im.encryptionkey")

(defn- encryption-key-fetch [{:keys [resolve reject]}]
  (-> (.getGenericPassword rn/keychain)
      (.then
        (fn [res]
          (if (not res)
            (when reject
              (reject))
            (let [encryption-key (.parse js/JSON (.-password res))]
              (log/debug "Found existing encryption key!")
              (re-frame/dispatch [:got-encryption-key {:encryption-key encryption-key
                                                       :callback       resolve}])))))
      (.catch
        (fn [err]
          (log/debug err)))))

(defn encryption-key-reset []
  (log/debug "Resetting key...")
  (-> (.resetGenericPassword rn/keychain)))

(defn get-encryption-key-then [callback]
  (log/debug "Initializing realm encryption key...")
  (encryption-key-fetch {:resolve  callback
                         :reject   (fn []
                                     (log/debug "No key exists, creating...")
                                     (-> (rn/secure-random key-bytes)
                                         (.then
                                           (fn [encryption-key]
                                             (-> (.setGenericPassword
                                                   rn/keychain
                                                   username
                                                   (.stringify js/JSON (.from js/Array encryption-key)))
                                                 (.then
                                                   (fn [res]
                                                     (encryption-key-fetch {:resolve callback})))
                                                 (.catch
                                                   (fn [err]
                                                     (log/debug err))))))
                                          (.catch
                                            (fn [err]
                                              (log/debug err)))))}))