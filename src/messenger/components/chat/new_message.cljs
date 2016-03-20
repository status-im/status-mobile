(ns messenger.components.chat.new-message
  (:require-macros
    [natal-shell.components :refer [view image text-input]])
  (:require [om.next :as om :refer-macros [defui]]
            [messenger.utils.resources :as res]))

(defui NewMessage
  Object
  (render
    [this]
    (view {:style {:flexDirection   "row"
                   :margin          10
                   :height          40
                   :backgroundColor "#E5F5F6"
                   :borderRadius    5}}
          (image {:source res/mic
                  :style  {:marginTop  11
                           :marginLeft 14
                           :width      13
                           :height     20}})
          (text-input {:underlineColorAndroid "#9CBFC0"
                       :style                 {:flex       1
                                               :marginLeft 18
                                               :lineHeight 42
                                               :fontSize   14
                                               :fontFamily "Avenir-Roman"
                                               :color      "#9CBFC0"}}
                      "Your message")
          (image {:source res/smile
                  :style  {:marginTop   11
                           :marginRight 12
                           :width       18
                           :height      18}})
          (image {:source res/att
                  :style  {:marginTop   14
                           :marginRight 16
                           :width       17
                           :height      14}}))))

(def new-message (om/factory NewMessage))
