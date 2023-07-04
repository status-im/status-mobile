(ns status-im2.contexts.onboarding.sign-in.view
  (:require [utils.i18n :as i18n]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [status-im2.contexts.syncing.scan-sync-code.view :as scan-sync-code]))

(defn navigate-back
  []
  (when @scan-sync-code/navigate-back-fn
    (@scan-sync-code/navigate-back-fn)))

(defn view
  []
  [scan-sync-code/view
   {:title             (i18n/label :t/sign-in-by-syncing)
    :show-bottom-view? true
    :background        [background/view true]
    :animated?         false}])

(defn animated-view
  []
  [scan-sync-code/view
   {:title             (i18n/label :t/sign-in-by-syncing)
    :show-bottom-view? true
    :animated?         true}])
