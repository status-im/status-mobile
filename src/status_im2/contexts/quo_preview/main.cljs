(ns status-im2.contexts.quo-preview.main
  (:refer-clojure :exclude [filter])
  (:require
    [quo2.core :as quo]
    [reagent.core :as reagent]
    [react-native.core :as rn]
    [status-im2.contexts.quo-preview.style :as style]
    [status-im2.contexts.quo-preview.common :as common]
    [status-im2.contexts.quo-preview.animated-header-list.animated-header-list :as animated-header-list]
    [status-im2.contexts.quo-preview.avatars.account-avatar :as account-avatar]
    [status-im2.contexts.quo-preview.avatars.channel-avatar :as channel-avatar]
    [status-im2.contexts.quo-preview.avatars.group-avatar :as group-avatar]
    [status-im2.contexts.quo-preview.avatars.icon-avatar :as icon-avatar]
    [status-im2.contexts.quo-preview.avatars.user-avatar :as user-avatar]
    [status-im2.contexts.quo-preview.selectors.reactions :as selector-reactions]
    [status-im2.contexts.quo-preview.avatars.wallet-user-avatar :as wallet-user-avatar]
    [status-im2.contexts.quo-preview.banners.banner :as banner]
    [status-im2.contexts.quo-preview.buttons.button :as button]
    [status-im2.contexts.quo-preview.buttons.composer-button :as composer-button]
    [status-im2.contexts.quo-preview.buttons.slide-button :as slide-button]
    [status-im2.contexts.quo-preview.buttons.dynamic-button :as dynamic-button]
    [status-im2.contexts.quo-preview.buttons.predictive-keyboard :as predictive-keyboard]
    [status-im2.contexts.quo-preview.buttons.wallet-button :as wallet-button]
    [status-im2.contexts.quo-preview.buttons.wallet-ctas :as wallet-ctas]
    [status-im2.contexts.quo-preview.calendar.calendar :as calendar]
    [status-im2.contexts.quo-preview.calendar.calendar-day :as calendar-day]
    [status-im2.contexts.quo-preview.calendar.calendar-year :as calendar-year]
    [status-im2.contexts.quo-preview.browser.browser-input :as browser-input]
    [status-im2.contexts.quo-preview.code.snippet :as code-snippet]
    [status-im2.contexts.quo-preview.graph.interactive-graph :as interactive-graph]
    [status-im2.contexts.quo-preview.graph.wallet-graph :as wallet-graph]
    [status-im2.contexts.quo-preview.colors.color-picker :as color-picker]
    [status-im2.contexts.quo-preview.community.community-card-view :as community-card]
    [status-im2.contexts.quo-preview.community.community-membership-list-view :as
     community-membership-list-view]
    [status-im2.contexts.quo-preview.community.discover-card :as discover-card]
    [status-im2.contexts.quo-preview.community.token-gating :as token-gating]
    [status-im2.contexts.quo-preview.counter.counter :as counter]
    [status-im2.contexts.quo-preview.counter.step :as step]
    [status-im2.contexts.quo-preview.dividers.date :as divider-date]
    [status-im2.contexts.quo-preview.dividers.divider-label :as divider-label]
    [status-im2.contexts.quo-preview.dividers.divider-line :as divider-line]
    [status-im2.contexts.quo-preview.dividers.new-messages :as new-messages]
    [status-im2.contexts.quo-preview.dividers.strength-divider :as strength-divider]
    [status-im2.contexts.quo-preview.drawers.action-drawers :as action-drawers]
    [status-im2.contexts.quo-preview.drawers.documentation-drawers :as documentation-drawers]
    [status-im2.contexts.quo-preview.drawers.drawer-buttons :as drawer-buttons]
    [status-im2.contexts.quo-preview.drawers.drawer-top :as drawer-top]
    [status-im2.contexts.quo-preview.drawers.permission-drawers :as permission-drawers]
    [status-im2.contexts.quo-preview.dropdowns.dropdown :as dropdown]
    [status-im2.contexts.quo-preview.dropdowns.network-dropdown :as network-dropdown]
    [status-im2.contexts.quo-preview.foundations.shadows :as shadows]
    [status-im2.contexts.quo-preview.info.info-message :as info-message]
    [status-im2.contexts.quo-preview.info.information-box :as information-box]
    [status-im2.contexts.quo-preview.inputs.input :as input]
    [status-im2.contexts.quo-preview.inputs.address-input :as address-input]
    [status-im2.contexts.quo-preview.inputs.locked-input :as locked-input]
    [status-im2.contexts.quo-preview.inputs.recovery-phrase-input :as recovery-phrase-input]
    [status-im2.contexts.quo-preview.inputs.profile-input :as profile-input]
    [status-im2.contexts.quo-preview.inputs.search-input :as search-input]
    [status-im2.contexts.quo-preview.inputs.title-input :as title-input]
    [status-im2.contexts.quo-preview.numbered-keyboard.keyboard-key :as keyboard-key]
    [status-im2.contexts.quo-preview.numbered-keyboard.numbered-keyboard :as numbered-keyboard]
    [status-im2.contexts.quo-preview.links.url-preview :as url-preview]
    [status-im2.contexts.quo-preview.links.url-preview-list :as url-preview-list]
    [status-im2.contexts.quo-preview.links.link-preview :as link-preview]
    [status-im2.contexts.quo-preview.list-items.account-list-card :as account-list-card]
    [status-im2.contexts.quo-preview.list-items.channel :as channel]
    [status-im2.contexts.quo-preview.list-items.dapp :as dapp]
    [status-im2.contexts.quo-preview.list-items.preview-lists :as preview-lists]
    [status-im2.contexts.quo-preview.list-items.token-value :as token-value]
    [status-im2.contexts.quo-preview.list-items.user-list :as user-list]
    [status-im2.contexts.quo-preview.list-items.community-list :as community-list]
    [status-im2.contexts.quo-preview.markdown.text :as text]
    [status-im2.contexts.quo-preview.markdown.list :as markdown-list]
    [status-im2.contexts.quo-preview.messages.author :as messages-author]
    [status-im2.contexts.quo-preview.messages.gap :as messages-gap]
    [status-im2.contexts.quo-preview.messages.system-message :as system-message]
    [status-im2.contexts.quo-preview.navigation.bottom-nav-tab :as bottom-nav-tab]
    [status-im2.contexts.quo-preview.navigation.floating-shell-button :as floating-shell-button]
    [status-im2.contexts.quo-preview.navigation.page-nav :as page-nav]
    [status-im2.contexts.quo-preview.navigation.top-nav :as top-nav]
    [status-im2.contexts.quo-preview.notifications.activity-logs :as activity-logs]
    [status-im2.contexts.quo-preview.notifications.activity-logs-photos :as activity-logs-photos]
    [status-im2.contexts.quo-preview.notifications.notification :as notification]
    [status-im2.contexts.quo-preview.notifications.toast :as toast]
    [status-im2.contexts.quo-preview.onboarding.small-option-card :as small-option-card]
    [status-im2.contexts.quo-preview.password.tips :as tips]
    [status-im2.contexts.quo-preview.profile.collectible :as collectible]
    [status-im2.contexts.quo-preview.profile.profile-card :as profile-card]
    [status-im2.contexts.quo-preview.profile.select-profile :as select-profile]
    [status-im2.contexts.quo-preview.profile.showcase-nav :as showcase-nav]
    [status-im2.contexts.quo-preview.reactions.react :as react]
    [status-im2.contexts.quo-preview.record-audio.record-audio :as record-audio]
    [status-im2.contexts.quo-preview.selectors.disclaimer :as disclaimer]
    [status-im2.contexts.quo-preview.selectors.filter :as filter]
    [status-im2.contexts.quo-preview.selectors.selectors :as selectors]
    [status-im2.contexts.quo-preview.settings.accounts :as accounts]
    [status-im2.contexts.quo-preview.settings.data-item :as data-item]
    [status-im2.contexts.quo-preview.settings.settings-item :as settings-item]
    [status-im2.contexts.quo-preview.settings.privacy-option :as privacy-option]
    [status-im2.contexts.quo-preview.settings.reorder-item :as reorder-item]
    [status-im2.contexts.quo-preview.settings.category :as category]
    [status-im2.contexts.quo-preview.share.qr-code :as qr-code]
    [status-im2.contexts.quo-preview.share.share-qr-code :as share-qr-code]
    [status-im2.contexts.quo-preview.switcher.switcher-cards :as switcher-cards]
    [status-im2.contexts.quo-preview.tabs.account-selector :as account-selector]
    [status-im2.contexts.quo-preview.tabs.segmented-tab :as segmented]
    [status-im2.contexts.quo-preview.tabs.tabs :as tabs]
    [status-im2.contexts.quo-preview.empty-state.empty-state :as empty-state]
    [status-im2.contexts.quo-preview.tags.context-tags :as context-tags]
    [status-im2.contexts.quo-preview.tags.network-tags :as network-tags]
    [status-im2.contexts.quo-preview.tags.number-tag :as number-tag]
    [status-im2.contexts.quo-preview.tags.permission-tag :as permission-tag]
    [status-im2.contexts.quo-preview.tags.status-tags :as status-tags]
    [status-im2.contexts.quo-preview.tags.tags :as tags]
    [status-im2.contexts.quo-preview.tags.token-tag :as token-tag]
    [status-im2.contexts.quo-preview.text-combinations.preview :as text-combinations]
    [status-im2.contexts.quo-preview.keycard.keycard :as keycard]
    [status-im2.contexts.quo-preview.loaders.skeleton-list :as skeleton-list]
    [status-im2.contexts.quo-preview.community.channel-actions :as channel-actions]
    [status-im2.contexts.quo-preview.gradient.gradient-cover :as gradient-cover]
    [status-im2.contexts.quo-preview.wallet.account-card :as account-card]
    [status-im2.contexts.quo-preview.wallet.account-origin :as account-origin]
    [status-im2.contexts.quo-preview.wallet.account-overview :as account-overview]
    [status-im2.contexts.quo-preview.wallet.keypair :as keypair]
    [status-im2.contexts.quo-preview.wallet.network-amount :as network-amount]
    [status-im2.contexts.quo-preview.wallet.network-bridge :as network-bridge]
    [status-im2.contexts.quo-preview.wallet.network-link :as network-link]
    [status-im2.contexts.quo-preview.wallet.progress-bar :as progress-bar]
    [status-im2.contexts.quo-preview.wallet.summary-info :as summary-info]
    [status-im2.contexts.quo-preview.wallet.token-input :as token-input]
    [status-im2.contexts.quo-preview.wallet.wallet-activity :as wallet-activity]
    [status-im2.contexts.quo-preview.wallet.transaction-summary :as transaction-summary]
    [status-im2.contexts.quo-preview.wallet.wallet-overview :as wallet-overview]
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
                        :component browser-input/preview-browser-input}]
   :calendar          [{:name      :calendar
                        :component calendar/view}
                       {:name      :calendar-day
                        :component calendar-day/view}
                       {:name      :calendar-year
                        :component calendar-year/view}]
   :code              [{:name      :snippet
                        :component code-snippet/view}]
   :colors            [{:name      :color-picker
                        :component color-picker/view}]
   :community         [{:name      :community-card-view
                        :component community-card/view}
                       {:name      :community-membership-list-view
                        :component community-membership-list-view/view}
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
                        :component permission-drawers/view}]
   :dropdowns         [{:name      :dropdown
                        :component dropdown/view}
                       {:name      :network-dropdown
                        :component network-dropdown/view}]
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
   :numbered-keyboard [{:name      :keyboard-key
                        :options   {:insets {:top? true}}
                        :component keyboard-key/preview-keyboard-key}
                       {:name      :numbered-keyboard
                        :options   {:insets {:top? true}}
                        :component numbered-keyboard/preview-numbered-keyboard}]
   :links             [{:name      :url-preview
                        :options   {:insets {:top? true}}
                        :component url-preview/view}
                       {:name      :url-preview-list
                        :options   {:insets {:top? true}}
                        :component url-preview-list/view}
                       {:name      :link-preview
                        :options   {:insets {:top? true}}
                        :component link-preview/view}]
   :list-items        [{:name      :account-list-card
                        :component account-list-card/view}
                       {:name      :channel
                        :component channel/view}
                       {:name      :community-list
                        :options   {:insets {:top? true}}
                        :component community-list/view}
                       {:name      :dapp
                        :component dapp/preview}
                       {:name      :preview-lists
                        :component preview-lists/view}
                       {:name      :token-value
                        :component token-value/preview}
                       {:name      :user-list
                        :options   {:topBar {:visible true}}
                        :component user-list/preview-user-list}]
   :loaders           [{:name      :skeleton-list
                        :options   {:topBar {:visible true}}
                        :component skeleton-list/view}]
   :markdown          [{:name      :texts
                        :component text/view}
                       {:name      :markdown-list
                        :component markdown-list/view}]
   :messages          [{:name      :gap
                        :component messages-gap/preview-messages-gap}
                       {:name      :system-messages
                        :component system-message/preview-system-message}
                       {:name      :author
                        :component messages-author/view}]
   :navigation        [{:name      :bottom-nav-tab
                        :component bottom-nav-tab/preview-bottom-nav-tab}
                       {:name      :top-nav
                        :component top-nav/preview}
                       {:name      :page-nav
                        :component page-nav/preview-page-nav}
                       {:name      :floating-shell-button
                        :component floating-shell-button/preview-floating-shell-button}]
   :notifications     [{:name      :activity-logs
                        :component activity-logs/preview-activity-logs}
                       {:name      :activity-logs-photos
                        :component activity-logs-photos/preview-activity-logs-photos}
                       {:name      :toast
                        :component toast/preview-toasts}
                       {:name      :notification
                        :component notification/preview-notification}]
   :onboarding        [{:name      :small-option-card
                        :component small-option-card/preview-small-option-card}]
   :password          [{:name      :tips
                        :component tips/preview-tips}]
   :profile           [{:name      :profile-card
                        :component profile-card/preview-profile-card}
                       {:name      :collectible
                        :component collectible/preview-collectible}
                       {:name      :select-profile
                        :component select-profile/preview-select-profile}
                       {:name      :showcase-nav
                        :component showcase-nav/view}]
   :reactions         [{:name      :react
                        :component react/preview-react}]
   :record-audio      [{:name      :record-audio
                        :component record-audio/preview-record-audio}]
   :switcher          [{:name      :switcher-cards
                        :component switcher-cards/preview-switcher-cards}]
   :selectors         [{:name      :disclaimer
                        :component disclaimer/preview-disclaimer}
                       {:name      :filter
                        :component filter/preview}
                       {:name      :selectors
                        :component selectors/preview-selectors}
                       {:name      :select-reactions
                        :component selector-reactions/preview}]
   :settings          [{:name      :privacy-option
                        :component privacy-option/preview-options}
                       {:name      :accounts
                        :component accounts/preview-accounts}
                       {:name      :settings-item
                        :component settings-item/preview}
                       {:name      :reorder-item
                        :component reorder-item/preview-reorder-item}
                       {:name      :category
                        :component category/preview}
                       {:name      :data-item
                        :component data-item/preview-data-item}]
   :share             [{:name      :qr-code
                        :component qr-code/preview-qr-code}
                       {:name      :share-qr-code
                        :component share-qr-code/preview-share-qr-code}]
   :tabs              [{:name      :segmented
                        :component segmented/preview-segmented}
                       {:name      :tabs
                        :component tabs/preview-tabs}
                       {:name      :account-selector
                        :component account-selector/preview-this}]
   :tags              [{:name      :context-tags
                        :component context-tags/preview-context-tags}
                       {:name      :network-tags
                        :component network-tags/preview}
                       {:name      :number-tag
                        :component number-tag/preview}
                       {:name      :permission-tag
                        :component permission-tag/preview-permission-tag}
                       {:name      :status-tags
                        :component status-tags/preview-status-tags}
                       {:name      :tags
                        :component tags/preview-tags}
                       {:name      :token-tag
                        :component token-tag/preview-token-tag}]
   :text-combinations [{:name      :text-combinations
                        :component text-combinations/preview}]
   :wallet            [{:name      :account-card
                        :component account-card/preview-account-card}
                       {:name      :account-origin
                        :component account-origin/view}
                       {:name      :account-overview
                        :component account-overview/preview-account-overview}
                       {:name      :keypair
                        :component keypair/preview}
                       {:name      :network-amount
                        :component network-amount/preview}
                       {:name      :network-bridge
                        :component network-bridge/preview}
                       {:name      :network-link
                        :component network-link/preview}
                       {:name      :progress-bar
                        :component progress-bar/preview}
                       {:name      :summary-info
                        :component summary-info/preview}
                       {:name      :token-input
                        :component token-input/preview}
                       {:name      :wallet-activity
                        :component wallet-activity/view}
                       {:name      :transaction-summary
                        :component transaction-summary/view}
                       {:name      :wallet-overview
                        :component wallet-overview/preview-wallet-overview}]
   :keycard           [{:name      :keycard-component
                        :component keycard/view}]})

(defn- category-view
  []
  (let [open?     (reagent/atom false)
        on-change #(swap! open? not)]
    (fn [category]
      [rn/view {:style {:margin-vertical 8}}
       [quo/dropdown
        {:selected  @open?
         :on-change on-change
         :type      :grey}
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
   [common/navigation-bar]
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
  [{:name      :quo2-preview
    :options   {:topBar {:visible false}
                :insets {:top? true}}
    :component main-screen}])
