(ns status-im.ui.screens.browser.site-blocked.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def container
  {:justify-content  :center
   :flex             1
   :background-color colors/gray-lighter})

(def container-root-view
  {:flex            1
   :margin          24
   :justify-content :center
   :align-items     :center})

(defstyle title-text
  {:color         colors/black
   :font-size     17
   :font-weight   :bold
   :margin-top    26
   :margin-bottom 10
   :line-height   20
   :text-align    :center
   :ios           {:letter-spacing -0.4}})

(defstyle description-text
  {:color       colors/gray
   :font-size   15
   :line-height 22
   :text-align  :center
   :ios         {:letter-spacing -0.2}})

(def chat-link-text
  {:color colors/blue})

(def buttons-container
  {:flex-direction  :row
   :justify-content :center
   :margin-top      24})