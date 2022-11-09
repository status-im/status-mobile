(ns quo2.screens.main
  (:require [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [quo2.components.markdown.text :as quo2-text]
            [quo2.components.buttons.button :as quo2-button]
            [quo2.screens.avatars.channel-avatar :as channel-avatar]
            [quo2.screens.avatars.icon-avatar :as icon-avatar]
            [quo2.screens.avatars.group-avatar :as group-avatar]
            [quo2.screens.avatars.user-avatar :as user-avatar]
            [quo2.screens.avatars.wallet-user-avatar :as wallet-user-avatar]
            [quo2.screens.buttons.button :as button]
            [quo2.screens.buttons.dynamic-button :as dynamic-button]
            [quo2.screens.code.snippet :as code-snippet]
            [quo2.screens.counter.counter :as counter]
            [quo2.screens.community.community-card-view :as community-card]
            [quo2.screens.community.discover-card :as discover-card]
            [quo2.screens.community.community-list-view :as community-list-view]
            [quo2.screens.dividers.divider-label :as divider-label]
            [quo2.screens.dividers.new-messages :as new-messages]
            [quo2.screens.drawers.action-drawers :as drawers]
            [quo2.screens.dropdowns.dropdown :as dropdown]
            [quo2.screens.info.info-message :as info-message]
            [quo2.screens.info.information-box :as information-box]
            [quo2.screens.wallet.lowest-price :as lowest-price]
            [quo2.screens.list-items.preview-lists :as preview-lists]
            [quo2.screens.list-items.channel :as channel]
            [quo2.screens.markdown.text :as text]
            [quo2.screens.messages.gap :as messages-gap]
            [quo2.screens.messages.system-message :as system-message]
            [quo2.screens.notifications.activity-logs :as activity-logs]
            [quo2.screens.reactions.react :as react]
            [quo2.screens.selectors.disclaimer :as disclaimer]
            [quo2.screens.selectors.selectors :as selectors]
            [quo2.screens.switcher.switcher-cards :as switcher-cards]
            [quo2.screens.navigation.top-nav :as top-nav]
            [quo2.screens.navigation.bottom-nav-tab :as bottom-nav-tab]
            [quo2.screens.navigation.floating-shell-button :as floating-shell-button]
            [quo2.screens.tabs.account-selector :as account-selector]
            [quo2.screens.tabs.segmented-tab :as segmented]
            [quo2.screens.tabs.tabs :as tabs]
            [quo2.screens.tags.context-tags :as context-tags]
            [quo2.screens.tags.tags :as tags]
            [quo2.screens.tags.permission-tag :as permission-tag]
            [quo2.screens.tags.status-tags :as status-tags]
            [quo2.screens.tags.token-tag :as token-tag]
            [quo2.screens.wallet.token-overview :as token-overview]
            [quo2.screens.wallet.network-breakdown :as network-breakdown]
            [quo2.screens.wallet.network-amount :as network-amount]
            [quo2.screens.navigation.page-nav :as page-nav]
            [quo2.screens.avatars.account-avatar :as account-avatar]
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
