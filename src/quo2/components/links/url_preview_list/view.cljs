(ns quo2.components.links.url-preview-list.view
  (:require
    [oops.core :as oops]
    [quo2.components.links.url-preview-list.style :as style]
    [quo2.components.links.url-preview.view :as url-preview]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(defn- use-scroll-to-last-item
  [flat-list-ref item-count]
  (rn/use-effect
   (fn []
     (when (pos? item-count)
       ;; We use a delay because calling `scrollToIndex` without a delay does
       ;; nothing.
       (let [timer-id (js/setTimeout
                       (fn []
                         (when (and @flat-list-ref (pos? item-count))
                           (.scrollToIndex ^js @flat-list-ref
                                           #js
                                            {:index    (max 0 (dec item-count))
                                             :animated true})))
                       25)]
         (fn []
           (js/clearTimeout timer-id)))))
   [item-count]))

(defn- separator
  []
  [rn/view {:style style/url-preview-separator}])

(defn- item-component
  [{:keys [title body loading? logo]} _ _ {:keys [width on-clear loading-message]}]
  [url-preview/view
   {:logo            logo
    :title           title
    :body            body
    :loading?        loading?
    :loading-message loading-message
    :on-clear        on-clear
    :container-style {:width width}}])

(defn- calculate-width
  [preview-width horizontal-spacing ^js e]
  (reset! preview-width
    (- (oops/oget e "nativeEvent.layout.width")
       (* 2 horizontal-spacing))))

(defn- view-component
  []
  (let [preview-width (reagent/atom 0)
        flat-list-ref (atom nil)]
    (fn [{:keys [data key-fn horizontal-spacing on-clear loading-message]}]
      (use-scroll-to-last-item flat-list-ref (count data))
      ;; We need to use a wrapping view expanded to 100% instead of "flex 1",
      ;; otherwise `on-layout` will be triggered multiple times as the flat list
      ;; renders its children.
      [rn/view
       {:style               {:width "100%"}
        :accessibility-label :url-preview-list}
       [rn/flat-list
        {:ref                               #(reset! flat-list-ref %)
         :key-fn                            key-fn
         :on-layout                         #(calculate-width preview-width horizontal-spacing %)
         :horizontal                        true
         :deceleration-rate                 :fast
         :on-scroll-to-index-failed         identity
         :content-container-style           {:padding-horizontal horizontal-spacing}
         :separator                         [separator]
         :snap-to-interval                  (+ @preview-width style/url-preview-gap)
         :shows-horizontal-scroll-indicator false
         :data                              data
         :render-fn                         item-component
         :render-data                       {:width           @preview-width
                                             :on-clear        on-clear
                                             :loading-message loading-message}}]])))

(defn view
  [props]
  [:f> view-component props])
