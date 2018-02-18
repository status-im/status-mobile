(ns status-im.ui.screens.profile.contact.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.screens.profile.contact.styles :as styles]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.profile.components.styles :as profile.components.styles]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.action-button.styles :as action-button.styles]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.icons.vector-icons :as vector-icons]))

(defn profile-contact-toolbar [contact]
  [toolbar/toolbar {}
   toolbar/default-nav-back
   [toolbar/content-title ""]
   [toolbar/actions
    (when (and (not (:pending? contact))
               (not (:unremovable? contact)))
      [(actions/opts [{:action #(re-frame/dispatch [:hide-contact contact])
                       :label  (i18n/label :t/remove-from-contacts)}])])]])

(defn network-info []
  [react/view styles/network-info
   [common/network-info]
   [common/separator]])

(defn profile-actions [{:keys [pending? whisper-identity dapp?]} chat-id]
  [react/view action-button.styles/actions-list
   (if pending?
     [action-button/action-button {:label     (i18n/label :t/add-to-contacts)
                                   :icon      :icons/add
                                   :icon-opts {:color :blue}
                                   :on-press  #(re-frame/dispatch [:add-pending-contact chat-id])}]
     [action-button/action-button-disabled {:label (i18n/label :t/in-contacts) :icon :icons/ok}])
   [action-button/action-separator]
   [action-button/action-button {:label     (i18n/label :t/start-conversation)
                                 :icon      :icons/chats
                                 :icon-opts {:color :blue}
                                 :on-press  #(re-frame/dispatch [:start-chat whisper-identity {:navigation-replace? true}])}]
   (when-not dapp?
     [react/view
      [action-button/action-separator]
      [action-button/action-button {:label     (i18n/label :t/send-transaction)
                                    :icon      :icons/arrow-right
                                    :icon-opts {:color :blue}
                                    :on-press  #(re-frame/dispatch [:profile/send-transaction chat-id whisper-identity])}]])])

(defn profile-info-item [{:keys [label value options text-mode empty-value? accessibility-label]}]
  [react/view styles/profile-setting-item
   [react/view (styles/profile-info-text-container options)
    [react/text {:style styles/profile-info-title}
     label]
    [react/view styles/profile-setting-spacing]
    [react/text {:style               (if empty-value?
                                        styles/profile-setting-text-empty
                                        styles/profile-setting-text)
                 :number-of-lines     1
                 :ellipsizeMode       text-mode
                 :accessibility-label accessibility-label}
     value]]
   (when options
     [react/touchable-highlight {:on-press #(list-selection/show {:options options})}
      [react/view profile.components.styles/modal-menu
       [vector-icons/icon :icons/options {:container-style styles/profile-info-item-button}]]])])

(defn profile-options [text]
  (into []
        (when text
          (list-selection/share-options text))))


(defn profile-info-address-item [address]
  [profile-info-item
   {:label               (i18n/label :t/address)
    :action              address
    :options             (profile-options address)
    :text-mode           :middle
    :accessibility-label :profile-address
    :value               address}])

(defn profile-info-public-key-item [whisper-identity]
  [profile-info-item
   {:label               (i18n/label :t/public-key)
    :action              whisper-identity
    :options             (profile-options whisper-identity)
    :text-mode           :middle
    :accessibility-label :profile-public-key
    :value               whisper-identity}])

(defn profile-info [{:keys [whisper-identity address]}]
  [react/view
   [profile-info-address-item address]
   [profile.components/settings-item-separator]
   [profile-info-public-key-item whisper-identity]])

(defview profile []
  (letsubs [contact [:contact]
            chat-id [:get :current-chat-id]]
    [react/view profile.components.styles/profile
     [status-bar/status-bar]
     [profile-contact-toolbar contact]
     [network-info]
     [react/scroll-view
      [react/view profile.components.styles/profile-form
       [profile.components/profile-header contact false false nil nil]]
      [common/form-spacer]
      [profile-actions contact chat-id]
      [common/form-spacer]
      [react/view profile.components.styles/profile-info-container
       [profile-info contact]
       [common/bottom-shadow]]]]))