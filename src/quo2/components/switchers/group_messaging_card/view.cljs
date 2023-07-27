(ns quo2.components.switchers.group-messaging-card.view
  (:require
    [react-native.core :as rn]
    [quo2.components.markdown.text :as text]
    [quo2.components.avatars.group-avatar :as group-avatar]
    [quo2.components.switchers.base-card.view :as base-card]
    [quo2.components.switchers.style :as style]
    [quo2.foundations.colors :as colors]
    [quo2.components.switchers.content :as content-container]))

(defn view
  []
  (fn [{:keys [avatar title subtitle customization-color
               banner on-close content]}]
    (let [color-60 (colors/custom-color customization-color 60)]
      [base-card/base-card
       {:banner              banner
        :customization-color customization-color
        :on-close            on-close}
       (when avatar
         [rn/view {:style style/avatar-container}
          [group-avatar/group-avatar
           {:color color-60
            :size  :large}]])
       [text/text
        {:size            :paragraph-1
         :weight          :semi-bold
         :number-of-lines 1
         :ellipsize-mode  :tail
         :style           style/title}
        title]
       [text/text
        {:size   :paragraph-2
         :weight :medium
         :style  style/subtitle}
        subtitle]
       [content-container/view content]])))
