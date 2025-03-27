(ns quo.components.tags.number-tag.view
  (:require
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.components.tags.number-tag.style :as style]
    [quo.context :as quo.context]
    [react-native.core :as rn]))

(defn view
  [{:keys [number size blur?] :as props}]
  (let [theme      (quo.context/use-theme)
        size-value (get-in style/sizes [size :size])
        icon-size  (get-in style/sizes [size :icon-size])]
    [rn/view (style/container props theme)
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
