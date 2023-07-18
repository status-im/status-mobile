(ns quo2.components.inputs.locked-input.view
  (:require [react-native.core :as rn]
            [status-im2.contexts.quo-preview.tabs.segmented-tab]
            [quo2.foundations.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]
            [quo2.components.inputs.locked-input.style :as style]
            [quo2.theme :as theme]
            [quo2.components.markdown.text :as text]))

(defn- info-box
  [theme icon label-text]
  [rn/view
   {:style (style/info-box-container theme)}
   [rn/view {:margin-right 1}
    [icons/icon icon {:width  20
                      :height 20
                      :color  (colors/theme-colors colors/neutral-50
                                                   colors/neutral-10 theme)}]]
   [text/text
    {:style (style/info-box-label theme)} label-text]])

(defn- locked-input-internal
  [{:keys [label-text label-style value-text icon style theme]}]
  [rn/view {:style style}
   [text/text {:size   :paragraph-2
               :weight :medium
               :style  label-style} label-text]
   [info-box theme icon value-text]])

(def locked-input (theme/with-theme locked-input-internal))
