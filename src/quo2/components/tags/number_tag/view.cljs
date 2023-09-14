(ns quo2.components.tags.number-tag.view
  (:require [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [react-native.core :as rn]
            [quo2.components.tags.number-tag.style :as style]
            [quo2.theme :as quo.theme]))

(defn view-internal
  [{:keys [number size blur? theme] :as props}]
  (let [size-value (get-in style/sizes [size :size])
        icon-size  (get-in style/sizes [size :icon-size])]
    [rn/view (style/container props)
     (if (and (> size-value 20) (< (count number) 3))
       [text/text
        {:size   (if (= size :size-32)
                   :paragraph-2
                   :label)
         :weight :medium
         :style  {:color (style/get-color blur? theme)}}
        (str "+" number)]
       [icons/icon :i/options
        {:size  icon-size
         :color (style/get-color blur? theme)}])]))

(def view (quo.theme/with-theme view-internal))
