(ns status-im.switcher.switcher
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [status-im.switcher.styles :as styles]
            [status-im.ui.components.animation :as anim]
            [status-im.switcher.animation :as animation]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.react-native.resources :as resources]
            [status-im.switcher.switcher-container :as switcher-container]))

(defn toggle-switcher-screen [switcher-opened? view-id anim-values]
  (swap! switcher-opened? not)
  (animation/animate @switcher-opened? view-id anim-values))

(defn switcher-button [switcher-opened? view-id anim-values]
  [rn/touchable-opacity {:active-opacity 1
                         :on-press       #(toggle-switcher-screen switcher-opened? view-id anim-values)
                         :style          (styles/switcher-button-touchable view-id)}
   [rn/animated-view {:style (styles/switcher-close-button-background
                              (:switcher-close-button-background-opacity anim-values))}]
   [rn/animated-view {:style (styles/switcher-close-button-icon
                              (:switcher-close-button-icon-opacity anim-values))}
    [icons/icon :main-icons/close {:color :white}]]
   [rn/animated-image-view {:source (resources/get-image :status-logo)
                            :style  (styles/switcher-button
                                     (:switcher-button-opacity anim-values))}]])

(defn switcher-screen [switcher-opened? view-id anim-values]
  [rn/view {:style          (styles/switcher-screen
                             view-id @(:switcher-screen-radius anim-values))
            :pointer-events (if switcher-opened? :auto :none)}
   [switcher-container/container
    view-id @(:switcher-screen-radius anim-values)
    #(toggle-switcher-screen switcher-opened? view-id anim-values)]])

(defn switcher [view-id]
  (let [switcher-opened? (reagent/atom false)
        anim-values      {:switcher-button-opacity                  (anim/create-value 1)
                          :switcher-close-button-icon-opacity       (anim/create-value 0)
                          :switcher-close-button-background-opacity (anim/create-value 0)
                          :switcher-screen-radius                   (reagent/atom 1)}]
    [:<>
     [switcher-screen switcher-opened? view-id anim-values]
     [switcher-button switcher-opened? view-id anim-values]]))
