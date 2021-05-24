(ns status-im.ui.screens.default-sync-period-settings.view
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.i18n.i18n :as i18n]
            [status-im.constants :as constants]
            [quo.core :as quo]
            [re-frame.core :as re-frame]))

(def titles {constants/one-day (i18n/label :t/one-day)
             constants/three-days (i18n/label :t/three-days)
             constants/one-week (i18n/label :t/one-week)
             constants/one-month (i18n/label :t/one-month)})

(defn radio-item [id value]
  [quo/list-item
   {:active    (= value id)
    :accessory :radio
    :title     (get titles id)
    :on-press  #(re-frame/dispatch [:multiaccounts.ui/default-sync-period-switched id])}])

(views/defview default-sync-period-settings []
  (views/letsubs [{:keys [default-sync-period]} [:multiaccount]]
    [react/view {:margin-top 8}
     [radio-item constants/one-day default-sync-period]
     [radio-item constants/three-days default-sync-period]
     [radio-item constants/one-week default-sync-period]
     [radio-item constants/one-month default-sync-period]]))
