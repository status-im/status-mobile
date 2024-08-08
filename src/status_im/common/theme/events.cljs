(ns status-im.common.theme.events
  (:require
    [re-frame.core :as re-frame]
    [status-im.common.theme.core :as theme]
    [status-im.common.theme.utils :as utils]))

(re-frame/reg-fx
 :theme/init-theme
 (fn []
   (theme/add-device-theme-change-listener)))

(re-frame/reg-event-fx
 :theme/switch
 (fn [{db :db} [{:keys [theme view-id theme-type]}]]
   (let [theme-type (or theme-type (get-in db [:profile/profile :appearance]))
         theme      (or theme (utils/theme-type->theme-value theme-type))]
     {:db (assoc db :theme theme)
      :fx [[:theme/legacy-theme-fx theme]
           [:dispatch
            [:reload-status-nav-color
             (or view-id (:view-id db))]]]})))
