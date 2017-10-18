(ns status-im.components.image-button.view
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                touchable-highlight]]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.components.styles :refer [icon-scan]]
            [status-im.i18n :refer [label]]
            [status-im.components.image-button.styles :as st]))

(defn image-button [{:keys [value style handler]}]
  [view st/image-button
   [touchable-highlight {:on-press handler}
    [view st/image-button-content
     [vi/icon :icons/fullscreen {:color :blue :style icon-scan}]
     (when text
       [text {:style style} value])]]])

(defn scan-button [{:keys [show-label? handler]}]
  [image-button {:value   (if show-label?
                            (label :t/scan-qr))
                 :style   st/scan-button-text
                 :handler handler}])