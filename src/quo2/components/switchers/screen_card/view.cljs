(ns quo2.components.switchers.screen-card.view
  (:require
    [react-native.core :as rn]
    [quo2.components.markdown.text :as text]
    [quo2.components.switchers.base-card.view :as base-card]
    [quo2.components.switchers.screen-card.style :as style]
    [quo2.foundations.colors :as colors]))

(defn screen-card
  []
  (fn [{:keys [avatar title subtitle customization-color
               banner on-close]} & children]
    [base-card/base-card
     {:banner              banner
      :customization-color customization-color
      :on-close            on-close}
     (when avatar
       [rn/view {:style style/avatar-container} avatar])
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
     [rn/view {:style style/content-container} children]]))
