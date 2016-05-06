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
            [syng-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               chat-background
                                               online-color
                                               selected-message-color
                                               text1-color
                                               text2-color
                                               text3-color]]))

(defonce drawer-atom (atom))

(defn open-drawer []
  (.openDrawer @drawer-atom))

(defn user-photo [{:keys [photo-path]}]
  [view {:borderRadius 50}
   [image {:source (if (s/blank? photo-path)
                     res/user-no-photo
                     {:uri photo-path})
           :style  {:borderRadius 50
                    :width        64
                    :height       64}}]])

(defn menu-item [{:keys [name handler]}]
  [touchable-opacity {:style {:height      48
                              :paddingLeft 16
                              :paddingTop  14}
                      :onPress handler}
   [text {:style {:fontSize   14
                  :fontFamily font
                  :lineHeight 21
                  :color      text1-color}}
    name]])

(defn drawer-menu [navigator]
  [view {:style {:flex            1
                 :backgroundColor color-white
                 :flexDirection   :column}}
   [view {:style {:marginTop      40
                  :alignItems     :center
                  :justifyContent :center}}
    [user-photo {}]]
   [view {:style {:marginTop  20
                  :alignItems :center}}
    [text {:style {:marginTop -2.5
                   :color     text1-color
                   :fontSize  16}}
     "Status"]]
   [view {:style {:flex          1
                  :marginTop     80
                  :alignItems    :stretch
                  :flexDirection :column}}
    [menu-item {:name    "Profile"
                :handler (fn []
                           (dispatch [:show-profile navigator]))}]
    [menu-item {:name    "Settings"
                :handler (fn [])}]
    [menu-item {:name    "Invite friends"
                :handler (fn [])}]
    [menu-item {:name    "FAQ"
                :handler (fn [])}]]
   [view {:style {:paddingVertical 36
                  :alignItems :center}}
    [touchable-opacity {}
     [text {:style {:fontSize   14
                    :fontFamily font
                    :lineHeight 21
                    :color      text3-color}}
      "Switch users"]]]])

(defn drawer-view [{:keys [navigator]} items]
  [drawer-layout-android {:drawerWidth            300
                          :drawerPosition         js/React.DrawerLayoutAndroid.positions.Left
                          :render-navigation-view #(r/as-element [drawer-menu navigator])
                          :ref  (fn [drawer]
                                  (reset! drawer-atom drawer))}
   items])
