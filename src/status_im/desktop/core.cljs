(ns status-im.desktop.core
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            status-im.utils.db
            status-im.ui.screens.db
            status-im.ui.screens.events
            status-im.ui.screens.subs
            status-im.data-store.core
            [status-im.ui.screens.views :as views]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.ui.components.react :as react]
            [status-im.core :as core]))

(defn app-root []

  (reagent/create-class
    {
     :component-did-mount (fn [] ())
     :display-name "root"
     :reagent-render views/main}))

(defn check-random-bytes [length]
      (.randomBytes rn-dependencies/random-bytes
                    length
                    (fn [& [err buf]]
                        (if err
                          (print "== react-native-randombytes check error: " err)
                          (print "== react-native-randombytes check success")))))

(defn check-i18n []
      (let [lcl (.-locale rn-dependencies/i18n)] (print "== locale:" lcl)))

(defn check-3rdparty-libraries []
      (print "========= Check 3rd party libraries ==========")
      (print "== Check react-native-randombytes...")
      (check-random-bytes 1024)
      (print "== Check react-native-i18n...")
      (check-i18n))


(defn init []
  (enable-console-print!)
  (check-3rdparty-libraries)
  (core/init app-root))
