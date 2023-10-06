(ns quo2.components.avatars.wallet-user-avatar
  (:require [clojure.string :as string]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [quo2.theme :as quo.theme]))

(def circle-sizes
  {:small   20
   :medium  32
   :large   48
   :size-64 64
   :x-large 80})

(def font-sizes
  {:small   :label
   :medium  :paragraph-2
   :large   :paragraph-1
   :size-64 :heading-1
   :x-large :heading-1})

(def font-weights
  {:small   :medium
   :medium  :semi-bold
   :large   :semi-bold
   :size-64 :medium
   :x-large :medium})

(defn- view-internal
  "params, first name, last name, customization-color, size
   and if it's dark or not!"
  [{:keys [f-name l-name customization-color size theme monospace? uppercase?]
    :or   {f-name     "John"
           l-name     "Doe"
           size       :x-large
           uppercase? true}}]
  (let [circle-size    (size circle-sizes)
        small?         (= size :small)
        f-name-initial (-> f-name
                           (#(if uppercase? (string/upper-case %) %))
                           (subs 0 1))
        l-name-initial (-> l-name
                           (#(if uppercase? (string/upper-case %) %))
                           (subs 0 1))
        circle-color   (if customization-color
                         (colors/resolve-color customization-color theme 20)
                         (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5 theme))
        text-color     (if customization-color
                         (colors/resolve-color customization-color theme)
                         (colors/theme-colors colors/neutral-80-opa-70 colors/white-opa-70 theme))]
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
       :weight (if monospace? :monospace (size font-weights))
       :style  {:color text-color}}
      (if small?
        (str f-name-initial)
        (str f-name-initial l-name-initial))]]))

(def wallet-user-avatar (quo.theme/with-theme view-internal))
