(ns quo2.components.inputs.locked-input.view
  (:require [react-native.core :as rn]
            [status-im2.contexts.quo-preview.tabs.segmented-tab]
            [quo2.foundations.colors :as colors]
            [quo2.components.icon :as icons]
            [quo2.components.inputs.locked-input.style :as style]
            [quo2.theme :as quo.theme]
            [quo2.components.markdown.text :as text]))

(defn- info-box
  [{:keys [icon value-text theme]}]
  [rn/view
   {:style (style/info-box-container theme)}
   [rn/view
    [icons/icon icon {:color (colors/theme-colors colors/neutral-50
                                                  colors/neutral-10 theme)}]]
   [text/text
    {:size  :paragraph-1
     :style (style/info-box-label theme)} value-text]])

(defn- locked-input-internal
  [{:keys [label-text icon style theme]} value]
  [rn/view {:style style}
   [text/text {:size   :paragraph-2
               :weight :regular
               :style  {:color colors/neutral-50}} label-text]
   [info-box {:theme      theme
              :icon       icon
              :value-text value}]])

(def locked-input (quo.theme/with-theme locked-input-internal))
