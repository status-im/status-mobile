(ns quo2.components.user-avatar
  (:require [clojure.string :refer [split]]
            [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]
            [clojure.string :refer [upper-case blank?]]
            [status-im.ui.components.react :as react]))

(def sizes {:big {:outer 80
                  :inner 72
                  :status-indicator 16
                  :status-indicator-border 2
                  :font-size 27}
            :medium {:outer 48
                     :inner 44
                     :status-indicator 10
                     :status-indicator-border 2
                     :font-size 15}
            :small {:outer 32
                    :inner 28
                    :status-indicator 10
                    :status-indicator-border 2
                    :font-size 13}})

(defn dot-indicator
  [size status-indicator? online? ring?]
  (when status-indicator?
    (let [dimensions (get-in sizes [size :status-indicator])
          border-width (get-in sizes [size :status-indicator-border])
          right (case size
                  :big 4
                  :medium 2
                  :small 0)
          bottom (case size
                   :big (if ring?
                          4
                          2)
                   :medium (if ring?
                             4
                             2)
                   :small (if ring?
                            2
                            0))]
      [rn/view {:style {:background-color (if online?
                                            colors/success-50
                                            colors/neutral-40)
                        :width dimensions
                        :height dimensions
                        :border-width border-width
                        :border-radius dimensions
                        :border-color "white"
                        :position :absolute
                        :bottom bottom
                        :right right}}])))

(defn container-styling [inner-dimensions outer-dimensions]
  {:width inner-dimensions
   :position :absolute
   :top (/ (- outer-dimensions inner-dimensions) 2)
   :left (/ (- outer-dimensions inner-dimensions) 2)
   :height inner-dimensions
   :border-radius inner-dimensions})

(defn container [inner-dimensions outer-dimensions & children]
  [rn/view {:style (merge {:background-color colors/turquoise-50
                           :justify-content :center
                           :align-items :center}
                          (container-styling inner-dimensions outer-dimensions))}
   children])

(defn user-avatar
  [{:keys [ring?
           online?
           size
           status-indicator?
           profile-picture
           full-name]
    :or {full-name "john doe"
         status-indicator? true
         online? true
         size :big
         ring? true}}]
  (let [initials (if full-name
                   (reduce str (map first (split full-name " ")))
                   "")
        first-initial-letter (if full-name
                               (first full-name)
                               "")
        small? (= size :small)
        using-profile-picture? (-> profile-picture
                                   blank?
                                   false?)
        outer-dimensions (get-in sizes [size :outer])
        inner-dimensions (get-in sizes [size (if ring?
                                               :inner
                                               :outer)])
        font-size (get-in sizes [size :font-size])]
    [rn/view {:style {:width outer-dimensions
                      :height outer-dimensions
                      :border-radius outer-dimensions}}
     (when ring?
       [icons/icon :main-icons/identicon-ring {:width outer-dimensions
                                               :height outer-dimensions
                                               :color "nil"}])
     (if using-profile-picture?
       [react/image {:style  (container-styling inner-dimensions outer-dimensions)
                     :source {:uri profile-picture}}]
       [container inner-dimensions outer-dimensions
        [rn/text {:style {:color colors/white-opa-70
                          :font-size font-size}}
         (upper-case (if small?
                       first-initial-letter
                       initials))]])
     [dot-indicator size status-indicator? online? ring?]]))
