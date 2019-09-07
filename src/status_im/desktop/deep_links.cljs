(ns status-im.desktop.deep-links
  (:require [re-frame.core :as re-frame]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [taoensso.timbre :as log]
            ["react-native" :refer (NativeEventEmitter)]))

(defn add-event-listener []
  (let [^js event-emitter (new NativeEventEmitter
                               js-dependencies/desktop-linking)]
    (.addListener event-emitter
                  "urlOpened"
                  (fn [data]
                    (log/debug "urlOpened event with data:" data)
                    (let [url (get (js->clj data) "url")]
                      (re-frame/dispatch [:handle-universal-link url]))))))
