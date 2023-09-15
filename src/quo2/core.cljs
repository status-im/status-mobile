(ns quo2.core
  (:refer-clojure :exclude [filter])
  (:require
    quo2.components.avatars.account-avatar.view
    quo2.components.avatars.channel-avatar.view
    quo2.components.avatars.group-avatar.view
    quo2.components.avatars.icon-avatar
    quo2.components.avatars.user-avatar.view
    quo2.components.avatars.wallet-user-avatar
    quo2.components.banners.banner.view
    quo2.components.buttons.button.view
    quo2.components.buttons.composer-button.view
    quo2.components.buttons.dynamic-button.view
    quo2.components.buttons.predictive-keyboard.view
    quo2.components.buttons.slide-button.view
    quo2.components.buttons.wallet-button.view
    quo2.components.buttons.wallet-ctas.view
    quo2.components.browser.browser-input.view
    quo2.components.calendar.calendar.view
    quo2.components.calendar.calendar-day.view
    quo2.components.calendar.calendar-year.view
    quo2.components.code.snippet
    quo2.components.colors.color-picker.view
    quo2.components.common.notification-dot.view
    quo2.components.common.separator.view
    quo2.components.community.banner.view
    quo2.components.community.channel-actions
    quo2.components.community.community-card-view
    quo2.components.community.community-list-view
    quo2.components.community.community-view
    quo2.components.community.icon
    quo2.components.community.token-gating
    quo2.components.counter.counter.view
    quo2.components.counter.step.view
    quo2.components.dividers.date
    quo2.components.dividers.divider-label.view
    quo2.components.dividers.divider-line.view
    quo2.components.dividers.new-messages
    quo2.components.dividers.strength-divider.view
    quo2.components.drawers.action-drawers.view
    quo2.components.drawers.documentation-drawers.view
    quo2.components.drawers.drawer-buttons.view
    quo2.components.drawers.drawer-top.view
    quo2.components.drawers.permission-context.view
    quo2.components.dropdowns.dropdown
    quo2.components.dropdowns.network-dropdown.view
    quo2.components.empty-state.empty-state.view
    quo2.components.gradient.gradient-cover.view
    quo2.components.graph.wallet-graph.view
    quo2.components.header
    quo2.components.icon
    quo2.components.info.info-message
    quo2.components.info.information-box.view
    quo2.components.inputs.input.view
    quo2.components.inputs.address-input.view
    quo2.components.inputs.locked-input.view
    quo2.components.inputs.profile-input.view
    quo2.components.inputs.recovery-phrase.view
    quo2.components.inputs.search-input.view
    quo2.components.inputs.title-input.view
    quo2.components.keycard.view
    quo2.components.links.link-preview.view
    quo2.components.links.url-preview-list.view
    quo2.components.links.url-preview.view
    quo2.components.list-items.account-list-card.view
    quo2.components.list-items.channel.view
    quo2.components.list-items.community.view
    quo2.components.list-items.dapp.view
    quo2.components.list-items.menu-item
    quo2.components.list-items.preview-list.view
    quo2.components.list-items.token-value.view
    quo2.components.list-items.user-list
    quo2.components.loaders.skeleton-list.view
    quo2.components.markdown.list.view
    quo2.components.markdown.text
    quo2.components.messages.author.view
    quo2.components.messages.gap
    quo2.components.messages.system-message
    quo2.components.navigation.floating-shell-button.view
    quo2.components.navigation.page-nav.view
    quo2.components.navigation.top-nav.view
    quo2.components.notifications.activity-log.view
    quo2.components.notifications.activity-logs-photos.view
    quo2.components.notifications.count-down-circle
    quo2.components.notifications.notification.view
    quo2.components.notifications.toast.view
    quo2.components.numbered-keyboard.keyboard-key.view
    quo2.components.numbered-keyboard.numbered-keyboard.view
    quo2.components.onboarding.small-option-card.view
    quo2.components.password.tips.view
    quo2.components.profile.profile-card.view
    quo2.components.profile.select-profile.view
    quo2.components.profile.showcase-nav.view
    quo2.components.reactions.reaction
    quo2.components.record-audio.record-audio.view
    quo2.components.record-audio.soundtrack.view
    quo2.components.selectors.disclaimer.view
    quo2.components.selectors.filter.view
    quo2.components.selectors.reactions.view
    quo2.components.selectors.selectors.view
    quo2.components.settings.accounts.view
    quo2.components.settings.data-item.view
    quo2.components.settings.privacy-option
    quo2.components.settings.reorder-item.view
    quo2.components.settings.settings-item.view
    quo2.components.settings.category.view
    quo2.components.share.qr-code.view
    quo2.components.share.share-qr-code.view
    quo2.components.tabs.account-selector
    quo2.components.tabs.segmented-tab
    quo2.components.tabs.tabs.view
    quo2.components.tags.context-tag.view
    quo2.components.tags.network-tags.view
    quo2.components.tags.number-tag.view
    quo2.components.tags.permission-tag
    quo2.components.tags.status-tags
    quo2.components.tags.tag
    quo2.components.tags.tags
    quo2.components.tags.token-tag
    quo2.components.text-combinations.view
    quo2.components.wallet.account-card.view
    quo2.components.wallet.account-origin.view
    quo2.components.wallet.account-overview.view
    quo2.components.wallet.keypair.view
    quo2.components.wallet.network-amount.view
    quo2.components.wallet.network-bridge.view
    quo2.components.wallet.network-link.view
    quo2.components.wallet.progress-bar.view
    quo2.components.wallet.summary-info.view
    quo2.components.wallet.token-input.view
    quo2.components.wallet.transaction-summary.view
    quo2.components.wallet.wallet-activity.view
    quo2.components.wallet.wallet-overview.view
    [quo2.components.graph.interactive-graph.view :as interactive-graph]))

(def separator quo2.components.common.separator.view/separator)

;;;; Avatar
(def account-avatar quo2.components.avatars.account-avatar.view/view)
(def channel-avatar quo2.components.avatars.channel-avatar.view/view)
(def group-avatar quo2.components.avatars.group-avatar.view/view)
(def icon-avatar quo2.components.avatars.icon-avatar/icon-avatar)
(def user-avatar quo2.components.avatars.user-avatar.view/user-avatar)
(def wallet-user-avatar quo2.components.avatars.wallet-user-avatar/wallet-user-avatar)

;;;; Banner
(def banner quo2.components.banners.banner.view/view)

;;;; Buttons
(def button quo2.components.buttons.button.view/button)
(def composer-button quo2.components.buttons.composer-button.view/view)
(def dynamic-button quo2.components.buttons.dynamic-button.view/view)
(def predictive-keyboard quo2.components.buttons.predictive-keyboard.view/view)
(def slide-button quo2.components.buttons.slide-button.view/view)
(def wallet-button quo2.components.buttons.wallet-button.view/view)
(def wallet-ctas quo2.components.buttons.wallet-ctas.view/view)

;;;; Browser
(def browser-input quo2.components.browser.browser-input.view/view)

;;;; Calendar
(def calendar quo2.components.calendar.calendar.view/view)
(def calendar-day quo2.components.calendar.calendar-day.view/view)
(def calendar-year quo2.components.calendar.calendar-year.view/view)

;;;; Code
(def snippet quo2.components.code.snippet/snippet)

;;;; Cards
(def small-option-card quo2.components.onboarding.small-option-card.view/small-option-card)
(def keycard quo2.components.keycard.view/keycard)

;;;; Colors
(def color-picker quo2.components.colors.color-picker.view/view)
(def picker-colors quo2.components.colors.color-picker.view/picker-colors)

;;;; Community
(def community-card-view-item quo2.components.community.community-card-view/view)
(def communities-membership-list-item
  quo2.components.community.community-list-view/communities-membership-list-item)
(def community-stats-column quo2.components.community.community-view/community-stats-column)
(def community-stats quo2.components.community.community-view/community-stats)
(def community-tags quo2.components.community.community-view/community-tags)
(def community-title quo2.components.community.community-view/community-title)
(def permission-tag-container quo2.components.community.community-view/permission-tag-container)
(def discover-card quo2.components.community.banner.view/view)
(def community-icon quo2.components.community.icon/community-icon)
(def token-requirement-list quo2.components.community.token-gating/token-requirement-list)
(def channel-actions quo2.components.community.channel-actions/channel-actions)

;;;; Counter
(def counter quo2.components.counter.counter.view/view)
(def step quo2.components.counter.step.view/view)

;;;; Dividers
(def divider-label quo2.components.dividers.divider-label.view/view)
(def divider-line quo2.components.dividers.divider-line.view/view)
(def new-messages quo2.components.dividers.new-messages/new-messages)
(def divider-date quo2.components.dividers.date/date)
(def strength-divider quo2.components.dividers.strength-divider.view/view)

;;;; Drawers
(def action-drawer quo2.components.drawers.action-drawers.view/action-drawer)
(def documentation-drawers quo2.components.drawers.documentation-drawers.view/view)
(def drawer-buttons quo2.components.drawers.drawer-buttons.view/view)
(def drawer-top quo2.components.drawers.drawer-top.view/view)
(def permission-context quo2.components.drawers.permission-context.view/view)

;;;; Dropdowns
(def dropdown quo2.components.dropdowns.dropdown/dropdown)
(def network-dropdown quo2.components.dropdowns.network-dropdown.view/view)

;;;; Empty State
(def empty-state quo2.components.empty-state.empty-state.view/empty-state)

;;;; Graph
(def interactive-graph quo2.components.graph.interactive-graph.view/view)
(def wallet-graph quo2.components.graph.wallet-graph.view/view)

;;;; Header
(def header quo2.components.header/header)

;;;; Gradient
(def gradient-cover quo2.components.gradient.gradient-cover.view/view)

;;;; Icon
(def icon quo2.components.icon/icon)

;;;; Info
(def info-message quo2.components.info.info-message/info-message)
(def information-box quo2.components.info.information-box.view/view)

;;;; Inputs
(def input quo2.components.inputs.input.view/input)
(def address-input quo2.components.inputs.address-input.view/address-input)
(def locked-input quo2.components.inputs.locked-input.view/locked-input)
(def profile-input quo2.components.inputs.profile-input.view/profile-input)
(def recovery-phrase-input quo2.components.inputs.recovery-phrase.view/recovery-phrase-input)
(def search-input quo2.components.inputs.search-input.view/search-input)
(def title-input quo2.components.inputs.title-input.view/view)

;;;; Numbered Keyboard
(def keyboard-key quo2.components.numbered-keyboard.keyboard-key.view/view)
(def numbered-keyboard quo2.components.numbered-keyboard.numbered-keyboard.view/view)

;;;; Links
(def link-preview quo2.components.links.link-preview.view/view)
(def url-preview quo2.components.links.url-preview.view/view)
(def url-preview-list quo2.components.links.url-preview-list.view/view)

;;;; List items
(def account-list-card quo2.components.list-items.account-list-card.view/view)
(def channel quo2.components.list-items.channel.view/view)
(def dapp quo2.components.list-items.dapp.view/view)
(def menu-item quo2.components.list-items.menu-item/menu-item)
(def preview-list quo2.components.list-items.preview-list.view/view)
(def user-list quo2.components.list-items.user-list/user-list)
(def community-list-item quo2.components.list-items.community.view/view)
(def token-value quo2.components.list-items.token-value.view/view)

;;;; Loaders
(def skeleton-list quo2.components.loaders.skeleton-list.view/view)

;;;; Navigation
(def floating-shell-button quo2.components.navigation.floating-shell-button.view/view)
(def page-nav quo2.components.navigation.page-nav.view/page-nav)
(def top-nav quo2.components.navigation.top-nav.view/view)

;;;; Markdown
(def markdown-list quo2.components.markdown.list.view/view)
(def text quo2.components.markdown.text/text)

;;;; Messages
(def gap quo2.components.messages.gap/gap)
(def system-message quo2.components.messages.system-message/system-message)

;;;; Notifications
(def activity-log quo2.components.notifications.activity-log.view/view)
(def activity-logs-photos quo2.components.notifications.activity-logs-photos.view/view)
(def notification-dot quo2.components.common.notification-dot.view/view)
(def count-down-circle quo2.components.notifications.count-down-circle/circle-timer)
(def notification quo2.components.notifications.notification.view/notification)
(def toast quo2.components.notifications.toast.view/toast)

;;;; Password
(def tips quo2.components.password.tips.view/view)

;;;; Profile
(def profile-card quo2.components.profile.profile-card.view/profile-card)
(def select-profile quo2.components.profile.select-profile.view/view)
(def showcase-nav quo2.components.profile.showcase-nav.view/view)

;;;; Reactions
(def reaction quo2.components.reactions.reaction/reaction)
(def add-reaction quo2.components.reactions.reaction/add-reaction)

;;;; Record Audio
(def record-audio quo2.components.record-audio.record-audio.view/record-audio)
(def soundtrack quo2.components.record-audio.soundtrack.view/f-soundtrack)

;;;; Selectors
(def author quo2.components.messages.author.view/author)
(def disclaimer quo2.components.selectors.disclaimer.view/view)
(def filter quo2.components.selectors.filter.view/view)
(def reactions quo2.components.selectors.reactions.view/view)
(def checkbox quo2.components.selectors.selectors.view/checkbox)

;;;; Settings
(def privacy-option quo2.components.settings.privacy-option/card)
(def account quo2.components.settings.accounts.view/account)
(def settings-item quo2.components.settings.settings-item.view/view)
(def reorder-item quo2.components.settings.reorder-item.view/reorder-item)
(def category quo2.components.settings.category.view/category)
(def data-item quo2.components.settings.data-item.view/view)

;;;; Share
(def qr-code quo2.components.share.qr-code.view/qr-code)
(def share-qr-code quo2.components.share.share-qr-code.view/view)

;;;; Tabs
(def tabs quo2.components.tabs.tabs.view/view)
(def segmented-control quo2.components.tabs.segmented-tab/segmented-control)
(def account-selector quo2.components.tabs.account-selector/account-selector)

;;;; Tags
(def context-tag quo2.components.tags.context-tag.view/view)
(def network-tags quo2.components.tags.network-tags.view/view)
(def number-tag quo2.components.tags.number-tag.view/view)
(def permission-tag quo2.components.tags.permission-tag/tag)
(def status-tag quo2.components.tags.status-tags/status-tag)
(def tag quo2.components.tags.tag/tag)
(def tags quo2.components.tags.tags/tags)
(def token-tag quo2.components.tags.token-tag/tag)

;;;; Text combinations
(def text-combinations quo2.components.text-combinations.view/view)

;;;; Wallet
(def account-card quo2.components.wallet.account-card.view/view)
(def account-origin quo2.components.wallet.account-origin.view/view)
(def account-overview quo2.components.wallet.account-overview.view/view)
(def keypair quo2.components.wallet.keypair.view/view)
(def network-amount quo2.components.wallet.network-amount.view/view)
(def network-bridge quo2.components.wallet.network-bridge.view/view)
(def progress-bar quo2.components.wallet.progress-bar.view/view)
(def summary-info quo2.components.wallet.summary-info.view/view)
(def network-link quo2.components.wallet.network-link.view/view)
(def token-input quo2.components.wallet.token-input.view/view)
(def wallet-overview quo2.components.wallet.wallet-overview.view/view)
(def wallet-activity quo2.components.wallet.wallet-activity.view/view)
(def transaction-summary quo2.components.wallet.transaction-summary.view/view)
