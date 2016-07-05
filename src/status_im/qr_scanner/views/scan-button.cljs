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


(defview scan-button [handler]
  []
  [view st/scan-button
   [touchable-highlight
    {:on-press handler}
    [view st/scan-button-content
     [image {:source {:uri :scan_blue}
             :style  icon-scan}]
     [text {:style st/scan-text} (label :t/scan-qr)]]]])