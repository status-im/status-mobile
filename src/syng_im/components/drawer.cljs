(ns syng-im.components.drawer
  (:require [clojure.string :as s]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [reagent.core :as r]
            [syng-im.components.react :refer [android?
                                              view
                                              text
                                              image
                                              navigator
                                              toolbar-android
                                              drawer-layout-android
                                              touchable-opacity]]
            [syng-im.resources :as res]
            [syng-im.components.drawer-styles :as st]))

(defonce drawer-atom (atom))

(defn open-drawer []
  (.openDrawer @drawer-atom))

(defn close-drawer []
  (.closeDrawer @drawer-atom))

(defn user-photo [{:keys [photo-path]}]
  [image {:source (if (s/blank? photo-path)
                    res/user-no-photo
                    {:uri photo-path})
          :style  st/user-photo}])

(defn menu-item [{:keys [name handler]}]
  [touchable-opacity {:style st/menu-item-touchable
                      :onPress (fn []
                                 (close-drawer)
                                 (handler))}
   [text {:style st/menu-item-text}
    name]])

(defn drawer-menu []
  (let [username (subscribe [:get :username])]
    (fn []
      [view st/drawer-menu
      [view st/user-photo-container
       [user-photo {}]]
      [view st/name-container
       [text {:style st/name-text}
        @username]]
      [view st/menu-items-container
       [menu-item {:name    "Profile"
                   :handler (fn []
                              (dispatch [:show-my-profile]))}]
       [menu-item {:name    "Settings"
                   :handler (fn []
                              ;; TODO not implemented
                              )}]
       [menu-item {:name    "Discovery"
                   :handler (fn []
                              (dispatch [:navigate-to :discovery]))}]
       [menu-item {:name    "Contacts"
                   :handler (fn []
                              (dispatch [:show-contacts navigator]))}]
       [menu-item {:name    "Invite friends"
                   :handler (fn []
                              ;; TODO not implemented
                              )}]
       [menu-item {:name    "FAQ"
                   :handler (fn [])}]]
      [view st/switch-users-container
       [touchable-opacity {:onPress (fn []
                                      (close-drawer)
                                      ;; TODO not implemented
                                      )}
        [text {:style st/switch-users-text}
         "Switch users"]]]])))

(defn drawer-view [items]
  [drawer-layout-android {:drawerWidth            260
                          :drawerPosition         js/React.DrawerLayoutAndroid.positions.Left
                          :render-navigation-view #(r/as-element [drawer-menu])
                          :ref  (fn [drawer]
                                  (reset! drawer-atom drawer))}
   items])
