(ns quo2.core
  (:refer-clojure :exclude [filter])
  (:require
    quo2.components.avatars.account-avatar.view
    quo2.components.avatars.channel-avatar.view
    quo2.components.avatars.group-avatar
    quo2.components.avatars.icon-avatar
    quo2.components.avatars.user-avatar.view
    quo2.components.avatars.wallet-user-avatar
    quo2.components.banners.banner.view
    quo2.components.buttons.button.view
    quo2.components.buttons.dynamic-button
    quo2.components.buttons.predictive-keyboard.view
    quo2.components.buttons.slide-button.view
    quo2.components.browser.browser-input.view
    quo2.components.calendar.calendar.view
    quo2.components.calendar.calendar-day.view
    quo2.components.calendar.calendar-year.view
    quo2.components.code.snippet
    quo2.components.colors.color-picker.view
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
    quo2.components.dividers.divider-label
    quo2.components.dividers.new-messages
    quo2.components.dividers.strength-divider.view
    quo2.components.drawers.action-drawers.view
    quo2.components.drawers.documentation-drawers.view
    quo2.components.drawers.drawer-buttons.view
    quo2.components.drawers.permission-context.view
    quo2.components.dropdowns.dropdown
    quo2.components.empty-state.empty-state.view
    quo2.components.gradient.gradient-cover.view
    quo2.components.graph.wallet-graph.view
    quo2.components.header
    quo2.components.icon
    quo2.components.info.info-message
    quo2.components.info.information-box.view
    quo2.components.inputs.input.view
    quo2.components.inputs.locked-input.view
    quo2.components.inputs.profile-input.view
    quo2.components.inputs.recovery-phrase.view
    quo2.components.inputs.search-input.view
    quo2.components.inputs.title-input.view
    quo2.components.keycard.view
    quo2.components.links.link-preview.view
    quo2.components.links.url-preview-list.view
    quo2.components.links.url-preview.view
    quo2.components.list-items.channel
    quo2.components.list-items.community.view
    quo2.components.list-items.menu-item
    quo2.components.list-items.preview-list
    quo2.components.list-items.token-value.view
    quo2.components.list-items.user-list
    quo2.components.loaders.skeleton
    quo2.components.loaders.skeleton.view
    quo2.components.markdown.list.view
    quo2.components.markdown.text
    quo2.components.messages.author.view
    quo2.components.messages.gap
    quo2.components.messages.system-message
    quo2.components.navigation.floating-shell-button
    quo2.components.navigation.page-nav
    quo2.components.notifications.activity-log.view
    quo2.components.notifications.activity-logs-photos.view
    quo2.components.notifications.count-down-circle
    quo2.components.notifications.notification-dot
    quo2.components.notifications.notification.view
    quo2.components.notifications.toast.view
    quo2.components.numbered-keyboard.keyboard-key.view
    quo2.components.numbered-keyboard.numbered-keyboard.view
    quo2.components.onboarding.small-option-card.view
    quo2.components.password.tips.view
    quo2.components.profile.profile-card.view
    quo2.components.profile.select-profile.view
    quo2.components.reactions.reaction
    quo2.components.record-audio.record-audio.view
    quo2.components.record-audio.soundtrack.view
    quo2.components.selectors.disclaimer.view
    quo2.components.selectors.filter.view
    quo2.components.selectors.reactions.view
    quo2.components.selectors.selectors.view
    quo2.components.settings.accounts.view
    quo2.components.settings.privacy-option
    quo2.components.settings.reorder-item.view
    quo2.components.settings.settings-list.view
    quo2.components.settings.category.view
    quo2.components.share.qr-code.view
    quo2.components.share.share-qr-code.view
    quo2.components.tabs.account-selector
    quo2.components.tabs.segmented-tab
    quo2.components.tabs.tabs
    quo2.components.tags.context-tag.view
    quo2.components.tags.permission-tag
    quo2.components.tags.status-tags
    quo2.components.tags.tag
    quo2.components.tags.tags
    quo2.components.tags.token-tag
    quo2.components.text-combinations.title.view
    quo2.components.wallet.account-overview.view
    quo2.components.wallet.account-card.view
    quo2.components.wallet.network-amount.view
    quo2.components.wallet.network-bridge.view
    quo2.components.wallet.progress-bar.view
    quo2.components.wallet.summary-info.view
    quo2.components.wallet.token-input.view
    quo2.components.wallet.wallet-overview.view))

(def separator quo2.components.common.separator.view/separator)

;;;; AVATAR
(def account-avatar quo2.components.avatars.account-avatar.view/view)
(def channel-avatar quo2.components.avatars.channel-avatar.view/view)
(def group-avatar quo2.components.avatars.group-avatar/group-avatar)
(def icon-avatar quo2.components.avatars.icon-avatar/icon-avatar)
(def user-avatar quo2.components.avatars.user-avatar.view/user-avatar)
(def wallet-user-avatar quo2.components.avatars.wallet-user-avatar/wallet-user-avatar)

;;;; BANNER
(def banner quo2.components.banners.banner.view/banner)

;;;; BUTTONS
(def button quo2.components.buttons.button.view/button)
(def dynamic-button quo2.components.buttons.dynamic-button/dynamic-button)
(def predictive-keyboard quo2.components.buttons.predictive-keyboard.view/view)
(def slide-button quo2.components.buttons.slide-button.view/view)

;;;; BROWSER
(def browser-input quo2.components.browser.browser-input.view/browser-input)

;;;; CALENDAR
(def calendar quo2.components.calendar.calendar.view/view)
(def calendar-day quo2.components.calendar.calendar-day.view/view)
(def calendar-year quo2.components.calendar.calendar-year.view/view)

;;;; CODE
(def snippet quo2.components.code.snippet/snippet)

;;;; CARDS
(def small-option-card quo2.components.onboarding.small-option-card.view/small-option-card)
(def keycard quo2.components.keycard.view/keycard)

;;;; COLORS
(def color-picker quo2.components.colors.color-picker.view/view)
(def picker-colors quo2.components.colors.color-picker.view/picker-colors)

;;;; COMMUNITY
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

;;;; COUNTER
(def counter quo2.components.counter.counter.view/view)
(def step quo2.components.counter.step.view/step)

;;;; DIVIDERS
(def divider-label quo2.components.dividers.divider-label/divider-label)
(def new-messages quo2.components.dividers.new-messages/new-messages)
(def divider-date quo2.components.dividers.date/date)
(def strength-divider quo2.components.dividers.strength-divider.view/view)

;;;; DRAWERS
(def action-drawer quo2.components.drawers.action-drawers.view/action-drawer)
(def documentation-drawers quo2.components.drawers.documentation-drawers.view/view)
(def drawer-buttons quo2.components.drawers.drawer-buttons.view/view)
(def permission-context quo2.components.drawers.permission-context.view/view)

;;;; DROPDOWNS
(def dropdown quo2.components.dropdowns.dropdown/dropdown)

;;;; EMPTY STATE
(def empty-state quo2.components.empty-state.empty-state.view/empty-state)

;;;; GRAPH
(def wallet-graph quo2.components.graph.wallet-graph.view/view)

;;;; HEADER
(def header quo2.components.header/header)

;;;; ICON
(def icon quo2.components.icon/icon)

;;;; INFO
(def info-message quo2.components.info.info-message/info-message)
(def information-box quo2.components.info.information-box.view/view)

;;;; INPUTS
(def input quo2.components.inputs.input.view/input)
(def locked-input quo2.components.inputs.locked-input.view/locked-input)
(def profile-input quo2.components.inputs.profile-input.view/profile-input)
(def recovery-phrase-input quo2.components.inputs.recovery-phrase.view/recovery-phrase-input)
(def search-input quo2.components.inputs.search-input.view/search-input)
(def title-input quo2.components.inputs.title-input.view/title-input)

;;;; NUMBERED KEYBOARD
(def keyboard-key quo2.components.numbered-keyboard.keyboard-key.view/view)
(def numbered-keyboard quo2.components.numbered-keyboard.numbered-keyboard.view/view)

;;;; LIST ITEMS
(def channel-list-item quo2.components.list-items.channel/list-item)
(def menu-item quo2.components.list-items.menu-item/menu-item)
(def preview-list quo2.components.list-items.preview-list/preview-list)
(def user-list quo2.components.list-items.user-list/user-list)
(def community-list-item quo2.components.list-items.community.view/view)
(def token-value quo2.components.list-items.token-value.view/view)

;;;; LOADERS
(def skeleton quo2.components.loaders.skeleton/skeleton)
(def static-skeleton quo2.components.loaders.skeleton.view/view)

;;;; NAVIGATION
(def floating-shell-button quo2.components.navigation.floating-shell-button/floating-shell-button)
(def page-nav quo2.components.navigation.page-nav/page-nav)

;;;; MARKDOWN
(def markdown-list quo2.components.markdown.list.view/view)
(def text quo2.components.markdown.text/text)

;;;; MESSAGES
(def gap quo2.components.messages.gap/gap)
(def system-message quo2.components.messages.system-message/system-message)

;;;; NOTIFICATIONS
(def activity-log quo2.components.notifications.activity-log.view/view)
(def activity-logs-photos quo2.components.notifications.activity-logs-photos.view/view)
(def notification-dot quo2.components.notifications.notification-dot/notification-dot)
(def count-down-circle quo2.components.notifications.count-down-circle/circle-timer)
(def notification quo2.components.notifications.notification.view/notification)
(def toast quo2.components.notifications.toast.view/toast)

;;;; PASSWORD
(def tips quo2.components.password.tips.view/view)

;;;; PROFILE
(def profile-card quo2.components.profile.profile-card.view/profile-card)
(def select-profile quo2.components.profile.select-profile.view/view)

;;;; REACTIONS
(def reaction quo2.components.reactions.reaction/reaction)
(def add-reaction quo2.components.reactions.reaction/add-reaction)

;;;; RECORD AUDIO
(def record-audio quo2.components.record-audio.record-audio.view/record-audio)
(def soundtrack quo2.components.record-audio.soundtrack.view/f-soundtrack)

;;;; SELECTORS
(def author quo2.components.messages.author.view/author)
(def disclaimer quo2.components.selectors.disclaimer.view/view)
(def filter quo2.components.selectors.filter.view/view)
(def reactions quo2.components.selectors.reactions.view/view)
(def checkbox quo2.components.selectors.selectors.view/checkbox)

;;;; SETTINGS
(def privacy-option quo2.components.settings.privacy-option/card)
(def account quo2.components.settings.accounts.view/account)
(def settings-list quo2.components.settings.settings-list.view/settings-list)
(def reorder-item quo2.components.settings.reorder-item.view/reorder-item)
(def category quo2.components.settings.category.view/category)

;;;; SHARE
(def qr-code quo2.components.share.qr-code.view/qr-code)
(def share-qr-code quo2.components.share.share-qr-code.view/view)

;;;; TABS
(def tabs quo2.components.tabs.tabs/tabs)
(def segmented-control quo2.components.tabs.segmented-tab/segmented-control)
(def account-selector quo2.components.tabs.account-selector/account-selector)

;;;; TAGS
(def tag quo2.components.tags.tag/tag)
(def tags quo2.components.tags.tags/tags)
(def permission-tag quo2.components.tags.permission-tag/tag)
(def status-tag quo2.components.tags.status-tags/status-tag)
(def token-tag quo2.components.tags.token-tag/tag)
(def user-avatar-tag quo2.components.tags.context-tag.view/user-avatar-tag)
(def context-tag quo2.components.tags.context-tag.view/context-tag)
(def group-avatar-tag quo2.components.tags.context-tag.view/group-avatar-tag)
(def audio-tag quo2.components.tags.context-tag.view/audio-tag)
(def community-tag quo2.components.tags.context-tag.view/community-tag)

;;;; TITLE
(def title quo2.components.text-combinations.title.view/title)

;;;; LINKS
(def url-preview quo2.components.links.url-preview.view/view)
(def url-preview-list quo2.components.links.url-preview-list.view/view)
(def link-preview quo2.components.links.link-preview.view/view)

;;;; GRADIENT
(def gradient-cover quo2.components.gradient.gradient-cover.view/view)

;;;; WALLET
(def account-card quo2.components.wallet.account-card.view/view)
(def account-overview quo2.components.wallet.account-overview.view/view)
(def network-amount quo2.components.wallet.network-amount.view/view)
(def network-bridge quo2.components.wallet.network-bridge.view/view)
(def progress-bar quo2.components.wallet.progress-bar.view/view)
(def summary-info quo2.components.wallet.summary-info.view/view)
(def token-input quo2.components.wallet.token-input.view/view)
(def wallet-overview quo2.components.wallet.wallet-overview.view/view)

