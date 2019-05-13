(ns status-im.ui.screens.about-app.views
  (:require-macros [status-im.utils.views :as views]
                   [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.screens.about-app.styles :as styles]
            [status-im.i18n :as i18n]
            [status-im.transport.utils :as transport.utils]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [re-frame.core :as re-frame]))

(defn app-info->json [app-version peers mailserver node-info node-version]
  (js/JSON.stringify
   (clj->js
    {:client {:app-version app-version
              :mailserver mailserver
              :node-info node-info
              :node-version node-version}
     :peers peers})
   nil
   4))

(defview learn-more-sheet []
  (letsubs [{:keys [title content]} [:bottom-sheet/options]]
    [react/view {:style {:padding 16}}
     [react/view {:style {:align-items :center :flex-direction :row :margin-bottom 16}}
      [vector-icons/icon :main-icons/info {:color colors/blue
                                           :container-style {:margin-right 13}}]
      [react/text {:style styles/learn-more-title} title]]
     [react/text {:style styles/learn-more-text} content]]))

(def learn-more
  {:content learn-more-sheet
   :content-height 180})

(defn peer-view [{:keys [enode]}]
  (let [[enode-id ip-address port] (transport.utils/extract-url-components enode)]
    ^{:key enode}
    [react/touchable-highlight {:style styles/peer-view
                                :on-press #(react/copy-to-clipboard enode)}
     [react/view
      [react/view
       [react/text {:style styles/peer-text} (str "id: " enode-id)]]
      [react/view
       [react/text {:style styles/peer-text} (str "ip: " ip-address)]]
      [react/view
       [react/text {:style styles/peer-text} (str "port: " port)]]]]))

(defn peers-summary-section [peers]
  (mapv peer-view peers))

(views/defview about-app []
  (views/letsubs [app-version  [:get-app-short-version]
                  peers        [:peers-summary]
                  mailserver   [:mailserver/current-id]
                  node-info    [:about-app/node-info]
                  node-version [:get-app-node-version]]
    [react/view {:flex 1}
     [status-bar/status-bar]
     [toolbar/simple-toolbar (i18n/label :t/about-app)]
     [react/scroll-view
      [react/view
       [profile.components/settings-item-separator]

       [profile.components/settings-item
        {:label-kw            :t/privacy-policy
         :accessibility-label :privacy-policy
         :action-fn           #(re-frame/dispatch [:privacy-policy/privacy-policy-button-pressed])}]
       (when status-im.utils.platform/ios?
         [profile.components/settings-item-separator])
       [profile.components/settings-item
        {:item-text           (i18n/label :t/version {:version app-version})
         :action-fn           #(react/copy-to-clipboard app-version)
         :accessibility-label :version
         :hide-arrow?         true}]
       (when status-im.utils.platform/ios?
         [profile.components/settings-item-separator])
       [profile.components/settings-item
        {:item-text           node-version
         :action-fn           #(react/copy-to-clipboard node-version)
         :accessibility-label :version
         :hide-arrow?         true}]
       [profile.components/settings-item
        {:item-text           mailserver
         :action-fn           #(react/copy-to-clipboard (str mailserver))
         :accessibility-label :mailserver
         :hide-arrow?         true}]
       [profile.components/settings-item
        {:item-text           (i18n/label :t/copy-info)
         :action-fn           #(react/copy-to-clipboard
                                (app-info->json
                                 app-version
                                 peers
                                 mailserver
                                 node-info
                                 node-version))
         :accessibility-label :copy-to-clipboard
         :hide-arrow?         true}]

       [react/view {:style styles/about-title}
        [react/text {:style styles/about-title-text} (i18n/label :t/node-info)]]
       [peer-view {:enode (:enode node-info)}]
       [react/view {:style styles/about-title}
        [react/text {:style styles/about-title-text} (i18n/label :t/peers)]]
       [react/view
        (map peer-view peers)]]]]))
