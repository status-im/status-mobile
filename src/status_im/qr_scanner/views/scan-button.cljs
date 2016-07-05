(ns status-im.qr-scanner.views.scan-button
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                touchable-highlight]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.components.drawer.view :refer [drawer-view open-drawer]]
            [status-im.components.styles :refer [icon-scan]]
            [status-im.i18n :refer [label]]
            [status-im.qr-scanner.styles :as st]))


(defview scan-button [{:keys [showLabel icon labelStyle handler]}]
  (let [showLabel (if (nil? showLabel) true showLabel)]
    [view st/scan-button
     [touchable-highlight
      {:on-press handler}
      [view st/scan-button-content
       [image {:source {:uri (or icon :scan_blue)}
               :style  icon-scan}]
       (when showLabel [text {:style (merge st/scan-text labelStyle)}
                                  (label :t/scan-qr)])]]]))