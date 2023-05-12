(ns quo2.components.links.url-preview-list.view
  (:require
    [quo2.components.links.url-preview-list.style :as style]
    [quo2.components.links.url-preview.view :as url-preview]
    [react-native.core :as rn]))

(defn- use-scroll-to-last-item
  [flat-list-ref item-count item-width]
  (rn/use-effect
   (fn []
     (when (and (pos? item-count) (pos? item-width))
       ;; We use a delay because calling `scrollToOffset` without a delay does
       ;; nothing while the flatlist is still rendering its children.
       ;; `scrollToEnd` doesn't work because it positions the item off-center
       ;; and there's no argument to offset it.
       (let [timer-id (js/setTimeout
                       (fn []
                         (when (and @flat-list-ref (pos? item-count))
                           (.scrollToOffset ^js @flat-list-ref
                                            #js
                                             {:animated true
                                              :offset   (* (+ item-width style/url-preview-gap)
                                                           (max 0 (dec item-count)))})))
                       25)]
         (fn []
           (js/clearTimeout timer-id)))))
   [item-count item-width]))

(defn- separator
  []
  [rn/view {:style style/url-preview-separator}])

(defn- item-component
  [{:keys [title body loading? logo]} _ _
   {:keys [width on-clear loading-message container-style]}]
  [url-preview/view
   {:logo            logo
    :title           title
    :body            body
    :loading?        loading?
    :loading-message loading-message
    :on-clear        on-clear
    :container-style (merge container-style {:width width})}])

(defn- f-view
  []
  (let [flat-list-ref (atom nil)]
    (fn [{:keys [data key-fn horizontal-spacing on-clear loading-message
                 container-style container-style-item
                 preview-width]}]
      (use-scroll-to-last-item flat-list-ref (count data) preview-width)
      ;; We need to use a wrapping view expanded to 100% instead of "flex 1",
      ;; otherwise `on-layout` will be triggered multiple times as the flat list
      ;; renders its children.
      [rn/view
       {:style               container-style
        :accessibility-label :url-preview-list}
       [rn/flat-list
        {:ref                               #(reset! flat-list-ref %)
         :keyboard-should-persist-taps      :always
         :key-fn                            key-fn
         :horizontal                        true
         :deceleration-rate                 :fast
         :on-scroll-to-index-failed         identity
         :content-container-style           {:padding-horizontal horizontal-spacing}
         :separator                         [separator]
         :snap-to-interval                  (+ preview-width style/url-preview-gap)
         :shows-horizontal-scroll-indicator false
         :data                              data
         :render-fn                         item-component
         :render-data                       {:width           preview-width
                                             :on-clear        on-clear
                                             :loading-message loading-message
                                             :container-style container-style-item}}]])))

(defn view
  [props]
  [:f> f-view props])
