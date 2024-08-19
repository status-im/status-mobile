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
   "Switches the theme and triggers related effects.
   Parameters:
   - theme: Optional theme keyword to apply. If not provided, defaults to the theme determined by theme-type.
   - theme-type: Optional theme type to determine the theme. If not provided, defaults to the theme type from the user's profile.
   - view-id: Optional view ID for updating status color. If not provided, uses the current view ID from the database."
   (let [theme-type (or theme-type (get-in db [:profile/profile :appearance]))
         theme      (or theme (utils/theme-type->theme-value theme-type))]
     {:db (assoc db :theme theme)
      :fx [[:theme/legacy-theme-fx theme]
           [:dispatch
            [:reload-status-nav-color
             (or view-id (:view-id db))]]]})))
