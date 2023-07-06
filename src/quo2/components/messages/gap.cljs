(ns quo2.components.messages.gap
  (:require [oops.core :refer [oget]]
            [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]))

;;; helpers
(def themes
  {:light {:icon       colors/neutral-40
           :time       colors/neutral-50
           :background colors/neutral-5}
   :dark  {:icon       colors/neutral-60
           :time       colors/neutral-40
           :background colors/neutral-95}})

(defn get-color
  [k]
  (get-in themes [(theme/get-theme) k]))

(def ui-images
  {:light {:horizontal (js/require "../resources/images/ui/message-gap-hborder-light.png")
           :vertical   (js/require "../resources/images/ui/message-gap-vborder-light.png")
           :circles    (js/require "../resources/images/ui/message-gap-circle-bg-light.png")}
   :dark  {:horizontal (js/require "../resources/images/ui/message-gap-hborder-dark.png")
           :circles    (js/require "../resources/images/ui/message-gap-circle-bg-dark.png")}})

(defn get-image
  [k]
  (get-in ui-images [(theme/get-theme) k]))

;;; components
;;;; borders
(defn hborder
  [{:keys [type style]}]
  [rn/image
   {:source      (get-image :horizontal)
    :resize-mode :repeat
    :style       (merge {:position           :absolute
                         :left               0
                         :padding-horizontal 4
                         :overflow           :hidden
                         :width              "110%"
                         :height             8
                         :margin-left        -4}
                        (if (= type :top)
                          {:top 0}
                          {:transform [{:rotateZ "180deg"}]
                           :bottom    0})
                        style)}])

(defn vborder
  [type body-height]
  (let [height @body-height
        img    (get-image :vertical)]
    (when (and img height)
      [rn/image
       {:source      img
        :resize-mode :repeat
        :style       (merge
                      {:position :absolute
                       :top      4
                       :height   (- height 8)
                       :width    4}
                      (if (= type :left)
                        {:left 0}
                        {:transform [{:rotate "180deg"}]
                         :right     0}))}])))

;;;; others
(defn circle
  []
  [rn/view
   {:width         9
    :height        9
    :border-width  1
    :margin        4
    :flex          0
    :border-color  (get-color :icon)
    :border-radius 50}])

(defn timestamp
  [str]
  [text/text
   {:size  :label
    :style {:text-transform :none
            :color          (get-color :time)}} str])

(defn info-button
  [on-press]
  [rn/touchable-without-feedback
   {:on-press on-press}
   [icon/icon "message-gap-info" {:size 12 :no-color true :container-style {:padding 4}}]])

;;;; timeline/body
(defn timeline
  []
  [rn/view
   {:flex            0
    :margin-right    20
    :align-items     :center
    :width           9
    :justify-content :space-between}
   [circle]
   [rn/image {:style {:flex 1} :source (get-image :circles) :resize-mode :repeat}]
   [circle]])

(defn body
  [timestamp-far timestamp-near on-info-button-pressed on-press warning-label]
  [rn/view {:flex 1}
   [rn/view
    {:flex-direction  :row
     :align-items     :center
     :justify-content :space-between
     :margin-right    2}
    [timestamp timestamp-far]
    (when on-info-button-pressed [info-button on-info-button-pressed])]

   [rn/touchable-without-feedback
    {:style    {:flex 1 :margin-top 16 :margin-bottom 20}
     :on-press #(when on-press (on-press))}
    [text/text warning-label]]

   [timestamp timestamp-near]])

;;; main
(defn gap
  "if `gap-ids` and `chat-id` are provided, press the main text area to fetch messages
  if `on-info-button-pressed` fn is provided, the info button will show up and is pressable"
  [{:keys [timestamp-far
           timestamp-near
           on-info-button-pressed
           style
           on-press
           warning-label]}]
  (let [body-height (reagent/atom nil)]
    (fn []
      [rn/view
       {:on-layout #(reset! body-height (oget % "nativeEvent.layout.height"))
        :overflow  :hidden
        :flex      1}
       [hborder {:type :top}]
       [hborder {:type :bottom}]
       [rn/view
        (merge {:width            "100%"
                :background-color (get-color :background)
                :flex-direction   :row
                :padding          20
                :padding-left     31
                :margin-vertical  4}
               style)

        [timeline]
        [body timestamp-far timestamp-near on-info-button-pressed on-press warning-label]]
       [vborder :left body-height]
       [vborder :right body-height]])))
