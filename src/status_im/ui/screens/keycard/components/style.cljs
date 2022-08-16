(ns status-im.ui.screens.keycard.components.style
  (:require [quo.design-system.colors :as colors]))

(def wrapper-style {:flex            1
                    :align-items     :center
                    :justify-content :center})

(def container-style {:flex-direction  :column
                      :align-items     :center
                      :padding-horizontal 40})

(def helper-text-style {:text-align  :center
                        :color       colors/gray
                        :line-height 22})

(def title-style {:text-align  :center
                  :line-height 22})
