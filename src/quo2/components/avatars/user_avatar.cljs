(ns quo2.components.avatars.user-avatar
  (:require [clojure.string :refer [blank? split upper-case]]
            [quo2.components.icon :as icons]
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
  [size status-indicator? online? ring? dark?]
  (when status-indicator?
    (let [dimensions   (get-in sizes [size :status-indicator])
          border-width (get-in sizes [size :status-indicator-border])
          right        (case size
                         :big    2
                         :medium 0
                         :small  -2
                         0)
          bottom       (case size
                         :big    (if ring?
                                   -1
                                   2)
                         :medium (if ring?
                                   0
                                   -2)
                         :small  (if ring?
                                   -2
                                   -2)
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
                :right            right}}])))

(defn container-styling
  [inner-dimensions outer-dimensions]
  {:width         inner-dimensions
   :position      :absolute
   :top           (/ (- outer-dimensions inner-dimensions) 2)
   :left          (/ (- outer-dimensions inner-dimensions) 2)
   :height        inner-dimensions
   :border-radius inner-dimensions})

(defn container
  [inner-dimensions outer-dimensions & children]
  [rn/view
   {:style (merge {:background-color (colors/custom-color-by-theme :turquoise 50 60)
                   :justify-content  :center
                   :align-items      :center}
                  (container-styling inner-dimensions outer-dimensions))}
   children])

(def small-sizes #{:xs :xxs :xxxs})
(def identicon-sizes #{:big :medium :small})

(defn user-avatar
  [{:keys [ring?
           online?
           size
           status-indicator?
           profile-picture
           full-name]
    :or   {full-name         "empty name"
           status-indicator? true
           online?           true
           size              :big
           ring?             true}}]
  (let [initials             (if full-name
                               (reduce str (map first (split full-name " ")))
                               "")
        first-initial-letter (if full-name
                               (or (first full-name) "")
                               "")
        identicon?           (contains? identicon-sizes size)
        small?               (contains? small-sizes size)
        outer-dimensions     (get-in sizes [size :outer])
        inner-dimensions     (get-in sizes
                                     [size
                                      (if ring?
                                        :inner
                                        :outer)])
        font-size            (get-in sizes [size :font-size])
        icon-text            (if-not (or (blank? first-initial-letter)
                                         (blank? initials))
                               (if small?
                                 first-initial-letter
                                 initials)
                               "")]
    [rn/view
     {:accessibility-label :user-avatar
      :style               {:width         outer-dimensions
                            :height        outer-dimensions
                            :border-radius outer-dimensions}}
     (when (and ring? identicon?)
       [icons/icon :i/identicon-ring
        {:size     outer-dimensions
         :no-color true}])
     (if profile-picture
       ;; display image
       [fast-image/fast-image
        {:source profile-picture
         :style  (container-styling inner-dimensions outer-dimensions)}]
       ;; else display initials
       [container inner-dimensions outer-dimensions
        ^{:key :icon-text}
        [text/text
         {:weight :semi-bold
          :size   font-size
          :style  {:color colors/white-opa-70}}
         (upper-case icon-text)]])
     [dot-indicator size status-indicator? online? ring? (dark?)]]))
