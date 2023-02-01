(ns status-im2.contexts.share.events
  (:require [utils.re-frame :as rf]
            [status-im2.common.toasts.events :as toasts]
            [quo2.foundations.colors :as colors]))

(rf/defn open-profile-share-view
  {:events [:share/open]}
  [{:keys [db]}]
  {:dispatch [:show-popover
              {:view                       :profile-share
               :style                      {:margin 0}
               :disable-touchable-overlay? true
               :blur-view?                 true
               :blur-view-props            {:blur-amount 20
                                            :blur-type   :dark}}]})

(rf/defn show-successfully-copied-toast
  {:events [:share/show-successfully-copied-toast]}
  [cofx toast-label]
  (toasts/upsert cofx
                 {:icon           :correct
                  :icon-color     colors/success-50
                  :override-theme :dark
                  :text           toast-label}))
