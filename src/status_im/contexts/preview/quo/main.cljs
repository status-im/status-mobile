(ns status-im.contexts.preview.quo.main
  (:refer-clojure :exclude [filter])
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.animated-header-list.animated-header-list
     :as animated-header-list]
    [status-im.contexts.preview.quo.avatars.account-avatar :as account-avatar]
    [status-im.contexts.preview.quo.avatars.channel-avatar :as channel-avatar]
    [status-im.contexts.preview.quo.avatars.collection-avatar :as collection-avatar]
    [status-im.contexts.preview.quo.avatars.community-avatar :as community-avatar]
    [status-im.contexts.preview.quo.avatars.dapp-avatar :as dapp-avatar]
    [status-im.contexts.preview.quo.avatars.group-avatar :as group-avatar]
    [status-im.contexts.preview.quo.avatars.icon-avatar :as icon-avatar]
    [status-im.contexts.preview.quo.avatars.token-avatar :as token-avatar]
    [status-im.contexts.preview.quo.avatars.user-avatar :as user-avatar]
    [status-im.contexts.preview.quo.avatars.wallet-user-avatar :as
     wallet-user-avatar]
    [status-im.contexts.preview.quo.banners.alert-banner :as alert-banner]
    [status-im.contexts.preview.quo.banners.banner :as banner]
    [status-im.contexts.preview.quo.browser.browser-input :as browser-input]
    [status-im.contexts.preview.quo.buttons.button :as button]
    [status-im.contexts.preview.quo.buttons.composer-button :as
     composer-button]
    [status-im.contexts.preview.quo.buttons.dynamic-button :as dynamic-button]
    [status-im.contexts.preview.quo.buttons.predictive-keyboard :as
     predictive-keyboard]
    [status-im.contexts.preview.quo.buttons.slide-button :as slide-button]
    [status-im.contexts.preview.quo.buttons.swap-order-button :as swap-order-button]
    [status-im.contexts.preview.quo.buttons.wallet-button :as wallet-button]
    [status-im.contexts.preview.quo.buttons.wallet-ctas :as wallet-ctas]
    [status-im.contexts.preview.quo.calendar.calendar :as calendar]
    [status-im.contexts.preview.quo.calendar.calendar-day :as calendar-day]
    [status-im.contexts.preview.quo.calendar.calendar-year :as calendar-year]
    [status-im.contexts.preview.quo.code.snippet :as code-snippet]
    [status-im.contexts.preview.quo.code.snippet-preview :as code-snippet-preview]
    [status-im.contexts.preview.quo.colors.color :as color]
    [status-im.contexts.preview.quo.colors.color-picker :as color-picker]
    [status-im.contexts.preview.quo.common :as common]
    [status-im.contexts.preview.quo.community.channel-action :as
     channel-action]
    [status-im.contexts.preview.quo.community.channel-actions :as
     channel-actions]
    [status-im.contexts.preview.quo.community.community-card-view :as
     community-card]
    [status-im.contexts.preview.quo.community.community-detail-token-gating :as
     community-detail-token-gating]
    [status-im.contexts.preview.quo.community.community-membership-list-view
     :as community-membership-list-view]
    [status-im.contexts.preview.quo.community.community-stat :as community-stat]
    [status-im.contexts.preview.quo.community.community-token-gating :as community-token-gating]
    [status-im.contexts.preview.quo.community.discover-card :as discover-card]
    [status-im.contexts.preview.quo.community.list-item :as community-list-item]
    [status-im.contexts.preview.quo.counter.collectible-counter :as collectible-counter]
    [status-im.contexts.preview.quo.counter.counter :as counter]
    [status-im.contexts.preview.quo.counter.step :as step]
    [status-im.contexts.preview.quo.dividers.date :as divider-date]
    [status-im.contexts.preview.quo.dividers.divider-label :as divider-label]
    [status-im.contexts.preview.quo.dividers.divider-line :as divider-line]
    [status-im.contexts.preview.quo.dividers.new-messages :as new-messages]
    [status-im.contexts.preview.quo.dividers.strength-divider :as
     strength-divider]
    [status-im.contexts.preview.quo.drawers.action-drawers :as action-drawers]
    [status-im.contexts.preview.quo.drawers.bottom-actions :as bottom-actions]
    [status-im.contexts.preview.quo.drawers.documentation-drawers :as
     documentation-drawers]
    [status-im.contexts.preview.quo.drawers.drawer-action :as drawer-action]
    [status-im.contexts.preview.quo.drawers.drawer-buttons :as drawer-buttons]
    [status-im.contexts.preview.quo.drawers.drawer-top :as drawer-top]
    [status-im.contexts.preview.quo.drawers.permission-drawers :as
     permission-drawers]
    [status-im.contexts.preview.quo.dropdowns.dropdown :as dropdown]
    [status-im.contexts.preview.quo.dropdowns.dropdown-input :as
     dropdown-input]
    [status-im.contexts.preview.quo.dropdowns.network-dropdown :as
     network-dropdown]
    [status-im.contexts.preview.quo.empty-state.empty-state :as empty-state]
    [status-im.contexts.preview.quo.foundations.gradients :as gradients]
    [status-im.contexts.preview.quo.foundations.shadows :as shadows]
    [status-im.contexts.preview.quo.gradient.gradient-cover :as gradient-cover]
    [status-im.contexts.preview.quo.graph.interactive-graph :as
     interactive-graph]
    [status-im.contexts.preview.quo.graph.wallet-graph :as wallet-graph]
    [status-im.contexts.preview.quo.info.info-message :as info-message]
    [status-im.contexts.preview.quo.info.information-box :as information-box]
    [status-im.contexts.preview.quo.inputs.address-input :as address-input]
    [status-im.contexts.preview.quo.inputs.input :as input]
    [status-im.contexts.preview.quo.inputs.locked-input :as locked-input]
    [status-im.contexts.preview.quo.inputs.profile-input :as profile-input]
    [status-im.contexts.preview.quo.inputs.recovery-phrase-input :as
     recovery-phrase-input]
    [status-im.contexts.preview.quo.inputs.search-input :as search-input]
    [status-im.contexts.preview.quo.inputs.title-input :as title-input]
    [status-im.contexts.preview.quo.ios.drawer-bar :as drawer-bar]
    [status-im.contexts.preview.quo.keycard.keycard :as keycard]
    [status-im.contexts.preview.quo.links.internal-link-card :as internal-link-card]
    [status-im.contexts.preview.quo.links.link-preview :as link-preview]
    [status-im.contexts.preview.quo.links.url-preview :as url-preview]
    [status-im.contexts.preview.quo.links.url-preview-list :as
     url-preview-list]
    [status-im.contexts.preview.quo.list-items.account :as
     account-item]
    [status-im.contexts.preview.quo.list-items.account-list-card :as
     account-list-card]
    [status-im.contexts.preview.quo.list-items.address :as address]
    [status-im.contexts.preview.quo.list-items.approval-info :as approval-info]
    [status-im.contexts.preview.quo.list-items.channel :as channel]
    [status-im.contexts.preview.quo.list-items.dapp :as dapp]
    [status-im.contexts.preview.quo.list-items.missing-keypair :as missing-keypair]
    [status-im.contexts.preview.quo.list-items.network-list :as network-list]
    [status-im.contexts.preview.quo.list-items.preview-lists :as preview-lists]
    [status-im.contexts.preview.quo.list-items.quiz-item :as quiz-item]
    [status-im.contexts.preview.quo.list-items.saved-address :as saved-address]
    [status-im.contexts.preview.quo.list-items.saved-contact-address :as
     saved-contact-address]
    [status-im.contexts.preview.quo.list-items.token-network :as token-network]
    [status-im.contexts.preview.quo.list-items.token-value :as token-value]
    [status-im.contexts.preview.quo.list-items.user-list :as user-list]
    [status-im.contexts.preview.quo.loaders.skeleton-list :as skeleton-list]
    [status-im.contexts.preview.quo.markdown.list :as markdown-list]
    [status-im.contexts.preview.quo.markdown.text :as text]
    [status-im.contexts.preview.quo.messages.author :as messages-author]
    [status-im.contexts.preview.quo.messages.gap :as messages-gap]
    [status-im.contexts.preview.quo.messages.system-message :as system-message]
    [status-im.contexts.preview.quo.navigation.bottom-nav-tab :as
     bottom-nav-tab]
    [status-im.contexts.preview.quo.navigation.floating-shell-button :as
     floating-shell-button]
    [status-im.contexts.preview.quo.navigation.page-nav :as page-nav]
    [status-im.contexts.preview.quo.navigation.top-nav :as top-nav]
    [status-im.contexts.preview.quo.notifications.activity-logs :as
     activity-logs]
    [status-im.contexts.preview.quo.notifications.activity-logs-photos :as
     activity-logs-photos]
    [status-im.contexts.preview.quo.notifications.notification :as
     notification]
    [status-im.contexts.preview.quo.notifications.toast :as toast]
    [status-im.contexts.preview.quo.numbered-keyboard.keyboard-key :as
     keyboard-key]
    [status-im.contexts.preview.quo.numbered-keyboard.numbered-keyboard :as
     numbered-keyboard]
    [status-im.contexts.preview.quo.onboarding.small-option-card :as
     small-option-card]
    [status-im.contexts.preview.quo.password.password-tips :as password-tips]
    [status-im.contexts.preview.quo.password.tips :as tips]
    [status-im.contexts.preview.quo.pin-input.pin-input :as pin-input]
    [status-im.contexts.preview.quo.profile.collectible :as collectible]
    [status-im.contexts.preview.quo.profile.collectible-list-item :as collectible-list-item]
    [status-im.contexts.preview.quo.profile.expanded-collectible :as expanded-collectible]
    [status-im.contexts.preview.quo.profile.link-card :as link-card]
    [status-im.contexts.preview.quo.profile.profile-card :as profile-card]
    [status-im.contexts.preview.quo.profile.select-profile :as select-profile]
    [status-im.contexts.preview.quo.profile.showcase-nav :as showcase-nav]
    [status-im.contexts.preview.quo.record-audio.record-audio :as record-audio]
    [status-im.contexts.preview.quo.selectors.disclaimer :as disclaimer]
    [status-im.contexts.preview.quo.selectors.filter :as filter]
    [status-im.contexts.preview.quo.selectors.react :as react]
    [status-im.contexts.preview.quo.selectors.react-selector :as react-selector]
    [status-im.contexts.preview.quo.selectors.reactions-selector :as reactions-selector]
    [status-im.contexts.preview.quo.selectors.selectors :as selectors]
    [status-im.contexts.preview.quo.settings.accounts :as accounts]
    [status-im.contexts.preview.quo.settings.category :as category]
    [status-im.contexts.preview.quo.settings.data-item :as data-item]
    [status-im.contexts.preview.quo.settings.page-setting :as page-setting]
    [status-im.contexts.preview.quo.settings.privacy-option :as privacy-option]
    [status-im.contexts.preview.quo.settings.reorder-item :as reorder-item]
    [status-im.contexts.preview.quo.settings.section-label :as section-label]
    [status-im.contexts.preview.quo.settings.settings-item :as settings-item]
    [status-im.contexts.preview.quo.share.qr-code :as qr-code]
    [status-im.contexts.preview.quo.share.share-qr-code :as share-qr-code]
    [status-im.contexts.preview.quo.slideshow.slider-bar :as slider-bar]
    [status-im.contexts.preview.quo.style :as style]
    [status-im.contexts.preview.quo.switcher.group-messaging-card :as
     group-messaging-card]
    [status-im.contexts.preview.quo.switcher.switcher-cards :as switcher-cards]
    [status-im.contexts.preview.quo.tabs.account-selector :as account-selector]
    [status-im.contexts.preview.quo.tabs.segmented-tab :as segmented]
    [status-im.contexts.preview.quo.tabs.tabs :as tabs]
    [status-im.contexts.preview.quo.tags.collectible-tag :as collectible-tag]
    [status-im.contexts.preview.quo.tags.context-tags :as context-tags]
    [status-im.contexts.preview.quo.tags.network-status-tag :as network-status-tag]
    [status-im.contexts.preview.quo.tags.network-tags :as network-tags]
    [status-im.contexts.preview.quo.tags.number-tag :as number-tag]
    [status-im.contexts.preview.quo.tags.permission-tag :as permission-tag]
    [status-im.contexts.preview.quo.tags.status-tags :as status-tags]
    [status-im.contexts.preview.quo.tags.summary-tag :as summary-tag]
    [status-im.contexts.preview.quo.tags.tag :as tag]
    [status-im.contexts.preview.quo.tags.tags :as tags]
    [status-im.contexts.preview.quo.tags.tiny-tag :as tiny-tag]
    [status-im.contexts.preview.quo.tags.token-tag :as token-tag]
    [status-im.contexts.preview.quo.text-combinations.channel-name :as channel-name]
    [status-im.contexts.preview.quo.text-combinations.page-top :as page-top]
    [status-im.contexts.preview.quo.text-combinations.preview :as text-combinations]
    [status-im.contexts.preview.quo.text-combinations.standard-title :as standard-title]
    [status-im.contexts.preview.quo.text-combinations.username :as username]
    [status-im.contexts.preview.quo.wallet.account-card :as account-card]
    [status-im.contexts.preview.quo.wallet.account-origin :as account-origin]
    [status-im.contexts.preview.quo.wallet.account-overview :as
     account-overview]
    [status-im.contexts.preview.quo.wallet.account-permissions :as account-permissions]
    [status-im.contexts.preview.quo.wallet.amount-input :as amount-input]
    [status-im.contexts.preview.quo.wallet.approval-label :as approval-label]
    [status-im.contexts.preview.quo.wallet.confirmation-progress :as
     confirmation-progress]
    [status-im.contexts.preview.quo.wallet.keypair :as keypair]
    [status-im.contexts.preview.quo.wallet.missing-keypairs :as missing-keypairs]
    [status-im.contexts.preview.quo.wallet.network-amount :as network-amount]
    [status-im.contexts.preview.quo.wallet.network-bridge :as network-bridge]
    [status-im.contexts.preview.quo.wallet.network-link :as network-link]
    [status-im.contexts.preview.quo.wallet.network-routing :as network-routing]
    [status-im.contexts.preview.quo.wallet.progress-bar :as progress-bar]
    [status-im.contexts.preview.quo.wallet.required-tokens :as required-tokens]
    [status-im.contexts.preview.quo.wallet.summary-info :as summary-info]
    [status-im.contexts.preview.quo.wallet.swap-input :as swap-input]
    [status-im.contexts.preview.quo.wallet.token-input :as token-input]
    [status-im.contexts.preview.quo.wallet.transaction-progress :as transaction-progress]
    [status-im.contexts.preview.quo.wallet.transaction-summary :as
     transaction-summary]
    [status-im.contexts.preview.quo.wallet.wallet-activity :as wallet-activity]
    [status-im.contexts.preview.quo.wallet.wallet-overview :as wallet-overview]
    [utils.re-frame :as rf]))

(def screens-categories
  {:foundations       [{:name      :gradients
                        :component gradients/view}
                       {:name      :shadows
                        :component shadows/view}]
   :animated-list     [{:name      :animated-header-list
                        :component animated-header-list/mock-screen}]
   :avatar            [{:name      :community-avatar
                        :component community-avatar/view}
                       {:name      :dapp-avatar
                        :component dapp-avatar/view}
                       {:name      :group-avatar
                        :component group-avatar/view}
                       {:name      :icon-avatar
                        :component icon-avatar/view}
                       {:name      :token-avatar
                        :component token-avatar/view}
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
   :banner            [{:name      :alert-banners
                        :component alert-banner/view}
                       {:name      :banner
                        :component banner/view}]
   :buttons           [{:name      :button
                        :component button/view}
                       {:name      :composer-button
                        :component composer-button/view}
                       {:name      :dynamic-button
                        :component dynamic-button/view}
                       {:name      :slide-button
                        :component slide-button/view}
                       {:name      :swap-order-button
                        :component swap-order-button/view}
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
                       {:name      :community-detail-token-gating
                        :component community-detail-token-gating/view}
                       {:name      :community-token-gating
                        :component community-token-gating/view}
                       {:name      :community-membership-list-view
                        :component community-membership-list-view/view}
                       {:name      :community-stat
                        :component community-stat/view}
                       {:name      :discover-card
                        :component discover-card/view}
                       {:name      :channel-action
                        :options   {:insets {:bottom? true}}
                        :component channel-action/view}
                       {:name      :channel-actions
                        :options   {:insets {:bottom? true}}
                        :component channel-actions/view}]
   :counter           [{:name      :collectible-counter
                        :component collectible-counter/view}
                       {:name      :counter
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
                       {:name      :drawer-action
                        :component drawer-action/view}
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
   :pin-input         [{:name      :pin-input
                        :component pin-input/view}]
   :links             [{:name      :internal-link-card
                        :options   {:insets {:top true}}
                        :component internal-link-card/view}
                       {:name      :url-preview
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
                       {:name      :approval-info
                        :component approval-info/view}
                       {:name      :channel
                        :component channel/view}
                       {:name      :community-list
                        :options   {:insets {:top? true}}
                        :component community-list-item/view}
                       {:name      :dapp
                        :component dapp/preview}
                       {:name      :missing-keypair
                        :component missing-keypair/view}
                       {:name      :network-list
                        :component network-list/view}
                       {:name      :preview-lists
                        :component preview-lists/view}
                       {:name      :quiz-item
                        :component quiz-item/view}
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
                        :component tips/view}
                       {:name      :password-tips
                        :component password-tips/view}]
   :profile           [{:name      :collectible
                        :component collectible/view}
                       {:name      :collectible-list-item
                        :component collectible-list-item/view}
                       {:name      :expanded-collectible
                        :component expanded-collectible/view}
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
                       {:name      :page-setting
                        :component page-setting/view}
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
   :slideshow         [{:name      :slider-bar
                        :component slider-bar/view}]
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
   :tags              [{:name      :collectible-tag
                        :component collectible-tag/view}
                       {:name      :context-tags
                        :component context-tags/view}
                       {:name      :network-status-tag
                        :component network-status-tag/view}
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
                       {:name      :amount-input
                        :component amount-input/view}
                       {:name      :approval-label
                        :component approval-label/view}
                       {:name      :confirmation-progress
                        :component confirmation-progress/view}
                       {:name :keypair :component keypair/view}
                       {:name      :missing-keypairs
                        :component missing-keypairs/view}
                       {:name :network-amount :component network-amount/view}
                       {:name :network-bridge :component network-bridge/view}
                       {:name :network-link :component network-link/view}
                       {:name :network-routing :component network-routing/view}
                       {:name :progress-bar :component progress-bar/view}
                       {:name      :required-tokens
                        :component required-tokens/view}
                       {:name :summary-info :component summary-info/view}
                       {:name :swap-input :component swap-input/view}
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
             :on-press        #(rf/dispatch [:open-modal category-name])}
            (name category-name)]))])))

(defn- main-screen
  []
  (let [theme (quo.theme/use-theme)]
    [:<>
     [common/navigation-bar {:title "Quo components preview"}]
     [rn/scroll-view {:style (style/main theme)}
      (for [category (sort screens-categories)]
        ^{:key (first category)}
        [category-view category])]]))

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
