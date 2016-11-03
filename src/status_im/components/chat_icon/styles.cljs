(ns status-im.components.chat-icon.styles
  (:require [status-im.components.styles :refer [color-white
                                                 online-color]]))

(defn default-chat-icon [color]
  {:margin          4
   :width           40
   :height          40
   :alignItems      :center
   :justifyContent  :center
   :borderRadius    20
   :backgroundColor color})

(defn default-chat-icon-chat-list [color]
  (merge (default-chat-icon color)
         {:width         40
          :height        40
          :border-radius 20}))

(defn default-chat-icon-menu-item [color]
  (merge (default-chat-icon color)
         {:width         24
          :height        24
          :border-radius 12}))

(defn default-chat-icon-profile [color]
  (merge (default-chat-icon color)
         {:width         64
          :height        64
          :border-radius 32}))

(defn default-chat-icon-view-action [color]
  (merge (default-chat-icon color)
         {:width         36
          :height        36
          :border-radius 18}))

(defn default-chat-icon-message-status [color]
  (merge (default-chat-icon color)
         {:width         64
          :height        64
          :border-radius 32}))

(def default-chat-icon-text
  {:marginTop  -2
   :color      color-white
   :fontSize   16
   :lineHeight 20})

(def message-status-icon-text
  {:marginTop -2
   :color     color-white
   :fontSize  24})

(def chat-icon
  {:margin        4
   :border-radius 20
   :width         40
   :height        40})

(def chat-icon-chat-list
  (merge chat-icon
         {:width  40
          :height 40
          :margin 0}))

(def chat-icon-menu-item
  {:width         24
   :height        24
   :border-radius 12})

(def chat-icon-profile
  (merge chat-icon
         {:width         64
          :height        64
          :border-radius 32}))

(def chat-icon-view-action
  (merge chat-icon
         {:width         36
          :height        36
          :border-radius 18
          :margin        0}))

(def chat-icon-message-status
  {:border-radius 32
   :width         64
   :height        64})

(def online-view-wrapper
  {:position         :absolute
   :bottom           -1
   :right            0
   :width            22
   :height           22
   :border-radius    11
   :background-color :white})

(def online-view
  {:position         :absolute
   :bottom           2
   :right            2
   :width            18
   :height           18
   :border-radius    9
   :background-color online-color})

(def online-view-menu-item
  (merge online-view
         {:width         14
          :height        14
          :border-radius 7}))

(def online-view-profile
  (merge online-view
         {:width         24
          :height        24
          :border-radius 12}))

(def online-dot
  {:position         :absolute
   :top              7
   :width            4
   :height           4
   :border-radius    2
   :background-color color-white})
(def online-dot-left (merge online-dot {:left 4}))
(def online-dot-right (merge online-dot {:left 10}))

(def photo-pencil
  {:margin-left  8
   :margin-right 2
   :margin-top   6
   :font-size    12
   :color        :white})

(def online-dot-menu-item
  (merge online-dot
         {:top    4
          :width  3
          :height 3}))
(def online-dot-left-menu-item
  (merge online-dot-menu-item {:left 1.7}))
(def online-dot-right-menu-item
  (merge online-dot-menu-item {:left 6.3}))

(def online-dot-profile
  (merge online-dot
         {:top    8
          :width  4
          :height 4}))
(def online-dot-left-profile
  (merge online-dot-profile {:left 5}))
(def online-dot-right-profile
  (merge online-dot-profile {:left 11}))

(def container
  {:width  44
   :height 44})

(def container-chat-list
  {:width  48
   :height 48})

(def container-menu-item
  {:width  32
   :height 32})

(def container-profile
  {:width  72
   :height 72})

(def container-message-status
  {:margin-top 20})

(def default-image-style
  {:margin 4})

(defn border-style [size]
  {:width            size
   :height           size
   :border-radius    (/ size 2)
   :background-color :#b9c8d6
   :padding          0.5})

(defn image-style [size]
  (let [image-size (dec size)]
    {:width         image-size
     :height        image-size
     :border-radius (/ image-size 2)}))
