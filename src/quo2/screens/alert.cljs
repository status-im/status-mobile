(ns quo2.screens.alert
  (:require [quo.theme :as theme]
            [quo.react-native :as rn] 
            [quo2.foundations.colors :as colors]))

(def themes
  {:light {:default    colors/white
           :background-color colors/black}
   :dark  {:default    colors/black
           :background-color colors/white}})

(defn get-color [key]
  (get-in themes [(theme/get-theme) key]))

(defn alert
  "type:    default, secondary, grey
   outline: true, false
   value:   integer"
  [{:keys [type outline]} value]
  (let [type       (or type :default)
        text-color (if (or
                        (= (theme/get-theme) :dark) 
                         (= type :default))
                     colors/white
                     colors/black)]
    [rn/view {:background-color "white"
              :flex             1}
     (rn/alert "Alert title" "Alert description" {:text "button1"
                                                  :on-press #(log/log "pressed")
                                                  :on-dismiss #(log/log "dismissed")
                                                  :cancelable false}
               {:text "button1"
                :on-press #(log/log "pressed")
                :on-dismiss #(log/log "dismissed")
                :cancelable false})]))
