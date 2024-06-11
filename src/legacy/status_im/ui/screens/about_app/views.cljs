(ns legacy.status-im.ui.screens.about-app.views
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.copyable-text :as copyable-text]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [quo.core :as quo]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf])
  (:require-macros [legacy.status-im.utils.views :as views]))

(views/defview about-app
  []
  (views/letsubs [app-version  [:get-app-short-version]
                  commit-hash  [:get-commit-hash]
                  node-version [:get-app-node-version]]
    [:<>
     [quo/page-nav
      {:type       :title
       :title      (i18n/label :t/about-app)
       :background :blur
       :icon-name  :i/close
       :on-press   #(rf/dispatch [:navigate-back])}]
     [react/scroll-view
      [copyable-text/copyable-text-view
       {:copied-text app-version}
       [list.item/list-item
        {:size                :small
         :accessibility-label :app-version
         :title               (i18n/label :t/version)
         :accessory           :text
         :accessory-text      app-version}]]
      [copyable-text/copyable-text-view
       {:copied-text commit-hash}
       [list.item/list-item
        {:size                :small
         :accessibility-label :commit-hash
         :title               (i18n/label :t/app-commit)
         :accessory           :text
         :accessory-text      commit-hash}]]
      [copyable-text/copyable-text-view
       {:copied-text node-version}
       [list.item/list-item
        {:size                :small
         :accessibility-label :node-version
         :title               (i18n/label :t/node-version)
         :accessory           :text
         :accessory-text      node-version}]]]]))

(views/defview learn-more-sheet
  []
  (views/letsubs [{:keys [title content]} [:bottom-sheet/options]]
    [react/view
     {:style {:padding-left   16
              :padding-top    16
              :padding-right  34
              :padding-bottom 0}}
     [react/view {:style {:align-items :center :flex-direction :row :margin-bottom 16}}
      [icons/icon :main-icons/info
       {:color           colors/blue
        :container-style {:margin-right 13}}]
      [react/text {:style {:typography :title-bold}} title]]
     [react/text {:style {:color colors/gray}} content]]))

(def learn-more
  {:content learn-more-sheet})
