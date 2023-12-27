(ns quo.core
  (:refer-clojure :exclude [filter])
  (:require
    quo.components.animated-header-flatlist.view
    quo.components.avatars.account-avatar.view
    quo.components.avatars.channel-avatar.view
    quo.components.avatars.collection-avatar.view
    quo.components.avatars.group-avatar.view
    quo.components.avatars.icon-avatar
    quo.components.avatars.user-avatar.view
    quo.components.avatars.wallet-user-avatar.view
    quo.components.banners.banner.view
    quo.components.browser.browser-input.view
    quo.components.buttons.button.view
    quo.components.buttons.composer-button.view
    quo.components.buttons.dynamic-button.view
    quo.components.buttons.logout-button.view
    quo.components.buttons.predictive-keyboard.view
    quo.components.buttons.slide-button.view
    quo.components.buttons.wallet-button.view
    quo.components.buttons.wallet-ctas.view
    quo.components.calendar.calendar-day.view
    quo.components.calendar.calendar-year.view
    quo.components.calendar.calendar.view
    quo.components.code.snippet-preview.view
    quo.components.code.snippet.view
    quo.components.colors.color-picker.view
    quo.components.colors.color.view
    quo.components.common.notification-dot.view
    quo.components.common.separator.view
    quo.components.community.banner.view
    quo.components.community.channel-actions
    quo.components.community.community-card-view
    quo.components.community.community-list-view
    quo.components.community.community-stat.view
    quo.components.community.community-view
    quo.components.community.icon
    quo.components.community.token-gating
    quo.components.counter.counter.view
    quo.components.counter.step.view
    quo.components.dividers.date
    quo.components.dividers.divider-label.view
    quo.components.dividers.divider-line.view
    quo.components.dividers.new-messages
    quo.components.dividers.strength-divider.view
    quo.components.drawers.action-drawers.view
    quo.components.drawers.bottom-actions.view
    quo.components.drawers.documentation-drawers.view
    quo.components.drawers.drawer-buttons.view
    quo.components.drawers.drawer-top.view
    quo.components.drawers.permission-context.view
    quo.components.dropdowns.dropdown-input.view
    quo.components.dropdowns.dropdown.view
    quo.components.dropdowns.network-dropdown.view
    quo.components.empty-state.empty-state.view
    quo.components.gradient.gradient-cover.view
    [quo.components.graph.interactive-graph.view :as interactive-graph]
    quo.components.graph.wallet-graph.view
    quo.components.header
    quo.components.icon
    quo.components.info.info-message
    quo.components.info.information-box.view
    quo.components.inputs.address-input.view
    quo.components.inputs.input.view
    quo.components.inputs.locked-input.view
    quo.components.inputs.profile-input.view
    quo.components.inputs.recovery-phrase.view
    quo.components.inputs.search-input.view
    quo.components.inputs.title-input.view
    quo.components.ios.drawer-bar.view
    quo.components.keycard.view
    quo.components.links.link-preview.view
    quo.components.links.url-preview-list.view
    quo.components.links.url-preview.view
    quo.components.list-items.account-list-card.view
    quo.components.list-items.account.view
    quo.components.list-items.address.view
    quo.components.list-items.channel.view
    quo.components.list-items.community.view
    quo.components.list-items.dapp.view
    quo.components.list-items.menu-item
    quo.components.list-items.preview-list.view
    quo.components.list-items.quiz-item.view
    quo.components.list-items.saved-address.view
    quo.components.list-items.saved-contact-address.view
    quo.components.list-items.token-network.view
    quo.components.list-items.token-value.view
    quo.components.list-items.user-list
    quo.components.loaders.skeleton-list.view
    quo.components.markdown.list.view
    quo.components.markdown.text
    quo.components.messages.author.view
    quo.components.messages.gap
    quo.components.messages.system-message.view
    quo.components.navigation.bottom-nav-tab.view
    quo.components.navigation.floating-shell-button.view
    quo.components.navigation.page-nav.view
    quo.components.navigation.top-nav.view
    quo.components.notifications.activity-log.view
    quo.components.notifications.activity-logs-photos.view
    quo.components.notifications.count-down-circle
    quo.components.notifications.notification.view
    quo.components.notifications.toast.view
    quo.components.numbered-keyboard.keyboard-key.view
    quo.components.numbered-keyboard.numbered-keyboard.view
    quo.components.onboarding.small-option-card.view
    quo.components.overlay.view
    quo.components.password.tips.view
    quo.components.profile.collectible.view
    quo.components.profile.link-card.view
    quo.components.profile.profile-card.view
    quo.components.profile.select-profile.view
    quo.components.profile.showcase-nav.view
    quo.components.record-audio.record-audio.view
    quo.components.record-audio.soundtrack.view
    quo.components.selectors.disclaimer.view
    quo.components.selectors.filter.view
    quo.components.selectors.react-selector.view
    quo.components.selectors.react.view
    quo.components.selectors.reactions-selector.view
    quo.components.selectors.selectors.view
    quo.components.settings.accounts.view
    quo.components.settings.category.view
    quo.components.settings.data-item.view
    quo.components.settings.privacy-option.view
    quo.components.settings.reorder-item.view
    quo.components.settings.section-label.view
    quo.components.settings.settings-item.view
    quo.components.share.qr-code.view
    quo.components.share.share-qr-code.view
    quo.components.switchers.group-messaging-card.view
    quo.components.tabs.account-selector
    quo.components.tabs.segmented-tab
    quo.components.tabs.tabs.view
    quo.components.tags.context-tag.view
    quo.components.tags.network-tags.view
    quo.components.tags.number-tag.view
    quo.components.tags.permission-tag
    quo.components.tags.status-tags
    quo.components.tags.summary-tag.view
    quo.components.tags.tag
    quo.components.tags.tags
    quo.components.tags.tiny-tag.view
    quo.components.tags.token-tag.view
    quo.components.text-combinations.channel-name.view
    quo.components.text-combinations.page-top.view
    quo.components.text-combinations.standard-title.view
    quo.components.text-combinations.username.view
    quo.components.text-combinations.view
    quo.components.utilities.token.view
    quo.components.wallet.account-card.view
    quo.components.wallet.account-origin.view
    quo.components.wallet.account-overview.view
    quo.components.wallet.account-permissions.view
    quo.components.wallet.address-text.view
    quo.components.wallet.confirmation-progress.view
    quo.components.wallet.keypair.view
    quo.components.wallet.network-amount.view
    quo.components.wallet.network-bridge.view
    quo.components.wallet.network-link.view
    quo.components.wallet.network-routing.view
    quo.components.wallet.progress-bar.view
    quo.components.wallet.required-tokens.view
    quo.components.wallet.summary-info.view
    quo.components.wallet.token-input.view
    quo.components.wallet.transaction-progress.view
    quo.components.wallet.transaction-summary.view
    quo.components.wallet.wallet-activity.view
    quo.components.wallet.wallet-overview.view))

(def separator quo.components.common.separator.view/separator)

;;;; Avatar
(def account-avatar quo.components.avatars.account-avatar.view/view)
(def channel-avatar quo.components.avatars.channel-avatar.view/view)
(def collection-avatar quo.components.avatars.collection-avatar.view/view)
(def group-avatar quo.components.avatars.group-avatar.view/view)
(def icon-avatar quo.components.avatars.icon-avatar/icon-avatar)
(def user-avatar quo.components.avatars.user-avatar.view/user-avatar)
(def wallet-user-avatar quo.components.avatars.wallet-user-avatar.view/wallet-user-avatar)

;;;; Banner
(def banner quo.components.banners.banner.view/view)

;;;; Buttons
(def button quo.components.buttons.button.view/button)
(def composer-button quo.components.buttons.composer-button.view/view)
(def dynamic-button quo.components.buttons.dynamic-button.view/view)
(def logout-button quo.components.buttons.logout-button.view/view)
(def predictive-keyboard quo.components.buttons.predictive-keyboard.view/view)
(def slide-button quo.components.buttons.slide-button.view/view)
(def wallet-button quo.components.buttons.wallet-button.view/view)
(def wallet-ctas quo.components.buttons.wallet-ctas.view/view)

;;;; Browser
(def browser-input quo.components.browser.browser-input.view/view)

;;;; Calendar
(def calendar quo.components.calendar.calendar.view/view)
(def calendar-day quo.components.calendar.calendar-day.view/view)
(def calendar-year quo.components.calendar.calendar-year.view/view)

;;;; Code
(def snippet quo.components.code.snippet.view/view)
(def snippet-preview quo.components.code.snippet-preview.view/view)

;;;; Cards
(def small-option-card quo.components.onboarding.small-option-card.view/small-option-card)
(def keycard quo.components.keycard.view/keycard)

;;;; Colors
(def color-picker quo.components.colors.color-picker.view/view)
(def color quo.components.colors.color.view/view)

;;;; Community
(def community-card-view-item quo.components.community.community-card-view/view)
(def communities-membership-list-item
  quo.components.community.community-list-view/communities-membership-list-item)
(def community-stats-column quo.components.community.community-view/community-stats-column)
(def community-stat quo.components.community.community-stat.view/view)
(def community-tags quo.components.community.community-view/community-tags)
(def community-title quo.components.community.community-view/community-title)
(def permission-tag-container quo.components.community.community-view/permission-tag-container)
(def discover-card quo.components.community.banner.view/view)
(def community-icon quo.components.community.icon/community-icon)
(def token-requirement-list quo.components.community.token-gating/token-requirement-list)
(def channel-actions quo.components.community.channel-actions/channel-actions)

;;;; Counter
(def counter quo.components.counter.counter.view/view)
(def step #'quo.components.counter.step.view/view)

;;;; Dividers
(def divider-label quo.components.dividers.divider-label.view/view)
(def divider-line quo.components.dividers.divider-line.view/view)
(def new-messages quo.components.dividers.new-messages/view)
(def divider-date quo.components.dividers.date/date)
(def strength-divider quo.components.dividers.strength-divider.view/view)

;;;; Drawers
(def action-drawer quo.components.drawers.action-drawers.view/action-drawer)
(def documentation-drawers quo.components.drawers.documentation-drawers.view/view)
(def drawer-buttons quo.components.drawers.drawer-buttons.view/view)
(def drawer-top quo.components.drawers.drawer-top.view/view)
(def permission-context quo.components.drawers.permission-context.view/view)
(def bottom-actions quo.components.drawers.bottom-actions.view/view)

;;;; Dropdowns
(def dropdown quo.components.dropdowns.dropdown.view/view)
(def dropdown-input quo.components.dropdowns.dropdown-input.view/view)
(def network-dropdown quo.components.dropdowns.network-dropdown.view/view)

;;;; Empty State
(def empty-state quo.components.empty-state.empty-state.view/empty-state)

;;;; Graph
(def interactive-graph quo.components.graph.interactive-graph.view/view)
(def wallet-graph quo.components.graph.wallet-graph.view/view)

;;;; Header
(def header quo.components.header/header)
(def animated-header-list quo.components.animated-header-flatlist.view/animated-header-list)

;;;; Gradient
(def gradient-cover quo.components.gradient.gradient-cover.view/view)

;;;; Icon
(def icon quo.components.icon/icon)

;;;; Info
(def info-message quo.components.info.info-message/info-message)
(def information-box quo.components.info.information-box.view/view)

;;;; Inputs
(def input quo.components.inputs.input.view/input)
(def address-input quo.components.inputs.address-input.view/address-input)
(def locked-input quo.components.inputs.locked-input.view/locked-input)
(def profile-input quo.components.inputs.profile-input.view/profile-input)
(def recovery-phrase-input quo.components.inputs.recovery-phrase.view/recovery-phrase-input)
(def search-input quo.components.inputs.search-input.view/search-input)
(def title-input quo.components.inputs.title-input.view/view)

;;;; iOS
(def drawer-bar quo.components.ios.drawer-bar.view/view)

;;;; Numbered Keyboard
(def keyboard-key quo.components.numbered-keyboard.keyboard-key.view/view)
(def numbered-keyboard quo.components.numbered-keyboard.numbered-keyboard.view/view)

;;;; Links
(def link-preview quo.components.links.link-preview.view/view)
(def url-preview quo.components.links.url-preview.view/view)
(def url-preview-list quo.components.links.url-preview-list.view/view)

;;;; List items
(def account-item quo.components.list-items.account.view/view)
(def account-list-card quo.components.list-items.account-list-card.view/view)
(def address quo.components.list-items.address.view/view)
(def channel quo.components.list-items.channel.view/view)
(def dapp quo.components.list-items.dapp.view/view)
(def menu-item quo.components.list-items.menu-item/menu-item)
(def preview-list quo.components.list-items.preview-list.view/view)
(def user-list quo.components.list-items.user-list/user-list)
(def community-list-item quo.components.list-items.community.view/view)
(def saved-address quo.components.list-items.saved-address.view/view)
(def saved-contact-address quo.components.list-items.saved-contact-address.view/view)
(def token-network quo.components.list-items.token-network.view/view)
(def token-value quo.components.list-items.token-value.view/view)
(def quiz-item quo.components.list-items.quiz-item.view/view)

;;;; Loaders
(def skeleton-list quo.components.loaders.skeleton-list.view/view)

;;;; Navigation
(def bottom-nav-tab quo.components.navigation.bottom-nav-tab.view/view)
(def floating-shell-button quo.components.navigation.floating-shell-button.view/view)
(def page-nav quo.components.navigation.page-nav.view/page-nav)
(def top-nav quo.components.navigation.top-nav.view/view)

;;;; Markdown
(def markdown-list quo.components.markdown.list.view/view)
(def text quo.components.markdown.text/text)

;;;; Messages
(def author quo.components.messages.author.view/view)
(def gap quo.components.messages.gap/gap)
(def system-message quo.components.messages.system-message.view/system-message)

;;;; Notifications
(def activity-log quo.components.notifications.activity-log.view/view)
(def activity-logs-photos quo.components.notifications.activity-logs-photos.view/view)
(def notification-dot quo.components.common.notification-dot.view/view)
(def count-down-circle quo.components.notifications.count-down-circle/circle-timer)
(def notification quo.components.notifications.notification.view/notification)
(def toast quo.components.notifications.toast.view/toast)

;;;; Overlay
(def overlay quo.components.overlay.view/view)

;;;; Password
(def tips quo.components.password.tips.view/view)

;;;; Profile
(def collectible quo.components.profile.collectible.view/collectible)
(def link-card quo.components.profile.link-card.view/view)
(def profile-card quo.components.profile.profile-card.view/profile-card)
(def select-profile quo.components.profile.select-profile.view/view)
(def showcase-nav quo.components.profile.showcase-nav.view/view)

;;;; Record Audio
(def record-audio quo.components.record-audio.record-audio.view/record-audio)
(def soundtrack quo.components.record-audio.soundtrack.view/f-soundtrack)

;;;; Selectors
(def disclaimer quo.components.selectors.disclaimer.view/view)
(def filter quo.components.selectors.filter.view/view)
(def reactions-selector quo.components.selectors.reactions-selector.view/view)
(def react quo.components.selectors.react.view/view)
(def react-selector quo.components.selectors.react-selector.view/view)
(def selectors quo.components.selectors.selectors.view/view)

;;;; Settings
(def account quo.components.settings.accounts.view/account)
(def category quo.components.settings.category.view/category)
(def data-item quo.components.settings.data-item.view/view)
(def privacy-option quo.components.settings.privacy-option.view/view)
(def reorder-item quo.components.settings.reorder-item.view/reorder-item)
(def section-label quo.components.settings.section-label.view/view)
(def settings-item quo.components.settings.settings-item.view/view)

;;;; Share
(def qr-code quo.components.share.qr-code.view/view)
(def share-qr-code quo.components.share.share-qr-code.view/view)

;;;; SWITCHER
(def group-messaging-card quo.components.switchers.group-messaging-card.view/view)

;;;; Tabs
(def tabs quo.components.tabs.tabs.view/view)
(def segmented-control quo.components.tabs.segmented-tab/segmented-control)
(def account-selector quo.components.tabs.account-selector/account-selector)

;;;; Tags
(def context-tag quo.components.tags.context-tag.view/view)
(def network-tags quo.components.tags.network-tags.view/view)
(def number-tag quo.components.tags.number-tag.view/view)
(def permission-tag quo.components.tags.permission-tag/tag)
(def status-tag quo.components.tags.status-tags/status-tag)
(def summary-tag quo.components.tags.summary-tag.view/view)
(def tag quo.components.tags.tag/tag)
(def tags quo.components.tags.tags/tags)
(def tiny-tag quo.components.tags.tiny-tag.view/view)
(def token-tag quo.components.tags.token-tag.view/view)

;;;; Text combinations
(def channel-name quo.components.text-combinations.channel-name.view/view)
(def page-top quo.components.text-combinations.page-top.view/view)
(def standard-title quo.components.text-combinations.standard-title.view/view)
(def text-combinations quo.components.text-combinations.view/view)
(def username quo.components.text-combinations.username.view/view)

;;;; Utilities - Outside of design system
(def token quo.components.utilities.token.view/view)

;;;; Wallet
(def account-card quo.components.wallet.account-card.view/view)
(def account-origin quo.components.wallet.account-origin.view/view)
(def account-overview quo.components.wallet.account-overview.view/view)
(def account-permissions quo.components.wallet.account-permissions.view/view)
(def address-text quo.components.wallet.address-text.view/view)
(def confirmation-propgress quo.components.wallet.confirmation-progress.view/view)
(def keypair quo.components.wallet.keypair.view/view)
(def network-amount quo.components.wallet.network-amount.view/view)
(def network-bridge quo.components.wallet.network-bridge.view/view)
(def network-routing quo.components.wallet.network-routing.view/view)
(def progress-bar quo.components.wallet.progress-bar.view/view)
(def required-tokens quo.components.wallet.required-tokens.view/view)
(def summary-info quo.components.wallet.summary-info.view/view)
(def network-link quo.components.wallet.network-link.view/view)
(def token-input quo.components.wallet.token-input.view/view)
(def wallet-overview quo.components.wallet.wallet-overview.view/view)
(def wallet-activity quo.components.wallet.wallet-activity.view/view)
(def transaction-progress quo.components.wallet.transaction-progress.view/view)
(def transaction-summary quo.components.wallet.transaction-summary.view/view)
