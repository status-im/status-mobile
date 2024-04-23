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
    [schema.core :as schema]))

(defn- view-internal
  [{:keys [options blur? collectible-img-src collectible-name collectible-id] :as props}]
  (let [theme                 (quo.theme/use-theme)
        [container-width
         set-container-width] (rn/use-state 0)
        on-layout             (rn/use-callback
                               #(set-container-width (oops/oget % :nativeEvent :layout :width)))
        size                  (or (:size props) :size-24)]
    [rn/view {:on-layout on-layout}
     [hole-view/hole-view
      {:holes (if options
                [{:x            (- container-width
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
       (when collectible-id
         [text/text
          {:size        :paragraph-2
           :weight      :medium
           :margin-left 5
           :style       (style/label theme)}
          collectible-id])]]
     (when options
       [rn/view {:style (style/options-icon size)}
        [icons/icon (if (= options :hold) :i/hold :i/add-token)
         {:size     20
          :no-color true}]])]))

(def view (schema/instrument #'view-internal component-schema/?schema))
