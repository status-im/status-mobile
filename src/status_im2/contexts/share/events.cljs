(ns status-im2.contexts.share.events
  (:require [utils.re-frame :as rf]
            [status-im2.common.toasts.events :as toasts]
            [quo2.foundations.colors :as colors]
            ;;TODO(siddarthkay) : move the components below over to status-im2 ns
            ;; issue -> https://github.com/status-im/status-mobile/issues/15549
            [status-im.ui.components.react :as react]))

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

(rf/defn copy-text-and-show-toast
  {:events [:share/copy-text-and-show-toast]}
  [cofx text-to-copy post-copy-message]
  (react/copy-to-clipboard text-to-copy)
  (toasts/upsert cofx
                 {:icon           :correct
                  :icon-color     colors/success-50
                  :override-theme :dark
                  :text           post-copy-message}))
