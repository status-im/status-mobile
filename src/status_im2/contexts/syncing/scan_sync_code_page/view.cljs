(ns status-im2.contexts.syncing.scan-sync-code-page.view
  (:require [react-native.core :as rn]
            [status-im2.contexts.syncing.scan-sync-code-page.style :as style]
            [status-im2.contexts.syncing.scan-sync-code.view :as scan-sync-code]
            [utils.i18n :as i18n]))

(defn view
  []
  [scan-sync-code/view
   {:title       (i18n/label :t/scan-sync-code)
    :background  [rn/view
                  {:style style/background} true]
    :screen-name "scan-sync-code-page"}])
