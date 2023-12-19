(ns legacy.status-im.ui.screens.default-sync-period-settings.view
  (:require-macros [legacy.status-im.utils.views :as views])
  (:require
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [re-frame.core :as re-frame]
    [status-im2.config :as config]
    [status-im2.constants :as constants]
    [utils.i18n :as i18n]))

(def titles
  {constants/two-mins   (i18n/label :t/two-minutes)
   constants/one-day    (i18n/label :t/one-day)
   constants/three-days (i18n/label :t/three-days)
   constants/one-week   (i18n/label :t/one-week)
   constants/one-month  (i18n/label :t/one-month)})

(defn radio-item
  [id value]
  [list.item/list-item
   {:active    (= value id)
    :accessory :radio
    :title     (get titles id)
    :on-press  #(re-frame/dispatch [:profile.settings/update-value :default-sync-period id])}])

(views/defview default-sync-period-settings
  []
  (views/letsubs [{:keys [default-sync-period]
                   :or   {default-sync-period constants/one-day}}
                  [:profile/profile]]
    [react/view {:margin-top 8}
     (when config/two-minutes-syncing?
       [radio-item constants/two-mins default-sync-period])
     [radio-item constants/one-day default-sync-period]
     [radio-item constants/three-days default-sync-period]
     [radio-item constants/one-week default-sync-period]
     [radio-item constants/one-month default-sync-period]]))
