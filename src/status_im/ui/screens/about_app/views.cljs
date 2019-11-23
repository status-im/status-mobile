(ns status-im.ui.screens.about-app.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.copyable-text :as copyable-text]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.about-app.styles :as styles]))

(defn- data [app-version node-version]
  [{:type                :small
    :title               :t/privacy-policy
    :accessibility-label :privacy-policy
    :on-press
    #(re-frame/dispatch
      [:privacy-policy/privacy-policy-button-pressed])
    :accessories         [:chevron]}
   [copyable-text/copyable-text-view
    {:copied-text app-version}
    [list-item/list-item
     {:type                :small
      :accessibility-label :app-version
      :title-prefix        :t/version
      :title
      [react/text
       {:number-of-lines 1
        :ellipsize-mode  :middle
        :style
        {:color        colors/gray
         :padding-left 16
         :text-align   :right
         :line-height  22}}
       app-version]}]]
   [copyable-text/copyable-text-view
    {:copied-text node-version}
    [list-item/list-item
     {:type                :small
      :accessibility-label :node-version
      :title-prefix        :t/node-version
      :title
      [react/text
       {:number-of-lines 1
        :ellipsize-mode  :middle
        :style
        {:color        colors/gray
         :padding-left 16
         :text-align   :right
         :line-height  22}}
       node-version]}]]])

(views/defview about-app []
  (views/letsubs [app-version  [:get-app-short-version]
                  node-version [:get-app-node-version]]
    [react/view {:flex 1 :background-color colors/white}
     [toolbar/simple-toolbar
      (i18n/label :t/about-app)]
     [list/flat-list
      {:data      (data app-version node-version)
       :key-fn    (fn [_ i] (str i))
       :render-fn list/flat-list-generic-render-fn}]]))

(views/defview learn-more-sheet []
  (views/letsubs [{:keys [title content]} [:bottom-sheet/options]]
    [react/view {:style {:padding-left 16 :padding-top 16
                         :padding-right 34 :padding-bottom 0}}
     [react/view {:style {:align-items :center :flex-direction :row :margin-bottom 16}}
      [vector-icons/icon :main-icons/info {:color colors/blue
                                           :container-style {:margin-right 13}}]
      [react/text {:style styles/learn-more-title} title]]
     [react/text {:style styles/learn-more-text} content]]))

(def learn-more
  {:content learn-more-sheet})
