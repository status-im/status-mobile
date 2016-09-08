(ns status-im.components.drawer.view
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [clojure.string :as s]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [reagent.core :as r]
            [status-im.components.react :refer [react-native
                                                view
                                                text
                                                image
                                                drawer-layout-android
                                                touchable-opacity]]
            [status-im.resources :as res]
            [status-im.components.drawer.styles :as st]
            [status-im.i18n :refer [label]]))

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

(defn menu-item [{:keys [name handler platform-specific]}]
  [touchable-opacity {:style   st/menu-item-touchable
                      :onPress (fn []
                                 (close-drawer)
                                 (handler))}
   [text {:style             st/menu-item-text
          :platform-specific platform-specific
          :font              :default}
    name]])

(defview drawer-menu [{platform-specific :platform-specific}]
  [{:keys [name address photo-path]} [:get-current-account]]
  [view st/drawer-menu
   [view st/user-photo-container
    [user-photo {:photo-path photo-path}]]
   [view st/name-container
    [text {:style             st/name-text
           :platform-specific platform-specific
           :number-of-lines   1
           :font              :default}
     (if (= name address)
       (label :t/user-anonymous)
       name)]]
   [view st/menu-items-container
    [menu-item {:name              (label :t/profile)
                :handler           #(dispatch [:navigate-to :my-profile])
                :platform-specific platform-specific}]
    [menu-item {:name              (label :t/settings)
                :handler           (fn []
                                     ;; TODO not implemented
                                     )
                :platform-specific platform-specific}]
    [menu-item {:name              (label :t/discovery)
                :handler           #(dispatch [:navigate-to :discovery])
                :platform-specific platform-specific}]
    [menu-item {:name              (label :t/contacts)
                :handler           #(dispatch [:navigate-to :contact-list])
                :platform-specific platform-specific}]
    [menu-item {:name              (label :t/invite-friends)
                :handler           (fn []
                                     ;; TODO not implemented
                                     )
                :platform-specific platform-specific}]
    [menu-item {:name              (label :t/faq)
                :handler           (fn [])
                :platform-specific platform-specific}]]
   [view st/switch-users-container
    [touchable-opacity {:onPress (fn []
                                   (close-drawer)
                                   (dispatch [:navigate-to :accounts])
                                   ;; TODO not implemented
                                   )}
     [text {:style             st/switch-users-text
            :platform-specific platform-specific
            :font              :default}
      (label :t/switch-users)]]]])

(defn drawer-view [opts items]
  [drawer-layout-android {:drawerWidth            260
                          :drawerPosition         js/ReactNative.DrawerLayoutAndroid.positions.Left
                          :render-navigation-view #(r/as-element [drawer-menu opts])
                          :ref                    (fn [drawer]
                                                    (reset! drawer-atom drawer))}
   items])
