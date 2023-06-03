(ns quo2.components.tabs.tab.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]))

(def tab-background-opacity 0.3)

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

(def notification-dot
  {:position :absolute
   :top      -2
   :right    -2})

(def container
  {:flex-direction :row})

(defn tab
  [{:keys [background-color
           disabled
           segmented?
           show-notification-dot?
           size]}]
  (let [border-radius (size->border-radius size)
        padding       (size->padding-left size)]
    (merge {:height                    size
            :align-items               :center
            :justify-content           :center
            :flex-direction            :row
            :border-top-left-radius    border-radius
            :border-bottom-left-radius border-radius
            :background-color          background-color
            :padding-left              padding}
           ;; The minimum padding right of 1 is a mandatory workaround. Without
           ;; it, the SVG rendered besides the tab will have a 1px margin. This
           ;; issue still exists in the latest react-native-svg versions.
           (if show-notification-dot?
             {:padding-right 1}
             {:border-radius border-radius
              :padding-right padding})
           (when segmented?
             {:flex 1})
           (when disabled
             {:opacity tab-background-opacity}))))

(def themes
  {:light {:default  {:background-color colors/neutral-10
                      :icon-color       colors/neutral-50
                      :label            {:style {:color colors/neutral-100}}}
           :active   {:background-color colors/neutral-50
                      :icon-color       colors/white
                      :label            {:style {:color colors/white}}}
           :disabled {:background-color colors/neutral-10
                      :icon-color       colors/neutral-50
                      :label            {:style {:color colors/neutral-100}}}}
   :dark  {:default  {:background-color colors/neutral-90
                      :icon-color       colors/neutral-40
                      :label            {:style {:color colors/white}}}
           :active   {:background-color colors/neutral-60
                      :icon-color       colors/white
                      :label            {:style {:color colors/white}}}
           :disabled {:background-color colors/neutral-90
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
