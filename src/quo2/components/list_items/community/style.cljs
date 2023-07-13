(ns quo2.components.list-items.community.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.shadows :as shadows]))

(def logo
  {:border-radius 50
   :border-width  0
   :width         32
   :height        32
   :margin-right  10})

(defn notification-dot
  [blur? theme]
  {:width            8
   :height           8
   :border-radius    4
   :background-color (if (and (= :dark theme) blur?)
                       colors/white-opa-40
                       (colors/theme-colors colors/neutral-40 colors/neutral-60 theme))})

(defn title
  [{:keys [type info blur? theme]}]
  {:color (cond
            (and (= type :engage) (= info :muted))
            (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)

            (and (= type :engage) (= info :default) (not blur?))
            (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)

            (and (= type :engage) (= info :default) (= :dark theme) blur?)
            colors/white-70-blur

            :else
            (colors/theme-colors colors/neutral-100 colors/white theme))})

(defn subtitle
  [blur? theme]
  {:color (if (and (= :dark theme) blur?)
            colors/white-opa-40
            (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))})

(defn container
  [{:keys [type pressed? blur? customization-color theme info]}]
  (merge {:padding-vertical   8
          :padding-horizontal 12
          :border-radius      12
          :flex-direction     :row
          :align-items        :center
          :background-color   (cond
                                (and pressed? (= type :engage) (= info :default) (= :dark theme) blur?)
                                colors/white-opa-5

                                (and pressed? (#{:engage :share} type))
                                (colors/theme-alpha customization-color 0.05 0.05)

                                (and (not pressed?) (= type :discover) (not blur?))
                                (colors/theme-colors colors/white colors/neutral-90 theme)

                                (and (not pressed?) (= type :discover) (= :dark theme) blur?)
                                colors/white-opa-5

                                (and pressed? (= type :discover))
                                (colors/theme-colors colors/white :transparent theme))}
         (when (and (= type :discover) (not pressed?))
           (shadows/get 3))))
