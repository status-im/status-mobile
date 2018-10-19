(ns status-im.desktop.deep-links
  (:require [re-frame.core :as re-frame]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [taoensso.timbre :as log]))

(defn add-event-listener []
  (let [event-emitter (new (.-NativeEventEmitter js-dependencies/react-native)
                           js-dependencies/desktop-linking)]
    (.addListener event-emitter
                  "urlOpened"
                  (fn [data]
                    (log/debug "urlOpened event with data:" data)
                    (let [url (get (js->clj data) "url")]
                      (re-frame/dispatch [:handle-universal-link url]))))))