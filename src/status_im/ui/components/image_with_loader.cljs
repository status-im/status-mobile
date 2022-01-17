(ns status-im.ui.components.image-with-loader
  (:require [reagent.core :as reagent]
            [quo.design-system.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.utils.contenthash :as contenthash]))

(defn- placeholder [props child]
  (let [{:keys [accessibility-label style]} props]
    [react/view {:accessibility-label accessibility-label
                 :style (merge style
                               {:align-items :center
                                :justify-content :center
                                :background-color colors/gray-lighter})}
     child]))

(defn source [props]
  (let [{:keys [source style accessibility-labels]} props
        loaded? (reagent/atom false)
        error? (reagent/atom false)
        current-source (reagent/atom (if (seq? source)
                                       source
                                       [source]))]
    (fn []
      [react/view
       (when @error?
         [placeholder {:accessibility-label (:error accessibility-labels)
                       :style style}
          [icons/icon :main-icons/cancel]])
       (when-not (or @loaded? @error?)
         [placeholder {:accessibility-label (:loading accessibility-labels)
                       :style style}
          [react/activity-indicator {:animating true}]])
       (when (not @error?)
         [react/fast-image {:accessibility-label (:success accessibility-labels)
                            :onError #(if (empty? (rest @current-source))
                                        (reset! error? true)
                                        (reset! current-source
                                                (rest @current-source)))
                            :onLoad #(reset! loaded? true)
                            :style (if @loaded? style {})
                            :source (first @current-source)}])])))

(defn ipfs [props]
  (let [{:keys [hash]} props]
    (source (merge (dissoc props :hash)
                   {:source (map (fn [u] {:uri u})
                                 (contenthash/alternatives hash))}))))
