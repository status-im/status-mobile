(ns status-im.ui.screens.browser.options.views
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [status-im.browser.core :as browser]
            [status-im.constants :as constants]
            [i18n.i18n :as i18n]
            [status-im.qr-scanner.core :as qr-scanner]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.components.views :as components]
            [status-im.utils.http :as http]
            [status-im.utils.utils :as utils]))

(defn hide-sheet-and-dispatch
  [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (re-frame/dispatch event))

(defn wallet-connection
  [host account]
  (fn []
    [react/view {:flex 1}
     [react/text {:style {:align-self :center :margin-horizontal 16 :margin-vertical 8}}
      (str "“" host "” " (i18n/label :t/has-permissions))]
     [quo/list-item
      {:icon      [chat-icon/custom-icon-view-list (:name account) (:color account)]
       :title     (:name account)
       :subtitle  (utils/get-shortened-checksum-address (:address account))
       :accessory [icons/icon :main-icons/check {:color colors/gray}]}]
     [react/view {:padding-vertical 8}
      [components/separator]]
     [quo/list-item
      {:theme               :negative
       :title               (i18n/label :t/revoke-access)
       :accessibility-label :revoke-access
       :icon                :main-icons/cancel
       :on-press            #(hide-sheet-and-dispatch [:browser/revoke-dapp-permissions host])}]]))

(defn browser-options
  [url account empty-tab name]
  (fn []
    (let [topic       (http/topic-from-url url)
          bookmarks   @(re-frame/subscribe [:bookmarks/active])
          permissions @(re-frame/subscribe [:dapps/permissions])
          fav?        (get bookmarks url)
          connected?  (some #{constants/dapp-permission-web3}
                            (get-in permissions [(http/url-host url) :permissions]))]
      [react/view {:flex 1}
       [quo/button
        {:style               {:align-self   :flex-end
                               :margin-right 15}
         :type                :icon
         :theme               :icon
         :accessibility-label :universal-qr-scanner
         :on-press            #(hide-sheet-and-dispatch
                                [::qr-scanner/scan-code
                                 {:handler ::qr-scanner/on-scan-success}])}
        :main-icons/qr]
       (when-not empty-tab
         [:<>
          [quo/list-item
           {:theme               :accent
            :title               (i18n/label :t/new-tab)
            :accessibility-label :new-tab
            :icon                :main-icons/add
            :on-press            #(hide-sheet-and-dispatch [:browser.ui/open-empty-tab])}]
          [quo/list-item
           {:theme               :accent
            :title               (if fav? (i18n/label :t/remove-favourite) (i18n/label :t/add-favourite))
            :accessibility-label :add-remove-fav
            :icon                (if fav? :main-icons/delete :main-icons/favourite)
            :on-press            #(hide-sheet-and-dispatch
                                   (if fav?
                                     [:browser/delete-bookmark url]
                                     [:open-modal :new-bookmark {:url url :name name :new true}]))}]
          [quo/list-item
           {:theme               :accent
            :title               (i18n/label :t/share)
            :accessibility-label :share
            :icon                :main-icons/share
            :on-press            (fn []
                                   (re-frame/dispatch-sync [:bottom-sheet/hide])
                                   (js/setTimeout
                                    #(browser/share-link url)
                                    200))}]
          [quo/list-item
           {:icon                [chat-icon/custom-icon-view-list
                                  topic
                                  (rand-nth colors/chat-colors)]
            :accessibility-label :open-chat
            :title               (str "#" topic)
            :subtitle            (i18n/label :t/open-chat)
            :on-press            #(hide-sheet-and-dispatch [:chat.ui/start-public-chat topic])
            :chevron             true}]
          [components/separator]])
       (if connected?
         [quo/list-item
          {:icon                [chat-icon/custom-icon-view-list (:name account) (:color account)]
           :title               (:name account)
           :subtitle            (i18n/label :t/connected)
           :accessibility-label :connected-account
           :chevron             true
           :on-press            #(hide-sheet-and-dispatch
                                  [:bottom-sheet/show-sheet
                                   {:content (wallet-connection (http/url-host url) account)}])}]
         [quo/list-item
          {:theme               :accent
           :title               (i18n/label :t/connect-wallet)
           :accessibility-label :connect-account
           :icon                :main-icons/wallet
           :on-press            #(hide-sheet-and-dispatch
                                  [:browser/bridge-message-received
                                   "{\"type\":\"api-request\",\"permission\":\"web3\"}"])}])])))
