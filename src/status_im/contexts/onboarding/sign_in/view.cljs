(ns status-im.contexts.onboarding.sign-in.view
  (:require
    [status-im.contexts.onboarding.common.background.view :as background]
    [status-im.contexts.syncing.scan-sync-code.view :as scan-sync-code]
    [utils.i18n :as i18n]))

(defn view
  []
  [scan-sync-code/view
   {:title             (i18n/label :t/sign-in-by-syncing)
    :background        [background/view true]
    :animated?         false
    :screen-name       "sign-in"}])

(defn animated-view
  []
  [scan-sync-code/view
   {:title             (i18n/label :t/sign-in-by-syncing)
    :animated?         true
    :screen-name       "sign-in-intro"}])
