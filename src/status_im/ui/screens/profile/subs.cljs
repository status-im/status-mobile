(ns status-im.ui.screens.profile.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [clojure.string :as string]
            [status-im.ui.screens.profile.db :refer [account-profile-keys]]))

(defn get-in-profile [profile current-account k]
  (or (get profile k)
      (get current-account k)))

(reg-sub :my-profile/get
  :<- [:get-current-account]
  :<- [:get :my-profile/profile]
  (fn [[current-account profile] [_ k]]
    (get-in-profile profile current-account k)))

(reg-sub :my-profile.drawer/get
  :<- [:get-current-account]
  :<- [:get :my-profile/drawer]
  (fn [[current-account profile] [_ k]]
    (get-in-profile profile current-account k)))

(reg-sub :my-profile/valid-name?
  :<- [:get :my-profile/profile]
  (fn [profile]
    (get profile :valid-name? true)))

(reg-sub :my-profile.drawer/valid-name?
  :<- [:get :my-profile/drawer]
  (fn [profile]
    (or (string/blank? (:name profile))
        (get profile :valid-name? true))))

(reg-sub :my-profile/changed?
  :<- [:get-current-account]
  :<- [:get :my-profile/profile]
  (fn [[current-account profile]]
    (not= (select-keys current-account account-profile-keys)
          (select-keys profile account-profile-keys))))

(reg-sub :my-profile.drawer/save-event
  :<- [:get-current-account]
  :<- [:get :my-profile/drawer]
  (fn [[{:keys [name status]} {new-name :name new-status :status}]]
    (cond
      (and new-name
           (not= name new-name))
      :my-profile.drawer/save-name

      (and status
           (not= status new-status))
      :my-profile.drawer/save-status)))
