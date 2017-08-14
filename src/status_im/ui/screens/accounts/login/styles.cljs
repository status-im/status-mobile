(ns status-im.ui.screens.accounts.login.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.components.styles :as common]))

(defstyle login-view
  {:flex              1
   :ios               {:margin-top    10
                       :margin-bottom 10}
   :android           {:margin-top    16
                       :margin-bottom 16}
   :margin-horizontal 16})

(defstyle login-badge-container
  {:background-color :white
   :ios              {:border-radius 8
                      :padding-top   16
                      :height        150}
   :android          {:border-radius 4
                      :padding-top   12
                      :height        144}})

(defstyle sign-it-text
  {:color   :white
   :ios     {:font-size 17
             :letter-spacing -0.2}
   :android {:font-size 16}})

(defstyle sign-in-button
  {:background-color common/color-blue3
   :align-items      :center
   :justify-content  :center
   :height           52
   :ios              {:border-radius 8}
   :android          {:border-radius 4}})

(def processing-view
  {:position         :absolute
   :top              0
   :bottom           0
   :right            0
   :left             0
   :align-items      :center
   :justify-content  :center
   :background-color (str common/color-black "1A")})
