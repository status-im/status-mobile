(ns status-im.contexts.preview-screens.quo-preview.main
  (:refer-clojure :exclude [filter])
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.animated-header-list.animated-header-list
     :as animated-header-list]
    [status-im.contexts.preview-screens.quo-preview.avatars.account-avatar :as account-avatar]
    [status-im.contexts.preview-screens.quo-preview.avatars.channel-avatar :as channel-avatar]
    [status-im.contexts.preview-screens.quo-preview.avatars.collection-avatar :as collection-avatar]
    [status-im.contexts.preview-screens.quo-preview.avatars.group-avatar :as group-avatar]
    [status-im.contexts.preview-screens.quo-preview.avatars.icon-avatar :as icon-avatar]
    [status-im.contexts.preview-screens.quo-preview.avatars.user-avatar :as user-avatar]
    [status-im.contexts.preview-screens.quo-preview.avatars.wallet-user-avatar :as
     wallet-user-avatar]
    [status-im.contexts.preview-screens.quo-preview.banners.banner :as banner]
    [status-im.contexts.preview-screens.quo-preview.browser.browser-input :as browser-input]
    [status-im.contexts.preview-screens.quo-preview.buttons.button :as button]
    [status-im.contexts.preview-screens.quo-preview.buttons.composer-button :as
     composer-button]
    [status-im.contexts.preview-screens.quo-preview.buttons.dynamic-button :as dynamic-button]
    [status-im.contexts.preview-screens.quo-preview.buttons.predictive-keyboard :as
     predictive-keyboard]
    [status-im.contexts.preview-screens.quo-preview.buttons.slide-button :as slide-button]
    [status-im.contexts.preview-screens.quo-preview.buttons.wallet-button :as wallet-button]
    [status-im.contexts.preview-screens.quo-preview.buttons.wallet-ctas :as wallet-ctas]
    [status-im.contexts.preview-screens.quo-preview.calendar.calendar :as calendar]
    [status-im.contexts.preview-screens.quo-preview.calendar.calendar-day :as calendar-day]
    [status-im.contexts.preview-screens.quo-preview.calendar.calendar-year :as calendar-year]
    [status-im.contexts.preview-screens.quo-preview.code.snippet :as code-snippet]
    [status-im.contexts.preview-screens.quo-preview.code.snippet-preview :as code-snippet-preview]
    [status-im.contexts.preview-screens.quo-preview.colors.color :as color]
    [status-im.contexts.preview-screens.quo-preview.colors.color-picker :as color-picker]
    [status-im.contexts.preview-screens.quo-preview.common :as common]
    [status-im.contexts.preview-screens.quo-preview.community.channel-actions :as
     channel-actions]
    [status-im.contexts.preview-screens.quo-preview.community.community-card-view :as
     community-card]
    [status-im.contexts.preview-screens.quo-preview.community.community-membership-list-view
     :as community-membership-list-view]
    [status-im.contexts.preview-screens.quo-preview.community.community-stat :as community-stat]
    [status-im.contexts.preview-screens.quo-preview.community.discover-card :as discover-card]
    [status-im.contexts.preview-screens.quo-preview.community.token-gating :as token-gating]
    [status-im.contexts.preview-screens.quo-preview.counter.counter :as counter]
    [status-im.contexts.preview-screens.quo-preview.counter.step :as step]
    [status-im.contexts.preview-screens.quo-preview.dividers.date :as divider-date]
    [status-im.contexts.preview-screens.quo-preview.dividers.divider-label :as divider-label]
    [status-im.contexts.preview-screens.quo-preview.dividers.divider-line :as divider-line]
    [status-im.contexts.preview-screens.quo-preview.dividers.new-messages :as new-messages]
    [status-im.contexts.preview-screens.quo-preview.dividers.strength-divider :as
     strength-divider]
    [status-im.contexts.preview-screens.quo-preview.drawers.action-drawers :as action-drawers]
    [status-im.contexts.preview-screens.quo-preview.drawers.bottom-actions :as bottom-actions]
    [status-im.contexts.preview-screens.quo-preview.drawers.documentation-drawers :as
     documentation-drawers]
    [status-im.contexts.preview-screens.quo-preview.drawers.drawer-buttons :as drawer-buttons]
    [status-im.contexts.preview-screens.quo-preview.drawers.drawer-top :as drawer-top]
    [status-im.contexts.preview-screens.quo-preview.drawers.permission-drawers :as
     permission-drawers]
    [status-im.contexts.preview-screens.quo-preview.dropdowns.dropdown :as dropdown]
    [status-im.contexts.preview-screens.quo-preview.dropdowns.dropdown-input :as
     dropdown-input]
    [status-im.contexts.preview-screens.quo-preview.dropdowns.network-dropdown :as
     network-dropdown]
    [status-im.contexts.preview-screens.quo-preview.empty-state.empty-state :as empty-state]
    [status-im.contexts.preview-screens.quo-preview.foundations.shadows :as shadows]
    [status-im.contexts.preview-screens.quo-preview.gradient.gradient-cover :as gradient-cover]
    [status-im.contexts.preview-screens.quo-preview.graph.interactive-graph :as
     interactive-graph]
    [status-im.contexts.preview-screens.quo-preview.graph.wallet-graph :as wallet-graph]
    [status-im.contexts.preview-screens.quo-preview.info.info-message :as info-message]
    [status-im.contexts.preview-screens.quo-preview.info.information-box :as information-box]
    [status-im.contexts.preview-screens.quo-preview.inputs.address-input :as address-input]
    [status-im.contexts.preview-screens.quo-preview.inputs.input :as input]
    [status-im.contexts.preview-screens.quo-preview.inputs.locked-input :as locked-input]
    [status-im.contexts.preview-screens.quo-preview.inputs.profile-input :as profile-input]
    [status-im.contexts.preview-screens.quo-preview.inputs.recovery-phrase-input :as
     recovery-phrase-input]
    [status-im.contexts.preview-screens.quo-preview.inputs.search-input :as search-input]
    [status-im.contexts.preview-screens.quo-preview.inputs.title-input :as title-input]
    [status-im.contexts.preview-screens.quo-preview.ios.drawer-bar :as drawer-bar]
    [status-im.contexts.preview-screens.quo-preview.keycard.keycard :as keycard]
    [status-im.contexts.preview-screens.quo-preview.links.link-preview :as link-preview]
    [status-im.contexts.preview-screens.quo-preview.links.url-preview :as url-preview]
    [status-im.contexts.preview-screens.quo-preview.links.url-preview-list :as
     url-preview-list]
    [status-im.contexts.preview-screens.quo-preview.list-items.account :as
     account-item]
    [status-im.contexts.preview-screens.quo-preview.list-items.account-list-card :as
     account-list-card]
    [status-im.contexts.preview-screens.quo-preview.list-items.address :as address]
    [status-im.contexts.preview-screens.quo-preview.list-items.channel :as channel]
    [status-im.contexts.preview-screens.quo-preview.list-items.community-list :as
     community-list]
    [status-im.contexts.preview-screens.quo-preview.list-items.dapp :as dapp]
    [status-im.contexts.preview-screens.quo-preview.list-items.preview-lists :as preview-lists]
    [status-im.contexts.preview-screens.quo-preview.list-items.saved-address :as saved-address]
    [status-im.contexts.preview-screens.quo-preview.list-items.saved-contact-address :as
     saved-contact-address]
    [status-im.contexts.preview-screens.quo-preview.list-items.token-network :as token-network]
    [status-im.contexts.preview-screens.quo-preview.list-items.token-value :as token-value]
    [status-im.contexts.preview-screens.quo-preview.list-items.user-list :as user-list]
    [status-im.contexts.preview-screens.quo-preview.loaders.skeleton-list :as skeleton-list]
    [status-im.contexts.preview-screens.quo-preview.markdown.list :as markdown-list]
    [status-im.contexts.preview-screens.quo-preview.markdown.text :as text]
    [status-im.contexts.preview-screens.quo-preview.messages.author :as messages-author]
    [status-im.contexts.preview-screens.quo-preview.messages.gap :as messages-gap]
    [status-im.contexts.preview-screens.quo-preview.messages.system-message :as system-message]
    [status-im.contexts.preview-screens.quo-preview.navigation.bottom-nav-tab :as
     bottom-nav-tab]
    [status-im.contexts.preview-screens.quo-preview.navigation.floating-shell-button :as
     floating-shell-button]
    [status-im.contexts.preview-screens.quo-preview.navigation.page-nav :as page-nav]
    [status-im.contexts.preview-screens.quo-preview.navigation.top-nav :as top-nav]
    [status-im.contexts.preview-screens.quo-preview.notifications.activity-logs :as
     activity-logs]
    [status-im.contexts.preview-screens.quo-preview.notifications.activity-logs-photos :as
     activity-logs-photos]
    [status-im.contexts.preview-screens.quo-preview.notifications.notification :as
     notification]
    [status-im.contexts.preview-screens.quo-preview.notifications.toast :as toast]
    [status-im.contexts.preview-screens.quo-preview.numbered-keyboard.keyboard-key :as
     keyboard-key]
    [status-im.contexts.preview-screens.quo-preview.numbered-keyboard.numbered-keyboard :as
     numbered-keyboard]
    [status-im.contexts.preview-screens.quo-preview.onboarding.small-option-card :as
     small-option-card]
    [status-im.contexts.preview-screens.quo-preview.password.tips :as tips]
    [status-im.contexts.preview-screens.quo-preview.profile.collectible :as collectible]
    [status-im.contexts.preview-screens.quo-preview.profile.link-card :as link-card]
    [status-im.contexts.preview-screens.quo-preview.profile.profile-card :as profile-card]
    [status-im.contexts.preview-screens.quo-preview.profile.select-profile :as select-profile]
    [status-im.contexts.preview-screens.quo-preview.profile.showcase-nav :as showcase-nav]
    [status-im.contexts.preview-screens.quo-preview.record-audio.record-audio :as record-audio]
    [status-im.contexts.preview-screens.quo-preview.selectors.disclaimer :as disclaimer]
    [status-im.contexts.preview-screens.quo-preview.selectors.filter :as filter]
    [status-im.contexts.preview-screens.quo-preview.selectors.react :as react]
    [status-im.contexts.preview-screens.quo-preview.selectors.react-selector :as react-selector]
    [status-im.contexts.preview-screens.quo-preview.selectors.reactions-selector :as reactions-selector]
    [status-im.contexts.preview-screens.quo-preview.selectors.selectors :as selectors]
    [status-im.contexts.preview-screens.quo-preview.settings.accounts :as accounts]
    [status-im.contexts.preview-screens.quo-preview.settings.category :as category]
    [status-im.contexts.preview-screens.quo-preview.settings.data-item :as data-item]
    [status-im.contexts.preview-screens.quo-preview.settings.privacy-option :as privacy-option]
    [status-im.contexts.preview-screens.quo-preview.settings.reorder-item :as reorder-item]
    [status-im.contexts.preview-screens.quo-preview.settings.section-label :as section-label]
    [status-im.contexts.preview-screens.quo-preview.settings.settings-item :as settings-item]
    [status-im.contexts.preview-screens.quo-preview.share.qr-code :as qr-code]
    [status-im.contexts.preview-screens.quo-preview.share.share-qr-code :as share-qr-code]
    [status-im.contexts.preview-screens.quo-preview.style :as style]
    [status-im.contexts.preview-screens.quo-preview.switcher.group-messaging-card :as
     group-messaging-card]
    [status-im.contexts.preview-screens.quo-preview.switcher.switcher-cards :as switcher-cards]
    [status-im.contexts.preview-screens.quo-preview.tabs.account-selector :as account-selector]
    [status-im.contexts.preview-screens.quo-preview.tabs.segmented-tab :as segmented]
    [status-im.contexts.preview-screens.quo-preview.tabs.tabs :as tabs]
    [status-im.contexts.preview-screens.quo-preview.tags.context-tags :as context-tags]
    [status-im.contexts.preview-screens.quo-preview.tags.network-tags :as network-tags]
    [status-im.contexts.preview-screens.quo-preview.tags.number-tag :as number-tag]
    [status-im.contexts.preview-screens.quo-preview.tags.permission-tag :as permission-tag]
    [status-im.contexts.preview-screens.quo-preview.tags.status-tags :as status-tags]
    [status-im.contexts.preview-screens.quo-preview.tags.summary-tag :as summary-tag]
    [status-im.contexts.preview-screens.quo-preview.tags.tag :as tag]
    [status-im.contexts.preview-screens.quo-preview.tags.tags :as tags]
    [status-im.contexts.preview-screens.quo-preview.tags.tiny-tag :as tiny-tag]
    [status-im.contexts.preview-screens.quo-preview.tags.token-tag :as token-tag]
    [status-im.contexts.preview-screens.quo-preview.text-combinations.channel-name :as channel-name]
    [status-im.contexts.preview-screens.quo-preview.text-combinations.page-top :as page-top]
    [status-im.contexts.preview-screens.quo-preview.text-combinations.preview :as text-combinations]
    [status-im.contexts.preview-screens.quo-preview.text-combinations.standard-title :as standard-title]
    [status-im.contexts.preview-screens.quo-preview.text-combinations.username :as username]
    [status-im.contexts.preview-screens.quo-preview.wallet.account-card :as account-card]
    [status-im.contexts.preview-screens.quo-preview.wallet.account-origin :as account-origin]
    [status-im.contexts.preview-screens.quo-preview.wallet.account-overview :as
     account-overview]
    [status-im.contexts.preview-screens.quo-preview.wallet.account-permissions :as account-permissions]
    [status-im.contexts.preview-screens.quo-preview.wallet.confirmation-progress :as
     confirmation-progress]
    [status-im.contexts.preview-screens.quo-preview.wallet.keypair :as keypair]
    [status-im.contexts.preview-screens.quo-preview.wallet.network-amount :as network-amount]
    [status-im.contexts.preview-screens.quo-preview.wallet.network-bridge :as network-bridge]
    [status-im.contexts.preview-screens.quo-preview.wallet.network-link :as network-link]
    [status-im.contexts.preview-screens.quo-preview.wallet.network-routing :as network-routing]
    [status-im.contexts.preview-screens.quo-preview.wallet.progress-bar :as progress-bar]
    [status-im.contexts.preview-screens.quo-preview.wallet.required-tokens :as required-tokens]
    [status-im.contexts.preview-screens.quo-preview.wallet.summary-info :as summary-info]
    [status-im.contexts.preview-screens.quo-preview.wallet.token-input :as token-input]
    [status-im.contexts.preview-screens.quo-preview.wallet.transaction-progress :as transaction-progress]
    [status-im.contexts.preview-screens.quo-preview.wallet.transaction-summary :as
     transaction-summary]
    [status-im.contexts.preview-screens.quo-preview.wallet.wallet-activity :as wallet-activity]
    [status-im.contexts.preview-screens.quo-preview.wallet.wallet-overview :as wallet-overview]
    [utils.re-frame :as rf]))

(def screens-categories
  {:foundations       [{:name      :shadows
                        :component shadows/view}]
   :animated-list     [{:name      :animated-header-list
                        :component animated-header-list/mock-screen}]
   :avatar            [{:name      :group-avatar
                        :component group-avatar/view}
                       {:name      :icon-avatar
                        :component icon-avatar/view}
                       {:name      :user-avatar
                        :component user-avatar/view}
                       {:name      :wallet-user-avatar
                        :component wallet-user-avatar/view}
                       {:name      :channel-avatar
                        :component channel-avatar/view}
                       {:name      :collection-avatar
                        :component collection-avatar/view}
                       {:name      :account-avatar
                        :component account-avatar/view}]
   :banner            [{:name      :banner
                        :component banner/view}]
   :buttons           [{:name      :button
                        :component button/view}
                       {:name      :composer-button
                        :component composer-button/view}
                       {:name      :dynamic-button
                        :component dynamic-button/view}
                       {:name      :slide-button
                        :component slide-button/view}
                       {:name      :predictive-keyboard
                        :component predictive-keyboard/view}
                       {:name      :wallet-button
                        :component wallet-button/view}
                       {:name      :wallet-ctas
                        :component wallet-ctas/view}]
   :browser           [{:name      :browser-input
                        :component browser-input/view}]
   :calendar          [{:name      :calendar
                        :component calendar/view}
                       {:name      :calendar-day
                        :component calendar-day/view}
                       {:name      :calendar-year
                        :component calendar-year/view}]
   :code              [{:name      :snippet
                        :component code-snippet/view}
                       {:name      :snippet-preview
                        :component code-snippet-preview/view}]
   :colors            [{:name      :color-picker
                        :component color-picker/view}
                       {:name      :color
                        :component color/view}]
   :community         [{:name      :community-card-view
                        :component community-card/view}
                       {:name      :community-membership-list-view
                        :component community-membership-list-view/view}
                       {:name      :community-stat
                        :component community-stat/view}
                       {:name      :discover-card
                        :component discover-card/view}
                       {:name      :token-gating
                        :options   {:insets {:bottom? true}}
                        :component token-gating/view}
                       {:name      :channel-actions
                        :options   {:insets {:bottom? true}}
                        :component channel-actions/view}]
   :counter           [{:name      :counter
                        :component counter/view}
                       {:name      :step
                        :component step/view}]
   :dividers          [{:name      :divider-label
                        :component divider-label/view}
                       {:name      :divider-line
                        :component divider-line/view}
                       {:name      :new-messages
                        :component new-messages/view}
                       {:name      :divider-date
                        :component divider-date/view}
                       {:name      :strength-divider
                        :component strength-divider/view}]
   :drawers           [{:name      :action-drawers
                        :component action-drawers/view}
                       {:name      :documentation-drawer
                        :component documentation-drawers/view}
                       {:name      :drawer-buttons
                        :component drawer-buttons/view}
                       {:name      :drawer-top
                        :component drawer-top/view}
                       {:name      :permission-drawers
                        :component permission-drawers/view}
                       {:name :bottom-actions
                        :component
                        bottom-actions/view}]
   :dropdowns         [{:name      :dropdown
                        :component dropdown/view}
                       {:name      :network-dropdown
                        :component network-dropdown/view}
                       {:name      :dropdown-input
                        :component dropdown-input/view}]
   :empty-state       [{:name      :empty-state
                        :component empty-state/view}]
   :gradient          [{:name      :gradient-cover
                        :component gradient-cover/view}]
   :graph             [{:name      :interactive-graph
                        :options   {:topBar {:visible true}}
                        :component interactive-graph/view}
                       {:name      :wallet-graph
                        :options   {:topBar {:visible true}}
                        :component wallet-graph/view}]
   :info              [{:name      :info-message
                        :component info-message/view}
                       {:name      :information-box
                        :component information-box/view}]
   :inputs            [{:name      :input
                        :component input/view}
                       {:name      :address-input
                        :component address-input/view}
                       {:name      :locked-input
                        :component locked-input/view}
                       {:name      :profile-input
                        :component profile-input/view}
                       {:name      :recovery-phrase-input
                        :component recovery-phrase-input/view}
                       {:name      :search-input
                        :component search-input/view}
                       {:name      :title-input
                        :component title-input/view}]
   :ios               [{:name      :drawer-bar
                        :component drawer-bar/view}]
   :numbered-keyboard [{:name      :keyboard-key
                        :component keyboard-key/view}
                       {:name      :numbered-keyboard
                        :component numbered-keyboard/view}]
   :links             [{:name      :url-preview
                        :options   {:insets {:top? true}}
                        :component url-preview/view}
                       {:name      :url-preview-list
                        :options   {:insets {:top? true}}
                        :component url-preview-list/view}
                       {:name      :link-preview
                        :options   {:insets {:top? true}}
                        :component link-preview/view}]
   :list-items        [{:name      :account
                        :component account-item/view}
                       {:name      :account-list-card
                        :component account-list-card/view}
                       {:name      :address
                        :component address/view}
                       {:name      :channel
                        :component channel/view}
                       {:name      :community-list
                        :options   {:insets {:top? true}}
                        :component community-list/view}
                       {:name      :dapp
                        :component dapp/preview}
                       {:name      :preview-lists
                        :component preview-lists/view}
                       {:name      :saved-address
                        :component saved-address/view}
                       {:name      :saved-contact-address
                        :component saved-contact-address/view}
                       {:name      :token-network
                        :component token-network/view}
                       {:name      :token-value
                        :component token-value/view}
                       {:name      :user-list
                        :options   {:topBar {:visible true}}
                        :component user-list/view}]
   :loaders           [{:name      :skeleton-list
                        :options   {:topBar {:visible true}}
                        :component skeleton-list/view}]
   :markdown          [{:name      :texts
                        :component text/view}
                       {:name      :markdown-list
                        :component markdown-list/view}]
   :messages          [{:name      :author
                        :component messages-author/view}
                       {:name      :gap
                        :component messages-gap/view}
                       {:name      :system-messages
                        :component system-message/view}]
   :navigation        [{:name      :bottom-nav-tab
                        :component bottom-nav-tab/view}
                       {:name      :top-nav
                        :component top-nav/view}
                       {:name      :page-nav
                        :component page-nav/view}
                       {:name      :floating-shell-button
                        :component floating-shell-button/view}]
   :notifications     [{:name      :activity-logs
                        :component activity-logs/view}
                       {:name      :activity-logs-photos
                        :component activity-logs-photos/view}
                       {:name      :toast
                        :component toast/view}
                       {:name      :notification
                        :component notification/view}]
   :onboarding        [{:name      :small-option-card
                        :component small-option-card/view}]
   :password          [{:name      :tips
                        :component tips/view}]
   :profile           [{:name      :collectible
                        :component collectible/view}
                       {:name      :link-card
                        :component link-card/view}
                       {:name      :profile-card
                        :component profile-card/view}
                       {:name      :select-profile
                        :component select-profile/view}
                       {:name      :showcase-nav
                        :component showcase-nav/view}]
   :record-audio      [{:name      :record-audio
                        :component record-audio/view}]
   :selectors         [{:name      :disclaimer
                        :component disclaimer/view}
                       {:name      :filter
                        :component filter/view}
                       {:name      :selectors
                        :component selectors/view}
                       {:name      :reactions-selector
                        :component reactions-selector/preview}
                       {:name      :react-selector
                        :component (react-selector/preview-react-selector)}
                       {:name      :react
                        :component react/preview-react}]
   :settings          [{:name      :privacy-option
                        :component privacy-option/view}
                       {:name      :accounts
                        :component accounts/view}
                       {:name      :settings-item
                        :component settings-item/view}
                       {:name      :reorder-item
                        :component reorder-item/view}
                       {:name      :category
                        :component category/view}
                       {:name      :data-item
                        :component data-item/view}
                       {:name      :section-label
                        :component section-label/view}]
   :share             [{:name      :qr-code
                        :component qr-code/view}
                       {:name      :share-qr-code
                        :component share-qr-code/view}]
   :switchers         [{:name      :group-messaging-card
                        :component group-messaging-card/view}
                       {:name      :switcher-cards
                        :component switcher-cards/view}]
   :tabs              [{:name      :segmented
                        :component segmented/view}
                       {:name      :tabs
                        :component tabs/view}
                       {:name      :account-selector
                        :component account-selector/view}]
   :tags              [{:name      :context-tags
                        :component context-tags/view}
                       {:name      :network-tags
                        :component network-tags/view}
                       {:name      :number-tag
                        :component number-tag/view}
                       {:name      :permission-tag
                        :component permission-tag/view}
                       {:name      :status-tags
                        :component status-tags/view}
                       {:name      :summary-tag
                        :component summary-tag/view}
                       {:name      :tag
                        :component tag/view}
                       {:name      :tags
                        :component tags/view}
                       {:name      :tiny-tag
                        :component tiny-tag/view}
                       {:name      :token-tag
                        :component token-tag/view}]
   :text-combinations [{:name      :text-combinations
                        :component text-combinations/view}
                       {:name      :channel-name
                        :component channel-name/view}
                       {:name      :page-top
                        :component page-top/view}
                       {:name      :standard-title
                        :component standard-title/view}
                       {:name      :username
                        :component username/view}]
   :wallet            [{:name :account-card :component account-card/view}
                       {:name :account-origin :component account-origin/view}
                       {:name      :account-overview
                        :component account-overview/view}
                       {:name      :account-permissions
                        :component account-permissions/view}
                       {:name      :confirmation-progress
                        :component confirmation-progress/view}
                       {:name :keypair :component keypair/view}
                       {:name :network-amount :component network-amount/view}
                       {:name :network-bridge :component network-bridge/view}
                       {:name :network-link :component network-link/view}
                       {:name :network-routing :component network-routing/view}
                       {:name :progress-bar :component progress-bar/view}
                       {:name      :required-tokens
                        :component required-tokens/view}
                       {:name :summary-info :component summary-info/view}
                       {:name :token-input :component token-input/view}
                       {:name :wallet-activity :component wallet-activity/view}
                       {:name :transaction-progress :component transaction-progress/view}
                       {:name :transaction-summary :component transaction-summary/view}
                       {:name      :wallet-overview
                        :component wallet-overview/view}]
   :keycard           [{:name :keycard-component :component keycard/view}]})

(defn- category-view
  []
  (let [open?    (reagent/atom false)
        on-press #(swap! open? not)]
    (fn [category]
      [rn/view {:style {:margin-vertical 8}}
       [quo/dropdown
        {:type     :grey
         :state    (if @open? :active :default)
         :on-press on-press}
        (name (key category))]
       (when @open?
         (for [{category-name :name} (val category)]
           ^{:key category-name}
           [quo/button
            {:type            :outline
             :container-style {:margin-vertical 8}
             :on-press        #(rf/dispatch [:navigate-to category-name])}
            (name category-name)]))])))

(defn- main-screen
  []
  [:<>
   [common/navigation-bar {:title "Quo components preview"}]
   [rn/scroll-view {:style (style/main)}
    (for [category (sort screens-categories)]
      ^{:key (first category)}
      [category-view category])]])

(def screens
  (->> screens-categories
       (map val)
       flatten
       (map (fn [subcategory]
              (update-in subcategory
                         [:options :topBar]
                         merge
                         {:visible false})))))

(def main-screens
  [{:name      :quo-preview
    :options   {:topBar {:visible false}
                :insets {:top? true}}
    :component main-screen}])
