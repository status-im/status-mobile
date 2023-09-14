(ns quo2.components.avatars.wallet-user-avatar
  (:require [clojure.string :as string]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(def circle-sizes
  {:small     20
   :medium    32
   :large     48
   :size/l-64 64
   :x-large   80})

(def font-sizes
  {:small     :label
   :medium    :paragraph-2
   :large     :paragraph-1
   :size/l-64 :heading-1
   :x-large   :heading-1})

(def font-weights
  {:small   :medium
   :medium  :semi-bold
   :large   :semi-bold
   :size/l-64 :medium
   :x-large :medium})

(defn wallet-user-avatar
  "params, first name, last name, color, size
   and if it's dark or not!"
  [{:keys [f-name l-name color size]
    :or   {f-name "John"
           l-name "Doe"
           color  :red
           size   :x-large}}]
  (let [circle-size    (size circle-sizes)
        small?         (= size :small)
        f-name-initial (-> f-name
                           string/upper-case
                           (subs 0 1))
        l-name-initial (-> l-name
                           string/upper-case
                           (subs 0 1))
        circle-color   (colors/custom-color color 50 20)
        text-color     (colors/custom-color-by-theme color 50 60)]
    [rn/view
     {:style {:width            circle-size
              :height           circle-size
              :border-radius    circle-size
              :text-align       :center
              :justify-content  :center
              :align-items      :center
              :background-color circle-color}}
     [text/text
      {:size   (size font-sizes)
       :weight (size font-weights)
       :style  {:color text-color}}
      (if small?
        (str f-name-initial)
        (str f-name-initial l-name-initial))]]))
