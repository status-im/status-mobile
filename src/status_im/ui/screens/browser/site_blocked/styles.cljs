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

(def title-text
  {:typography    :title-bold
   :margin-top    26
   :margin-bottom 10
   :text-align    :center})

(def description-text
  {:color       colors/gray
   :text-align  :center})

(def chat-link-text
  {:color colors/blue})

(def buttons-container
  {:flex-direction  :row
   :justify-content :center
   :margin-top      24})
