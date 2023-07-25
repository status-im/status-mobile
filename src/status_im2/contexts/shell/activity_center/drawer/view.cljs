(ns status-im2.contexts.shell.activity-center.drawer.view
  (:require [utils.re-frame :as rf]
            [quo2.core :as quo]
            [utils.i18n :as i18n]
            [quo2.foundations.colors :as colors]))

(defn options
  []
  (let [unread-count (rf/sub [:activity-center/unread-count])]
    [quo/action-drawer
     [[{:icon      :i/mark-as-read
        :label     (i18n/label :t/mark-all-notifications-as-read)
        :disabled? (zero? unread-count)
        :on-press  (fn []
                     (rf/dispatch [:activity-center.notifications/mark-all-as-read-locally
                                   (fn []
                                     {:icon       :up-to-date
                                      :icon-color colors/success-50
                                      :text       (i18n/label :t/notifications-marked-as-read
                                                              {:count unread-count})
                                      :theme      :dark})])
                     (rf/dispatch [:hide-bottom-sheet]))}]]]))
