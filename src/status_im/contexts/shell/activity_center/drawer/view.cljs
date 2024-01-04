(ns status-im.contexts.shell.activity-center.drawer.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

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
                     (rf/dispatch [:hide-bottom-sheet]))}
       {:icon     :i/settings
        :label    (i18n/label :t/chat-notification-preferences)
        :on-press (fn []
                    (rf/dispatch [:navigate-to :notifications-settings])
                    (rf/dispatch [:hide-bottom-sheet]))}]]]))
