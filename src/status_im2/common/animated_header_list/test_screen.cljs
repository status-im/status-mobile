(ns status-im2.common.animated-header-list.test-screen
  (:require
   [quo2.core :as quo]
   [quo2.foundations.colors :as colors]
   [react-native.core :as rn]
   [react-native.fast-image :as fast-image]
   [status-im2.common.animated-header-list.view :as animated-header-list]))

;; THIS IS A TEMPORARY SCREEN. WILL REMOVE AFTER QA TESTING.

(def data [0 1 2 3 4 5 6 7 8 9 10])

(defn child
  []
  [rn/view
   {:style {:height           100
            :background-color colors/primary-50-opa-40
            :margin           10
            :justify-content  :center
            :padding-left     10}}
   [rn/text "This is some message!!!!!"]])


(defn main-comp
  []
  [rn/flat-list
   {:data      data
    :render-fn child
    :key-fn    (fn [item] (str item))}])

(defn display-picture-comp
  []
  [fast-image/fast-image
   {:style  {:width         72
             :height        72
             :border-radius 72}
    :source {:uri
             "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Cat03.jpg/1200px-Cat03.jpg"}}])

(defn title-comp
  []
  [rn/view
   {:style {:flex-direction  :row
            :justify-content :center
            :align-items     :center}}
   [fast-image/fast-image
    {:source {:uri
              "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Cat03.jpg/1200px-Cat03.jpg"}
     :style  {:width         32
              :height        32
              :border-radius 16
              :margin-right  8}}]
   [quo/text {:weight :semi-bold} "Alecia Keys"]])

(def theme-color (colors/theme-alpha "#5BCC95" 0.2 0.2))

(def parameters
  {:theme-color          theme-color
   :cover-uri
   "https://hips.hearstapps.com/hmg-prod.s3.amazonaws.com/images/kitten-playing-with-toy-mouse-royalty-free-image-590055188-1542816918.jpg?crop=1.00xw:0.758xh;0,0.132xh&resize=480:*"
   :display-picture-comp display-picture-comp
   :title-comp           title-comp
   :main-comp            main-comp})

(defn test-screen
  []
  [animated-header-list/animated-header-list parameters])


