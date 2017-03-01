(ns status-im.components.confirm-button
  (:require [status-im.components.styles :as st]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.components.react :refer [view
                                                text
                                                touchable-highlight]]))

(defn confirm-button [label on-press]
  [touchable-highlight {:on-press on-press}
   [view st/confirm-button
    [text {:style (get-in platform-specific [:component-styles :confirm-button-label])
           :uppercase? (get-in platform-specific [:uppercase?])}
          label]]])