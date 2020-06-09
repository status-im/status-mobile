(ns status-im.utils.theme
  (:require ["react-native-dark-mode" :as react-native-dark-mode]
            [status-im.utils.platform :as platform]
            [oops.core :refer [oget ocall]]))

(def event-emitter nil #_(oget react-native-dark-mode "eventEmitter"))
(def initial-mode nil #_(atom (oget react-native-dark-mode "initialMode")))

(defn add-mode-change-listener [callback]
  (ocall event-emitter "on" "currentModeChanged" #(do (reset! initial-mode %)
                                                      (callback (keyword %)))))

(defn is-dark-mode []
  (if platform/desktop?
    false
    (= @initial-mode "dark")))
