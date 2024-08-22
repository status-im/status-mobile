(ns status-im.common.theme.events
  (:require
    [re-frame.core :as re-frame]
    [status-im.common.theme.core :as theme]
    [status-im.constants :as constants]))

(re-frame/reg-fx
 :theme/init-theme
 (fn []
   (theme/add-device-theme-change-listener)))

(defn- appearance-type->theme
  "Converts appearance type identifier to a theme keyword.
  Returns `:light` or `:dark` based on `appearance-type`:
  - `appearance-type-light` (1): Light theme.
  - `appearance-type-dark` (2): Dark theme.
  - `appearance-type-system` (0): Uses system preference."
  [appearance-type]
  (condp = appearance-type
    constants/appearance-type-dark   :dark
    constants/appearance-type-light  :light
    constants/appearance-type-system (if (theme/device-theme-dark?) :dark :light)
    :dark))

;; Switches the theme and triggers related effects.
;;
;; Parameters:
;; - `theme`: Optional theme keyword to apply. If not provided, defaults to the theme determined by
;;   `appearance-type`.
;; - `appearance-type`: Optional appearance type to determine the theme. If not provided, defaults to
;; the appearance type from the user's profile.
;; - `view-id`: Optional view ID for updating status and navigation color. If not provided, uses the
;; current view ID from the database. `view-id` is required because status and navigation bar colors
;; depends on the screen. For example, on the home screen, the navigation color should always be dark,
;; regardless of the theme, due to the bottom tabs.
(re-frame/reg-event-fx
 :theme/switch
 (fn [{db :db} [{:keys [theme view-id appearance-type]}]]
   (let [appearance-type (or appearance-type (get-in db [:profile/profile :appearance]))
         theme           (or theme (appearance-type->theme appearance-type))]
     {:db (assoc db :theme theme)
      :fx [[:theme/legacy-theme-fx theme]
           [:dispatch
            [:reload-status-nav-color
             (or view-id (:view-id db))]]]})))
