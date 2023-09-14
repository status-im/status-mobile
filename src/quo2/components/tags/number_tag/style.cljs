(ns quo2.components.tags.number-tag.style
  (:require [quo2.foundations.colors :as colors]))

(def sizes
  {:size-32 {:size          32
               :width-extra   40
               :border-radius {:rounded 16 :squared 10}
               :icon-size     20}
   :size-24 {:size          24
               :width-extra   32
               :border-radius {:rounded 12 :squared 8}
               :icon-size     16}
   :size-20 {:size          20
               :width-extra   24
               :border-radius {:rounded 10 :squared 8}
               :icon-size     12}
   :size-16 {:size          16
               :width-extra   20
               :border-radius {:rounded 8 :squared 8}
               :icon-size     12}
   :size-14 {:size          14
               :width-extra   16
               :border-radius {:rounded 7 :squared 7}
               :icon-size     12}})

(defn get-color
  [blur? theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-70
                         colors/white-opa-70
                         theme)
    (colors/theme-colors colors/neutral-50
                         colors/neutral-40
                         theme)))

(defn get-bg-color
  [blur? theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-5
                         colors/white-opa-5
                         theme)
    (colors/theme-colors colors/neutral-20
                         colors/neutral-90
                         theme)))

(defn get-shape-value
  [size attribute shape]
  (let [shapes (get-in sizes [size attribute])]
    (when shapes (or (shape shapes) (first (vals shapes))))))

(defn get-width
  [size number]
  (let [size-value (get-in sizes [size :size])
        widen?     (and (> size-value 20) (= (count number) 2))]
    (get-in sizes [size (if widen? :width-extra :size)])))

(defn container
  [{:keys [type number size blur? theme]}]
  {:style {:width            (get-width size number)
           :height           (get-in sizes [size :size])
           :border-radius    (get-shape-value size :border-radius type)
           :justify-content  :center
           :align-items      :center
           :background-color (get-bg-color blur? theme)}})
