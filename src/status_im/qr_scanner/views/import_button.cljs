(ns status-im.qr-scanner.views.import-button
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                touchable-highlight]]
            [status-im.components.styles :refer [icon-qr]]
            [status-im.i18n :refer [label]]
            [status-im.qr-scanner.styles :as st]))


(defview import-button [handler]
  []
  [view st/import-button
   [touchable-highlight
    {:on-press handler}
    [view st/import-button-content
     [image {:source {:uri :icon_qr}
             :style  icon-qr}]
     [text {:style st/import-text} (label :t/import-qr)]]]])