(ns quo.components.banners.alert-banner.view
  (:require [quo.components.banners.alert-banner.schema :as component-schema]
            [quo.components.banners.alert-banner.style :as style]
            [quo.components.buttons.button.view :as button]
            [quo.components.icon :as icon]
            [quo.components.markdown.text :as text]
            [quo.foundations.colors :as colors]
            [quo.theme]
            [react-native.core :as rn]
            [react-native.linear-gradient :as linear-gradient]
            [schema.core :as schema]))

(defn- view-internal
  [{:keys [action? text button-text text-number-of-lines container-style on-button-press]}]
  (let [theme (quo.theme/use-theme)]
    [rn/view
     {:accessibility-label :alert-banner}
     [linear-gradient/linear-gradient
      {:style  (style/container container-style)
       :start  {:x 0 :y 0}
       :end    {:x 0 :y 1}
       :colors [(colors/theme-colors
                 colors/danger-50-opa-5
                 colors/danger-50-opa-10
                 theme)
                colors/danger-50-opa-0]}
      [rn/view {:style style/content-container}
       [icon/icon
        :i/alert
        {:color           (colors/resolve-color :danger theme)
         :size            16
         :container-style style/icon}]
       [text/text
        {:style           (style/label theme)
         :size            :paragraph-2
         :number-of-lines (or text-number-of-lines 1)}
        text]]
      (when action?
        [button/button
         {:accessibility-label :button
          :type                :danger
          :size                24
          :on-press            on-button-press}
         [text/text
          {:style  style/button-text
           :size   :paragraph-2
           :weight :medium}
          button-text]])]]))

(def view (schema/instrument #'view-internal component-schema/?schema))
