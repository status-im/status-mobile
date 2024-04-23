(ns quo.components.community.community-stat.view
  (:require [quo.components.community.community-stat.style :as style]
            [quo.components.icon :as quo.icons]
            [quo.components.markdown.text :as quo.text]
            [quo.foundations.colors :as colors]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            utils.money))

(defn view
  [{:keys [value icon style accessibility-label text-size]}]
  (let [theme (quo.theme/use-theme)]
    [rn/view
     {:style               (merge style/container style)
      :accessibility-label accessibility-label}
     [quo.icons/icon icon
      {:size            16
       :container-style {:align-items     :center
                         :justify-content :center}
       :resize-mode     :center
       :color           (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}]
     [quo.text/text
      {:size   (or text-size :paragraph-1)
       :weight :regular
       :style  (style/text theme)} (utils.money/format-amount value)]]))
