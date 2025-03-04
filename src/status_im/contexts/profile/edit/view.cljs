(ns status-im.contexts.profile.edit.view
  (:require [quo.core :as quo]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.common.not-implemented :as not-implemented]
            [status-im.config :as config]
            [status-im.contexts.profile.edit.header.view :as header]
            [status-im.contexts.profile.edit.list-items :as edit.items]
            [status-im.contexts.profile.edit.style :as style]
            [utils.re-frame :as rf]))

(defn- item-view
  [data]
  [quo/category
   {:container-style {:padding-bottom 9.5}
    :list-type       :settings
    :blur?           true
    :label           (:label data)
    :data            (:items data)}])

(defn- get-item-layout
  [_ index]
  #js {:length 100 :offset (* 100 index) :index index})

(defn view
  []
  (let [theme  (quo.theme/use-theme)
        insets (safe-area/get-insets)]
    [quo/overlay
     {:type            :shell
      :container-style (style/page-wrapper (:top insets))}
     [quo/page-nav
      {:key        :header
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   #(rf/dispatch [:navigate-back])
       :right-side (when config/show-not-implemented-features?
                     [{:icon-name :i/reveal :on-press not-implemented/alert}])}]
     [rn/flat-list
      {:key                             :list
       :header                          [header/view]
       :data                            (edit.items/items theme)
       :key-fn                          :label
       :get-item-layout                 get-item-layout
       :initial-num-to-render           3
       :max-to-render-per-batch         3
       :shows-vertical-scroll-indicator false
       :render-fn                       item-view}]]))
