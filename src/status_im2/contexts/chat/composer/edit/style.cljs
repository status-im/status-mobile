(ns status-im2.contexts.chat.composer.edit.style)

(def container
  {:flex-direction :row
   :height         24})

(def content-container
  {:flex           1
   :flex-direction :row})

(def icon-container
  {:position :absolute
   :left     0
   :bottom   -4
   :width    16
   :height   16})

(def text-container
  {:position       :absolute
   :left           24
   :top            3
   :flex-direction :row
   :align-items    :center})
