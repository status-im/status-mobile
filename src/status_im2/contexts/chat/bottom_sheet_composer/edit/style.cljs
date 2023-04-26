(ns status-im2.contexts.chat.bottom-sheet-composer.edit.style)

(def container
  {:flex-direction :row
   :height         24})

(def content-container
  {:padding-horizontal 10
   :flex               1
   :flex-direction     :row})

(def icon-container
  {:position :absolute
   :left     10
   :bottom   -4
   :width    16
   :height   16})

(def text-container
  {:position       :absolute
   :left           36
   :right          54
   :top            3
   :flex-direction :row
   :align-items    :center})

