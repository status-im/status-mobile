(ns quo2.components.avatars.user-avatar.view
  (:require [clojure.string :as string]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :refer [dark?]]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]))

(def sizes
  {:big    {:outer                   80
            :inner                   72
            :status-indicator        20
            :status-indicator-border 4
            :font-size               :heading-1}
   :medium {:outer                   48
            :inner                   44
            :status-indicator        12
            :status-indicator-border 2
            :font-size               :paragraph-1}
   :small  {:outer                   32
            :inner                   28
            :status-indicator        12
            :status-indicator-border 2
            :font-size               :paragraph-2}
   :xs     {:outer                   24
            :inner                   24
            :status-indicator        0
            :status-indicator-border 0
            :font-size               :paragraph-2}
   :xxs    {:outer                   20
            :inner                   20
            :status-indicator        0
            :status-indicator-border 0
            :font-size               :label}
   :xxxs   {:outer                   16
            :inner                   16
            :status-indicator        0
            :status-indicator-border 0
            :font-size               :label}})

(defn dot-indicator
  [{:keys [size online? ring? dark?]}]
  (let [dimensions   (get-in sizes [size :status-indicator])
        border-width (get-in sizes [size :status-indicator-border])
        right        (case size
                       :big    2
                       :medium 0
                       :small  -2
                       0)
        bottom       (case size
                       :big    (if ring? -1 2)
                       :medium (if ring? 0 -2)
                       :small  -2
                       0)]
    [rn/view
     {:style {:background-color (if online?
                                  colors/success-50
                                  colors/neutral-40)
              :width            dimensions
              :height           dimensions
              :border-width     border-width
              :border-radius    dimensions
              :border-color     (if dark?
                                  colors/neutral-100
                                  colors/white)
              :position         :absolute
              :bottom           bottom
              :right            right}}]))

(defn initials-style
  [inner-dimensions outer-dimensions]
  {:position         :absolute
   :top              (/ (- outer-dimensions inner-dimensions) 2)
   :left             (/ (- outer-dimensions inner-dimensions) 2)
   :width            inner-dimensions
   :height           inner-dimensions
   :border-radius    inner-dimensions
   :justify-content  :center
   :align-items      :center
   :background-color (colors/custom-color-by-theme :turquoise 50 60)})

(defn outer-styles
  [outer-dimensions]
  {:width         outer-dimensions
   :height        outer-dimensions
   :border-radius outer-dimensions})

(def one-initial-letter-sizes #{:xs :xxs :xxxs})
(def valid-ring-sizes #{:big :medium :small})

(defn initials-avatar
  [{:keys [full-name size inner-dimensions outer-dimensions]}]
  (let [amount-initials (if (one-initial-letter-sizes size) 1 2)
        initials        (as-> full-name $
                          (string/split $ " ")
                          (map (comp string/upper-case first) $)
                          (take amount-initials $)
                          (string/join $))
        font-size       (get-in sizes [size :font-size])]
    [rn/view {:style (initials-style inner-dimensions outer-dimensions)}
     [text/text
      {:style  {:color colors/white-opa-70}
       :weight :semi-bold
       :size   font-size}
      initials]]))

(defn user-avatar
  "If no `profile-picture` is given, draws the initials based on the `full-name` and
  uses `ring-background` to display the ring behind the initials when given. Otherwise,
  shows the profile picture which already comes with the ring drawn over it."
  [{:keys [full-name status-indicator? online? size profile-picture ring-background]
    :or   {status-indicator? true
           online?           true
           size              :big}}]
  (let [full-name        (or full-name "empty name")
        draw-ring?       (and ring-background (valid-ring-sizes size))
        outer-dimensions (get-in sizes [size :outer])
        inner-dimensions (get-in sizes [size (if draw-ring? :inner :outer)])]
    [rn/view
     {:style               (outer-styles outer-dimensions)
      :accessibility-label :user-avatar}
     ;; The `profile-picture` already has the ring in it
     (when-let [image (or profile-picture ring-background)]
       [fast-image/fast-image
        {:style  (outer-styles outer-dimensions)
         :source image}])
     (when-not profile-picture
       [initials-avatar
        {:full-name        full-name
         :size             size
         :inner-dimensions inner-dimensions
         :outer-dimensions outer-dimensions}])
     (when status-indicator?
       [dot-indicator
        {:size    size
         :online? online?
         :ring?   draw-ring?
         :dark?   (dark?)}])]))
