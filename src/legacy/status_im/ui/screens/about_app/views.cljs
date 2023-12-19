(ns legacy.status-im.ui.screens.about-app.views
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.copyable-text :as copyable-text]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.webview :refer [webview]]
    [re-frame.core :as re-frame]
    [status-im2.constants :refer
     [principles-link privacy-policy-link terms-of-service-link]]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :as views]))

(views/defview about-app
  []
  (views/letsubs [app-version  [:get-app-short-version]
                  commit-hash  [:get-commit-hash]
                  node-version [:get-app-node-version]]
    [react/scroll-view
     [list.item/list-item
      {:size :small
       :title (i18n/label :t/privacy-policy)
       :accessibility-label :privacy-policy
       :on-press
       #(re-frame/dispatch [:navigate-to :privacy-policy])
       :chevron true}]
     [list.item/list-item
      {:size                :small
       :title               (i18n/label :t/terms-of-service)
       :accessibility-label :terms-of-service
       :on-press            #(re-frame/dispatch [:navigate-to :terms-of-service])
       :chevron             true}]
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
        :accessory-text      node-version}]]]))

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

(views/defview privacy-policy
  []
  [webview
   {:source              {:uri privacy-policy-link}
    :java-script-enabled true}])

(views/defview tos
  []
  [webview
   {:source              {:uri terms-of-service-link}
    :java-script-enabled true}])

(views/defview principles
  []
  [webview
   {:source              {:uri principles-link}
    :java-script-enabled true}])
