(ns status-im.utils.theme
  (:require ["react-native" :refer (Appearance)]
            [oops.core :refer [ocall]]))

(def initial-mode (atom (ocall Appearance "getColorScheme")))

;; Note - don't use value returned by change listener
;; https://github.com/facebook/react-native/issues/28525
(defn add-mode-change-listener [callback]
  (ocall Appearance "addChangeListener" #(let [mode (ocall Appearance "getColorScheme")]
                                           (when-not (= mode @initial-mode)
                                             (reset! initial-mode mode)
                                             (callback (keyword mode))))))

(defn is-dark-mode []
  (= @initial-mode "dark"))
