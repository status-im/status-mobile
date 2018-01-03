(ns status-im.ui.components.image-button.view
  (:require [status-im.i18n :as i18n]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.image-button.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]))

(defn- image-button [{:keys [value style handler]}]
  [react/view styles/image-button
   [react/touchable-highlight {:on-press handler}
    [react/view styles/image-button-content
     [vector-icons/icon :icons/fullscreen {:color :blue :style components.styles/icon-scan}]
     (when value
       [react/text {:style style} value])]]])

(defn scan-button [{:keys [show-label? handler]}]
  [image-button {:value   (when show-label?
                            (i18n/label :t/scan-qr))
                 :style   styles/scan-button-text
                 :handler handler}])