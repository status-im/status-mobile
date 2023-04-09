(ns quo2.components.links.url-preview-list.view
  (:require
    [oops.core :as oops]
    [quo2.components.links.url-preview-list.style :as style]
    [quo2.components.links.url-preview.view :as url-preview]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(defn- url-preview-separator
  []
  [rn/view {:style style/url-preview-separator}])

(defn- url-preview-item
  [{:keys [title body loading? logo]} _ _ {:keys [width on-clear loading-message]}]
  [url-preview/view
   {:logo            logo
    :title           title
    :body            body
    :loading?        loading?
    :loading-message loading-message
    :on-clear        on-clear
    :container-style {:width width}}])

(defn view
  []
  (let [preview-width (reagent/atom 0)]
    (fn [{:keys [ref data key-fn horizontal-spacing on-clear loading-message]}]
      ;; We need to use a wrapping view expanded to 100% instead of "flex 1",
      ;; otherwise `on-layout` will be triggered multiple times as the flat list
      ;; renders its children.
      [rn/view
       {:style               {:width "100%"}
        :accessibility-label :url-preview-list}
       [rn/flat-list
        (merge
         (when ref
           {:ref #(reset! ref %)})
         {:key-fn                            key-fn
          :on-layout                         (fn [^js e]
                                               (let [width (- (oops/oget e "nativeEvent.layout.width")
                                                              (* 2 horizontal-spacing))]
                                                 (reset! preview-width width)))
          :horizontal                        true
          :deceleration-rate                 :fast
          :on-scroll-to-index-failed         identity
          :content-container-style           {:padding-horizontal horizontal-spacing}
          :separator                         [url-preview-separator]
          :snap-to-interval                  (+ @preview-width style/url-preview-gap)
          :shows-horizontal-scroll-indicator false
          :data                              data
          :render-fn                         url-preview-item
          :render-data                       {:width           @preview-width
                                              :on-clear        on-clear
                                              :loading-message loading-message}})]])))
