(ns quo.components.tags.token-tag.view
  (:require
    [oops.core :refer [oget]]
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.components.tags.token-tag.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.hole-view :as hole-view]
    [reagent.core :as reagent]))

(defn- view-internal
  "Options:
    - :options - false / :add / :hold (default false)
    - :size - :size-24 / :size-32 (default :size-24)
    - :blur? - boolean (default false)
    - :theme - :light / :dark 
    - :token-img-src - token image source
    - :token-value - string - token value
    - :token-symbol - string"
  []
  (let [container-width (reagent/atom 0)]
    (fn [{:keys [options size blur? theme token-img-src token-value token-symbol]
          :or   {size :size-24}}]
      [rn/view
       {:on-layout #(reset! container-width
                      (oget % :nativeEvent :layout :width))}
       [hole-view/hole-view
        {:holes (if options
                  [{:x            (- @container-width
                                     (condp = size
                                       :size-24 10
                                       :size-32 12))
                    :y            (condp = size
                                    :size-24 -6
                                    :size-32 -4)
                    :width        16
                    :height       16
                    :borderRadius 8}]
                  [])}
        [rn/view
         {:style (style/container size options blur? theme)}
         [rn/image
          {:style  (style/token-img size)
           :source token-img-src}]
         [text/text
          {:size   :paragraph-2
           :weight :medium
           :style  (style/label theme)}
          (str token-value " " token-symbol)]]]
       (when options
         [rn/view {:style (style/options-icon size)}
          [icons/icon (if (= options :hold) :i/hold :i/add-token)
           {:size     20
            :no-color true}]])])))

(def view (quo.theme/with-theme view-internal))
