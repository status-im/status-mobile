(ns quo2.components.avatars.wallet-user-avatar.view
  (:require [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo.theme]
            [react-native.core :as rn]
            [quo2.components.avatars.wallet-user-avatar.style :as style]
            utils.string))

(def circle-sizes
  {:small   20
   :medium  32
   :large   48
   :x-large 80})

(def font-sizes
  {:small   :label
   :medium  :paragraph-2
   :large   :paragraph-1
   :x-large :heading-1})

(def font-weights
  {:small   :medium
   :medium  :semi-bold
   :large   :semi-bold
   :x-large :medium})

(defn- view-internal
  "Options:

  :full-name - string (default: nil) - used to generate initials
  :color  - keyword (default: nil) - color of the avatar
  :size   - keyword (default: :x-large) - size of the avatar"
  [{:keys [full-name color size theme]
    :or   {full-name "empty name"
           color     :red
           size      :x-large}}]
  (let [circle-size  (size circle-sizes)
        small?       (= size :small)
        circle-color (colors/theme-colors
                      (colors/custom-color color 50 20)
                      (colors/custom-color color 60 20)
                      theme)
        text-color   (colors/theme-colors
                      (colors/custom-color color 50)
                      (colors/custom-color color 60)
                      theme)]
    [rn/view
     {:style (style/container circle-size circle-color)}
     [text/text
      {:accessibility-label :wallet-user-avatar
       :size                (size font-sizes)
       :weight              (size font-weights)
       :style               {:color text-color}}
      (utils.string/get-initials full-name (if small? 1 2))]]))

(def view (quo.theme/with-theme view-internal))
