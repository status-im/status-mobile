(ns quo.components.tags.collectible-tag.view
  (:require
    [oops.core :as oops]
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.components.tags.collectible-tag.schema :as component-schema]
    [quo.components.tags.collectible-tag.style :as style]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.hole-view :as hole-view]
    [reagent.core :as reagent]
    [schema.core :as schema]))

(defn- view-internal
  []
  (let [container-width (reagent/atom 0)
        on-layout       #(->> (oops/oget % :nativeEvent :layout :width)
                              (reset! container-width))]
    (fn [{:keys [options blur? theme collectible-img-src collectible-name collectible-id] :as props}]
      (let [size (or (:size props) :size-24)]
        [rn/view
         {:on-layout on-layout}
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
            collectible-id]]]
         (when options
           [rn/view {:style (style/options-icon size)}
            [icons/icon (if (= options :hold) :i/hold :i/add-token)
             {:size     20
              :no-color true}]])]))))

(def view
  (quo.theme/with-theme
   (schema/instrument #'view-internal component-schema/?schema)))
