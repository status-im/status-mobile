(ns quo2.components.avatars.channel-avatar
  (:require [quo2.foundations.colors :as colors]
            [quo.react-native :as rn]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo.theme :as theme]))

(defn channel-avatar [{:keys [big? lock-status emoji-background-color emoji]}]
  (let [locked?      (= :locked lock-status)
        lock-exists? (not= :none lock-status)
        dark?        (theme/dark?)]
    [rn/view {:style {:width            (if big? 32 24)
                      :height           (if big? 32 24)
                      :border-radius    (if big? 32 24)
                      :justify-content  :center
                      :align-items      :center
                      :background-color emoji-background-color}}
     [rn/view {:style {:display         :flex
                       :justify-content :center
                       :align-items     :center}}
      [text/text {:size (if big?
                          :paragraph-1
                          :label)} emoji]
      (when lock-exists?
        [rn/view {:style {:position         :absolute
                          :left             (if big?
                                              13
                                              5)
                          :top              (if big?
                                              14
                                              5)
                          :background-color (if dark?
                                              colors/neutral-90
                                              colors/white)
                          :border-radius    15
                          :padding          2}}
         [icons/icon (if locked?
                       :main-icons/locked
                       :main-icons/unlocked)
          {:color           (if dark?
                              colors/neutral-40
                              colors/neutral-50)
           :container-style {:width  16
                             :height 16}
           :size            12}]])]]))