(ns quo2.components.new-tab
  (:require [quo2.foundations.colors :as colors]
            [quo.react-native :as rn]
            [quo.design-system.spacing :as spacing]
            [quo.theme :as theme]
            [status-im.ui.components.icons.icons :as icons]
            [quo2.components.text :as text]))

(def sizes
  {:large {:border-radius 10
           :padding-horizontal 12
           :padding-vertical 6
           :height 32
           :font-size 15
           :flex-direction :row
           :justify-content :space-between
           :align-items :center
           :font-weight "500"
           :text-align :center}
  :medium {:border-radius 8
           :padding-horizontal 6
           :padding-vertical 2
           :font-size 13
           :height 24
           :font-weight "500"
           :flex-direction :row
           :justify-content :space-between
           :align-items :center
           :text-align :center}})

(def light {:color colors/black
            :background-color colors/neutral-10})

(def themes {:dark {:color colors/white
                   :background-color colors/neutral-50}
            :light light
            :opaque (assoc light :opacity 0.3)})

(defn tab-icon
  [icon font-size text-color]
  [icons/icon (keyword (str ":main-icons/" icon))
   {:color text-color 
    :width font-size :height font-size}])

(defn new-tab
  [{:keys [size text icon opaque? theme] :or {text "Tab"
                                              theme :light
                                              size :medium
                                              opaque? false
                                              icon :placeholder}}]
  
  (let [font-size (:font-size (size sizes))
        kw-icon (keyword icon)
        text-color (:color (theme themes))
        font-weight (:font-weight (theme themes))
        component-styling (merge (theme themes)
                                 (size sizes))]
    [rn/view {:style component-styling}
     [rn/view {:style (cond-> {:margin-right (:x-tiny spacing/spacing)}
                        opaque? (assoc :opacity 0.3))}
      (case kw-icon
        :none nil
        :placeholder [tab-icon "placeholder12" font-size text-color]
        [tab-icon (name icon) font-size text-color])] 
      [rn/text {:style
                (cond-> {:color text-color
                         :font-weight font-weight}
                  opaque? (assoc :opacity 0.3))} text]]))