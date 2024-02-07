(ns quo.components.tags.collectible-tag.view
  (:require
    [oops.core :refer [oget]]
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.components.tags.collectible-tag.style :as style]
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
    - :collectible-name - string
    - :collectible-number - string"
  []
  (let [container-width (reagent/atom 0)]
    (fn [{:keys [options size blur? theme collectible-img-src collectible-name collectible-number]
          :or   {size :size-24}}]
      [rn/view
       {:on-layout #(reset! container-width
                      (oget % :nativeEvent :layout :width))}
       [hole-view/hole-view
        {:holes (if options
                  [{:x            (- @container-width
                                     (case size
                                       :size-24 10
                                       :size-32 12
                                       nil))
                    :y            (case size
                                    :size-24 -6
                                    :size-32 -4
                                    nil)
                    :width        16
                    :height       16
                    :borderRadius 8}]
                  [])}
        [rn/view {:style (style/container size options blur? theme)}
         [rn/image {:style (style/collectible-img size) :source collectible-img-src}]
         [text/text
          {:size   :paragraph-2
           :weight :medium
           :style  (style/label theme)}
          collectible-name]
         [text/text
          {:size        :paragraph-2
           :weight      :medium
           :margin-left 5
           :style       (style/label theme)}
          collectible-number]]]
       (when options
         [rn/view {:style (style/options-icon size)}
          [icons/icon (if (= options :hold) :i/hold :i/add-token)
           {:size     20
            :no-color true}]])])))

(def view (quo.theme/with-theme view-internal))
