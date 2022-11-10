(ns status-im.ui2.screens.quo2-preview.main
  (:require [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [quo2.components.markdown.text :as quo2-text]
            [quo2.components.buttons.button :as quo2-button]
            [status-im.ui2.screens.quo2-preview.avatars.channel-avatar :as channel-avatar]
            [status-im.ui2.screens.quo2-preview.avatars.icon-avatar :as icon-avatar]
            [status-im.ui2.screens.quo2-preview.avatars.group-avatar :as group-avatar]
            [status-im.ui2.screens.quo2-preview.avatars.user-avatar :as user-avatar]
            [status-im.ui2.screens.quo2-preview.avatars.wallet-user-avatar :as wallet-user-avatar]
            [status-im.ui2.screens.quo2-preview.buttons.button :as button]
            [status-im.ui2.screens.quo2-preview.buttons.dynamic-button :as dynamic-button]
            [status-im.ui2.screens.quo2-preview.code.snippet :as code-snippet]
            [status-im.ui2.screens.quo2-preview.counter.counter :as counter]
            [status-im.ui2.screens.quo2-preview.community.community-card-view :as community-card]
            [status-im.ui2.screens.quo2-preview.community.community-list-view :as community-list-view]
            [status-im.ui2.screens.quo2-preview.community.discover-card :as discover-card]
            [status-im.ui2.screens.quo2-preview.dividers.divider-label :as divider-label]
            [status-im.ui2.screens.quo2-preview.dividers.new-messages :as new-messages]
            [status-im.ui2.screens.quo2-preview.drawers.action-drawers :as drawers]
            [status-im.ui2.screens.quo2-preview.dropdowns.dropdown :as dropdown]
            [status-im.ui2.screens.quo2-preview.info.info-message :as info-message]
            [status-im.ui2.screens.quo2-preview.info.information-box :as information-box]
            [status-im.ui2.screens.quo2-preview.wallet.lowest-price :as lowest-price]
            [status-im.ui2.screens.quo2-preview.list-items.preview-lists :as preview-lists]
            [status-im.ui2.screens.quo2-preview.list-items.channel :as channel]
            [status-im.ui2.screens.quo2-preview.markdown.text :as text]
            [status-im.ui2.screens.quo2-preview.messages.gap :as messages-gap]
            [status-im.ui2.screens.quo2-preview.messages.system-message :as system-message]
            [status-im.ui2.screens.quo2-preview.notifications.activity-logs :as activity-logs]
            [status-im.ui2.screens.quo2-preview.reactions.react :as react]
            [status-im.ui2.screens.quo2-preview.selectors.disclaimer :as disclaimer]
            [status-im.ui2.screens.quo2-preview.selectors.selectors :as selectors]
            [status-im.ui2.screens.quo2-preview.switcher.switcher-cards :as switcher-cards]
            [status-im.ui2.screens.quo2-preview.navigation.top-nav :as top-nav]
            [status-im.ui2.screens.quo2-preview.navigation.bottom-nav-tab :as bottom-nav-tab]
            [status-im.ui2.screens.quo2-preview.navigation.floating-shell-button :as floating-shell-button]
            [status-im.ui2.screens.quo2-preview.tabs.account-selector :as account-selector]
            [status-im.ui2.screens.quo2-preview.tabs.segmented-tab :as segmented]
            [status-im.ui2.screens.quo2-preview.tabs.tabs :as tabs]
            [status-im.ui2.screens.quo2-preview.tags.context-tags :as context-tags]
            [status-im.ui2.screens.quo2-preview.tags.tags :as tags]
            [status-im.ui2.screens.quo2-preview.tags.permission-tag :as permission-tag]
            [status-im.ui2.screens.quo2-preview.tags.status-tags :as status-tags]
            [status-im.ui2.screens.quo2-preview.tags.token-tag :as token-tag]
            [status-im.ui2.screens.quo2-preview.wallet.token-overview :as token-overview]
            [status-im.ui2.screens.quo2-preview.wallet.network-breakdown :as network-breakdown]
            [status-im.ui2.screens.quo2-preview.wallet.network-amount :as network-amount]
            [status-im.ui2.screens.quo2-preview.navigation.page-nav :as page-nav]
            [status-im.ui2.screens.quo2-preview.avatars.account-avatar :as account-avatar]
            [re-frame.core :as re-frame]))

(def screens-categories
  {:avatar [{:name      :group-avatar
             :insets    {:top false}
             :component group-avatar/preview-group-avatar}
            {:name      :icon-avatar
             :insets    {:top false}
             :component icon-avatar/preview-icon-avatar}
            {:name      :user-avatar
             :insets    {:top false}
             :component user-avatar/preview-user-avatar}
            {:name      :wallet-user-avatar
             :insets    {:top false}
             :component wallet-user-avatar/preview-wallet-user-avatar}
            {:name      :channel-avatar
             :insets    {:top false}
             :component channel-avatar/preview-channel-avatar}
            {:name      :account-avatar
             :insets    {:top false}
             :component account-avatar/preview-account-avatar}]
   :buttons [{:name      :button
              :insets    {:top false}
              :component button/preview-button}
             {:name      :dynamic-button
              :insets    {:top false}
              :component dynamic-button/preview-dynamic-button}]
   :code [{:name         :snippet
           :insets       {:top false}
           :component    code-snippet/preview-code-snippet}]
   :community [{:name      :community-card-view
                :insets    {:top false}
                :component community-card/preview-community-card}
               {:name      :community-list-view
                :insets    {:top false}
                :component community-list-view/preview-community-list-view}
               {:name      :discover-card
                :insets    {:top false}
                :component discover-card/preview-discoverd-card}]
   :counter [{:name      :counter
              :insets    {:top false}
              :component counter/preview-counter}]
   :dividers [{:name      :divider-label
               :inset     {:top false}
               :component divider-label/preview-divider-label}
              {:name      :new-messages
               :insets    {:top false}
               :component new-messages/preview-new-messages}]
   :drawers [{:name      :action-drawers
              :insets    {:top false}
              :component drawers/preview-action-drawers}]
   :dropdowns [{:name      :dropdown
                :insets    {:top false}
                :component dropdown/preview-dropdown}]
   :info [{:name      :info-message
           :insets    {:top false}
           :component info-message/preview-info-message}
          {:name      :information-box
           :insets    {:top false}
           :component information-box/preview-information-box}]
   :list-items [{:name      :channel
                 :insets    {:top false}
                 :component channel/preview-channel}
                {:name      :preview-lists
                 :insets    {:top false}
                 :component preview-lists/preview-preview-lists}]
   :markdown [{:name      :texts
               :insets    {:top false}
               :component text/preview-text}]
   :messages [{:name      :gap
               :insets    {:top false}
               :component messages-gap/preview-messages-gap}
              {:name      :system-messages
               :insets    {:top false}
               :component system-message/preview-system-message}]
   :navigation [{:name      :bottom-nav-tab
                 :insets    {:top false}
                 :component bottom-nav-tab/preview-bottom-nav-tab}
                {:name      :top-nav
                 :insets    {:top false}
                 :component top-nav/preview-top-nav}
                {:name      :page-nav
                 :insets    {:top false}
                 :component page-nav/preview-page-nav}
                {:name      :floating-shell-button
                 :insets    {:top false}
                 :component floating-shell-button/preview-floating-shell-button}]
   :notifications [{:name      :activity-logs
                    :insets    {:top false}
                    :component activity-logs/preview-activity-logs}]
   :reactions [{:name      :react
                :insets    {:top false}
                :component react/preview-react}]
   :switcher [{:name :switcher-cards
               :insets {:top false}
               :component switcher-cards/preview-switcher-cards}]
   :selectors [{:name :disclaimer
                :insets {:top false}
                :component disclaimer/preview-disclaimer}
               {:name :selectors
                :insets {:top false}
                :component selectors/preview-selectors}]
   :tabs [{:name      :segmented
           :insets    {:top false}
           :component segmented/preview-segmented}
          {:name      :tabs
           :insets    {:top false}
           :component tabs/preview-tabs}
          {:name      :account-selector
           :insets    {:top false}
           :component account-selector/preview-this}]
   :tags [{:name      :context-tags
           :insets    {:top false}
           :component context-tags/preview-context-tags}
          {:name      :tags
           :insets    {:top false}
           :component tags/preview-tags}
          {:name      :permission-tag
           :insets    {:top false}
           :component permission-tag/preview-permission-tag}
          {:name      :status-tags
           :insets    {:top false}
           :component status-tags/preview-status-tags}
          {:name      :token-tag
           :insets    {:top false}
           :component token-tag/preview-token-tag}]
   :wallet [{:name      :lowest-price
             :insets    {:top false}
             :component lowest-price/preview-lowest-price}
            {:name      :token-overview
             :insets    {:top false}
             :component token-overview/preview-token-overview}
            {:name      :network-breakdown
             :insets    {:top false}
             :component network-breakdown/preview-network-breakdown}
            {:name      :network-amount
             :insets    {:top false}
             :component network-amount/preview}]})

(def screens (flatten (map val screens-categories)))

(defn theme-switcher []
  [rn/view {:style {:flex-direction   :row
                    :margin-vertical  8}}
   [quo2-button/button {:on-press #(theme/set-theme :light)} "Set light theme"]
   [quo2-button/button {:on-press #(theme/set-theme :dark)} "Set dark theme"]])

(defn main-screen []
  (fn []
    [safe-area/consumer
     (fn [insets]
       [rn/scroll-view {:flex               1
                        :padding-top        (:top insets)
                        :padding-bottom     8
                        :padding-horizontal 16
                        :background-color   (colors/theme-colors colors/white colors/neutral-90)}
        [theme-switcher]
        [quo2-text/text {:size :heading-1} "Preview Quo2 Components"]
        [rn/view
         (map (fn [category]
                ^{:key (get category 0)}
                [rn/view  {:style {:margin-vertical 8}}
                 [quo2-text/text
                  {:weight :semi-bold
                   :size :heading-2}
                  (clojure.core/name (key category))]
                 (for [{:keys [name]} (val category)]
                   ^{:key name}
                   [quo2-button/button
                    {:style {:margin-vertical 8}
                     :on-press #(re-frame/dispatch [:navigate-to name])}
                    (clojure.core/name name)])]) (sort screens-categories))]])]))

(def main-screens [{:name      :quo2-preview
                    :insets    {:top false}
                    :component main-screen}])
