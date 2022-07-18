(ns quo2.components.channel-avatar
  (:require [quo2.foundations.colors :as colors]
            [quo.react-native :as rn]
            [clojure.string :as clj-string]
            [status-im.ui.components.icons.icons :as icons]))

(defn channel-avatar [{:keys [big? dark? lock-status icon x y]}]
  (let [locked? (= :locked lock-status)
        lock-exists? (not= :none lock-status)]
    [rn/view {:style {:width (if big? 32 24)
                      :height (if big? 32 24)
                      :top (if (or (clj-string/blank? x)
                                   (js/isNaN x))
                             0
                             (js/parseInt x))
                      :left (if (or (clj-string/blank? y)
                                    (js/isNaN y))
                              0
                              (js/parseInt y))
                      :border-radius (if big? 32 24)
                      :background-color (if dark? colors/neutral-70
                                            colors/neutral-30)}}
     [rn/view {:style {:left (if big? 6 3)
                       :top (if big? 6 3)
                       :width 20
                       :height 20
                       :display :flex
                       :justify-content :center
                       :align-items :center}}
      [icons/icon (keyword (str "main-icons/" icon))
       {:color "nil"
        :width 15
        :height 15}]
      (when lock-exists?
        [rn/view {:style {:position :absolute
                          :left (if big? 16 8)
                          :top (if big? 16 8)
                          :background-color (if dark? colors/neutral-90
                                                "white")
                          :border-radius 15
                          :padding 2}}
         [icons/icon (if locked?
                       :main-icons/locked16
                       :main-icons/unlocked16)
          {:color (if dark?
                    colors/neutral-40
                    colors/neutral-50)
           :width 16
           :height 16}]])]]))