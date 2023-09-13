(ns quo2.components.avatars.wallet-user-avatar.style
  (:require utils.string))

(defn container
  [circle-size circle-color]
  {:width            circle-size
   :height           circle-size
   :border-radius    circle-size
   :text-align       :center
   :justify-content  :center
   :align-items      :center
   :background-color circle-color})
