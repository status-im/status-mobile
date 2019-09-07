(ns status-im.utils.theme
  (:require ["react-native-dark-mode" :as react-native-dark-mode]
            [oops.core :refer [oget ocall]]))

(def event-emitter (oget react-native-dark-mode "eventEmitter"))
(def initial-mode (atom (oget react-native-dark-mode "initialMode")))

(defn add-mode-change-listener [callback]
  (ocall event-emitter "on" "currentModeChanged" #(do (reset! initial-mode %)
                                                      (callback (keyword %)))))

(defn is-dark-mode []
  (= @initial-mode "dark"))
