(ns quo2.components.avatars.wallet-user-avatar
  (:require
    [clojure.string :as string]
    [quo2.components.markdown.text :as text]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]))

(def circle-sizes
  {:size-20 20
   :size-32 32
   :size-48 48
   :size-64 64
   :size-80 80})

(def font-sizes
  {:size-20 :label
   :size-32 :paragraph-2
   :size-48 :paragraph-1
   :size-64 :heading-1
   :size-80 :heading-1})

(def font-weights
  {:size-20 :medium
   :size-32 :semi-bold
   :size-48 :semi-bold
   :size-64 :medium
   :size-80 :medium})

(defn- view-internal
  "params, first name, last name, customization-color, size
   and if it's dark or not!"
  [{:keys [f-name l-name customization-color size theme monospace? uppercase?]
    :or   {f-name     "John"
           l-name     "Doe"
           size       :size-80
           uppercase? true}}]
  (let [circle-size    (size circle-sizes)
        small?         (= size :size-20)
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

(def view (quo.theme/with-theme view-internal))
