(ns quo2.components.dividers.new-messages
  (:require [quo.react-native :as rn]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]))

(def themes
  {:light {:primary    {:text-color       colors/primary-50
                        :background-color colors/primary-50-opa-5}
           :purple     {:text-color       colors/purple-50
                        :background-color colors/purple-50-opa-5}
           :indigo     {:text-color       colors/indigo-50
                        :background-color colors/indigo-50-opa-5}
           :turquoise  {:text-color       colors/turquoise-50
                        :background-color colors/turquoise-50-opa-5}
           :blue       {:text-color       colors/blue-50
                        :background-color colors/blue-50-opa-5}
           :green      {:text-color       colors/green-50
                        :background-color colors/green-50-opa-5}
           :yellow     {:text-color       colors/yellow-50
                        :background-color colors/yellow-50-opa-5}
           :orange     {:text-color       colors/orange-50
                        :background-color colors/orange-50-opa-5}
           :red        {:text-color       colors/red-50
                        :background-color colors/red-50-opa-5}
           :pink       {:text-color       colors/pink-50
                        :background-color colors/pink-50-opa-5}
           :brown      {:text-color       colors/brown-50
                        :background-color colors/brown-50-opa-5}
           :beige      {:text-color       colors/beige-50
                        :background-color colors/beige-50-opa-5}}
   :dark  {:primary    {:text-color       colors/primary-60
                        :background-color colors/primary-60-opa-5}
           :purple     {:text-color       colors/purple-60
                        :background-color colors/purple-60-opa-5}
           :indigo     {:text-color       colors/indigo-60
                        :background-color colors/indigo-60-opa-5}
           :turquoise  {:text-color       colors/turquoise-60
                        :background-color colors/turquoise-60-opa-5}
           :blue       {:text-color       colors/blue-60
                        :background-color colors/blue-60-opa-5}
           :green      {:text-color       colors/green-60
                        :background-color colors/green-60-opa-5}
           :yellow     {:text-color       colors/yellow-60
                        :background-color colors/yellow-60-opa-5}
           :orange     {:text-color       colors/orange-60
                        :background-color colors/orange-60-opa-5}
           :red        {:text-color       colors/red-60
                        :background-color colors/red-60-opa-5}
           :pink       {:text-color       colors/pink-60
                        :background-color colors/pink-60-opa-5}
           :brown      {:text-color       colors/brown-60
                        :background-color colors/brown-60-opa-5}
           :beige      {:text-color       colors/beige-60
                        :background-color colors/beige-60-opa-5}}})

(defn new-messages
  "new-messages params - label, color"
  [{:keys [label color] :or {label (i18n/label :new-messages-header)
                             color :primary}}]
  (let [colors (colors/theme-colors (themes :light) (themes :dark))
        bg-color (get-in colors [color :background-color])
        text-color (get-in colors [color :text-color])]
    [react/linear-gradient {:colors [bg-color "rgba(0,0,0,0)"]
                            :start {:x 0 :y 0} :end {:x 0 :y 1}}
     [rn/view {:style {:padding-left 60
                       :padding-vertical 12
                       :padding-right 24}}
      [text/text
       {:size :paragraph-2
        :weight :medium
        :style {:color text-color}}
       label]]]))