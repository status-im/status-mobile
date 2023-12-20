(ns status-im.contexts.profile.edit.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.common.not-implemented :as not-implemented]
            [status-im.contexts.profile.edit.header :as header]
            [status-im.contexts.profile.edit.list-items :as edit.items]
            [status-im.contexts.profile.edit.style :as style]
            [utils.re-frame :as rf]))

(defn- item-view
  [data]
  [quo/category
   {:list-type :settings
    :label     (:label data)
    :blur?     true
    :data      (:items data)}])

(defn- get-item-layout
  [_ index]
  #js {:length 48 :offset (* 48 index) :index index})

(defn view
  []
  (let [insets (safe-area/get-insets)]
    [quo/overlay {:type :shell}
     [rn/view
      {:key   :header
       :style (style/header-container (:top insets))}
      [quo/page-nav
       {:background :blur
        :icon-name  :i/arrow-left
        :on-press   #(rf/dispatch [:navigate-back])
        :right-side [{:icon-name :i/reveal :on-press not-implemented/alert}]}]]
     [rn/flat-list
      {:key                             :list
       :header                          [header/view]
       :data                            (edit.items/items)
       :key-fn                          :label
       :get-item-layout                 get-item-layout
       :initial-num-to-render           3
       :max-to-render-per-batch         3
       :shows-vertical-scroll-indicator false
       :render-fn                       item-view}]]))
