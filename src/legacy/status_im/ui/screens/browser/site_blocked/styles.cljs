(ns legacy.status-im.ui.screens.browser.site-blocked.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]))

(def container
  {:justify-content :center
   :flex            1})

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
  {:color      colors/gray
   :text-align :center})

(def chat-link-text
  {:color colors/blue})

(def buttons-container
  {:margin 24})
