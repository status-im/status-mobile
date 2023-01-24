(ns quo2.components.tabs.tab.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]))

(def notification-dot-offset 2)

(defn size->padding-left
  [size]
  (case size
    32 12
    28 12
    24 8
    20 8
    nil))

(defn size->border-radius
  [size]
  (case size
    32 10
    28 8
    24 8
    20 6
    nil))

(defn notification-dot
  [dot-size]
  {:position :absolute
   :z-index  1
   :right    (- dot-size notification-dot-offset)
   :top      (- notification-dot-offset)})

(def container
  {:flex-direction :row})

(defn tab
  [{:keys [size disabled background-color show-notification-dot?]}]
  (let [border-radius (size->border-radius size)
        padding       (size->padding-left size)]
    (merge {:height                    size
            :align-items               :center
            :justify-content           :flex-end
            :flex-direction            :row
            :border-top-left-radius    border-radius
            :border-bottom-left-radius border-radius
            :background-color          background-color
            :padding-left              padding}
           (when-not show-notification-dot?
             {:padding-horizontal padding
              :border-radius      border-radius})
           (when disabled
             {:opacity 0.3}))))

(def themes
  {:light {:default  {:background-color colors/neutral-20
                      :icon-color       colors/neutral-50
                      :label            {:style {:color colors/neutral-100}}}
           :active   {:background-color colors/neutral-50
                      :icon-color       colors/white
                      :label            {:style {:color colors/white}}}
           :disabled {:background-color colors/neutral-20
                      :icon-color       colors/neutral-50
                      :label            {:style {:color colors/neutral-100}}}}
   :dark  {:default  {:background-color colors/neutral-80
                      :icon-color       colors/neutral-40
                      :label            {:style {:color colors/white}}}
           :active   {:background-color colors/neutral-60
                      :icon-color       colors/white
                      :label            {:style {:color colors/white}}}
           :disabled {:background-color colors/neutral-80
                      :icon-color       colors/neutral-40
                      :label            {:style {:color colors/white}}}}})

(def themes-for-blur-background
  {:light {:default  {:background-color colors/neutral-80-opa-5
                      :icon-color       colors/neutral-80-opa-40
                      :label            {:style {:color colors/neutral-100}}}
           :active   {:background-color colors/neutral-80-opa-60
                      :icon-color       colors/white
                      :label            {:style {:color colors/white}}}
           :disabled {:background-color colors/neutral-80-opa-5
                      :icon-color       colors/neutral-80-opa-40
                      :label            {:style {:color colors/neutral-100}}}}
   :dark  {:default  {:background-color colors/white-opa-5
                      :icon-color       colors/white
                      :label            {:style {:color colors/white}}}
           :active   {:background-color colors/white-opa-20
                      :icon-color       colors/white
                      :label            {:style {:color colors/white}}}
           :disabled {:background-color colors/white-opa-5
                      :icon-color       colors/neutral-40
                      :label            {:style {:color colors/white}}}}})

(defn by-theme
  [{:keys [override-theme disabled active blur?]}]
  (let [state (cond disabled :disabled
                    active   :active
                    :else    :default)
        theme (or override-theme (theme/get-theme))]
    (get-in (if blur? themes-for-blur-background themes)
            [theme state])))
