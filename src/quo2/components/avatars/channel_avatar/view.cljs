(ns quo2.components.avatars.channel-avatar.view
  (:require [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]))

(defn view
  [{:keys [big? locked? emoji-background-color emoji]}]
  (let [lock-exists? locked?
        dark?        (theme/dark?)]
    [rn/view
     {:style {:width            (if big? 32 24)
              :height           (if big? 32 24)
              :border-radius    (if big? 32 24)
              :justify-content  :center
              :align-items      :center
              :background-color emoji-background-color}}
     [rn/view
      {:style {:display         :flex
               :justify-content :center
               :align-items     :center}}
      [text/text
       {:size (if big?
                :paragraph-1
                :label)} emoji]
      (when lock-exists?
        [rn/view
         {:style {:position         :absolute
                  :left             (if big?
                                      14
                                      8)
                  :top              (if big?
                                      15
                                      8)
                  :background-color (if dark?
                                      colors/neutral-90
                                      colors/white)
                  :border-radius    15
                  :padding          2}}
         [icons/icon
          (if locked?
            :main-icons/locked
            :main-icons/unlocked)
          {:color           (if dark?
                              colors/neutral-40
                              colors/neutral-50)
           :container-style {:width  12
                             :height 12}
           :size            12}]])]]))
