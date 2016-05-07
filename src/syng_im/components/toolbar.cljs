(ns syng-im.components.toolbar
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.resources :as res]
            [syng-im.components.react :refer [view
                                              text-input
                                              text
                                              image
                                              touchable-highlight]]
            [syng-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               color-purple
                                               text1-color
                                               text2-color
                                               toolbar-background1]]
            [syng-im.components.realm :refer [list-view]]
            [syng-im.utils.listview :refer [to-realm-datasource]]
            [reagent.core :as r]
            [syng-im.navigation :refer [nav-pop]]))

(defn toolbar [{:keys [navigator title nav-action action background-color]}]
  [view {:style {:flexDirection   "row"
                 :backgroundColor (or background-color toolbar-background1)
                 :height          56
                 :elevation       2}}
   (if nav-action
     [touchable-highlight {:on-press (:handler nav-action)}
      [view {:width          56
             :height         56
             :alignItems     "center"
             :justifyContent "center"}
       [image (:image nav-action)]]]
     [touchable-highlight {:on-press #(nav-pop navigator)}
      [view {:width  56
             :height 56}
       [image {:source {:uri "icon_back"}
               :style  {:marginTop  21
                        :marginLeft 23
                        :width      8
                        :height     14}}]]])
   [view {:style {:flex           1
                  :alignItems     "center"
                  :justifyContent "center"}}
    [text {:style {:marginTop  -2.5
                   :color      text1-color
                   :fontSize   16
                   :fontFamily font}}
     title]]
   [touchable-highlight {:on-press (:handler action)}
    [view {:width          56
           :height         56
           :alignItems     "center"
           :justifyContent "center"}
     [image (:image action)]]]])
