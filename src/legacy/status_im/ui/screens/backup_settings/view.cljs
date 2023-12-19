(ns legacy.status-im.ui.screens.backup-settings.view
  (:require-macros [legacy.status-im.utils.views :as views])
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [re-frame.core :as re-frame]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]))

(defn perform-backup!
  []
  (re-frame/dispatch [:multiaccounts.ui/perform-backup-pressed]))

(defn footer
  [performing-backup?]
  [react/touchable-highlight
   {:on-press (when-not performing-backup?
                perform-backup!)
    :style    {:height 52}}
   [react/view
    {:style {:justify-content :center
             :flex            1
             :align-items     :center}}
    [react/text
     {:color      colors/blue
      :text-align :center}
     (if performing-backup?
       (i18n/label :t/backing-up)
       (i18n/label :t/perform-backup))]]])

(views/defview backup-settings
  []
  (views/letsubs
    [{:keys [last-backup backup-enabled?]}
     [:profile/profile]
     performing-backup? [:backup/performing-backup]]
    [:<>
     [react/scroll-view
      [list.item/list-item
       {:size :small
        :title (i18n/label :t/backup-through-waku)
        :accessibility-label :backup-enabled
        :container-margin-bottom 8
        :on-press
        #(re-frame/dispatch
          [:multiaccounts.ui/switch-backup-enabled (not backup-enabled?)])
        :accessory :switch
        :active backup-enabled?}]
      [list.item/list-item
       {:size      :small
        :title     (i18n/label :t/last-backup-performed)
        :accessory :text
        :subtitle  (if (pos? last-backup)
                     (datetime/time-ago-long (datetime/to-date (* 1000 last-backup)))
                     (i18n/label :t/never))}]]
     [footer performing-backup?]]))
