(ns status-im.utils.theme
  (:require ["react-native" :refer (Appearance)]
            [oops.core :refer [oget ocall]]))

(def initial-mode (atom (ocall Appearance "getColorScheme")))

(defn add-mode-change-listener [callback]
  (ocall Appearance "addChangeListener" #(let [mode (oget % "colorScheme")]
                                           (when-not (= mode @initial-mode)
                                             (reset! initial-mode mode)
                                             (callback (keyword mode))))))

(defn is-dark-mode []
  (= @initial-mode "dark"))
