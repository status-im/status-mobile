(ns status-im.ui.components.chat-icon.screen
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.chat-icon.styles :as styles]
            [status-im.ui.components.styles :as components.styles]
            [status-im.react-native.resources :as resources]))

(defn default-chat-icon [name styles]
  [react/view (:default-chat-icon styles)
   [react/text {:style (:default-chat-icon-text styles)}
    (first name)]])

(defn chat-icon [photo-path {:keys [size accessibility-label]}]
  (let [photo (when photo-path
                (if (string/starts-with? photo-path "contacts://")
                  (->> (string/replace photo-path #"contacts://" "")
                       (keyword)
                       (get resources/contacts))
                  {:uri photo-path}))]
    [react/image {:source              photo
                  :style               (styles/image-style size)
                  :accessibility-label (or accessibility-label :chat-icon)}]))

(defn dapp-badge [{:keys [online-view-wrapper online-view online-dot-left online-dot-right]}]
  [react/view online-view-wrapper
   [react/view online-view
    [react/view
     [react/view online-dot-left]
     [react/view online-dot-right]]]])

(defview pending-contact-badge
  [chat-id {:keys [pending-wrapper pending-outer-circle pending-inner-circle]}]
  (letsubs [pending-contact? [:get-in [:contacts/contacts chat-id :pending?]]]
    (when pending-contact?
      [react/view pending-wrapper
       [react/view pending-outer-circle
        [react/view pending-inner-circle]]])))

(defview chat-icon-view [chat-id _group-chat name _online styles & [hide-dapp?]]
  (letsubs [photo-path [:get-chat-photo chat-id]
            dapp?      [:get-in [:contacts/contacts chat-id :dapp?]]]
    [react/view (:container styles)
     (if-not (string/blank? photo-path)
       [chat-icon photo-path styles]
       [default-chat-icon name styles])
     (when (and dapp? (not hide-dapp?))
       [dapp-badge styles])
     [pending-contact-badge chat-id styles]]))

(defn chat-icon-view-chat-list [chat-id group-chat name color online]
  [chat-icon-view chat-id group-chat name online
   {:container              styles/container-chat-list
    :online-view-wrapper    styles/online-view-wrapper
    :online-view            styles/online-view
    :online-dot-left        styles/online-dot-left
    :online-dot-right       styles/online-dot-right
    :pending-wrapper        styles/pending-wrapper
    :pending-outer-circle   styles/pending-outer-circle
    :pending-inner-circle   styles/pending-inner-circle
    :size                   40
    :chat-icon              styles/chat-icon-chat-list
    :default-chat-icon      (styles/default-chat-icon-chat-list color)
    :default-chat-icon-text styles/default-chat-icon-text}])

(defn chat-icon-view-action [chat-id group-chat name online]
  ^{:key chat-id}
  [chat-icon-view chat-id group-chat name online
   {:container              styles/container-chat-list
    :online-view-wrapper    styles/online-view-wrapper
    :online-view            styles/online-view
    :online-dot-left        styles/online-dot-left
    :online-dot-right       styles/online-dot-right
    :size                   40
    :chat-icon              styles/chat-icon-chat-list
    :default-chat-icon      (styles/default-chat-icon-chat-list components.styles/default-chat-color)
    :default-chat-icon-text styles/default-chat-icon-text}])

(defn chat-icon-view-menu-item [chat-id group-chat name color online]
  ^{:key chat-id}
  [chat-icon-view chat-id group-chat name online
   {:container              styles/container-menu-item
    :online-view-wrapper    styles/online-view-menu-wrapper
    :online-view            styles/online-view-menu-item
    :online-dot-left        styles/online-dot-left-menu-item
    :online-dot-right       styles/online-dot-right-menu-item
    :pending-wrapper        styles/pending-view-menu-wrapper
    :pending-outer-circle   styles/pending-outer-circle
    :pending-inner-circle   styles/pending-inner-circle
    :size                   24
    :chat-icon              styles/chat-icon-menu-item
    :default-chat-icon      (styles/default-chat-icon-view-action color)
    :default-chat-icon-text styles/default-chat-icon-text}
   true])

(defn chat-icon-message-status [chat-id group-chat name color online]
  ^{:key chat-id}
  [chat-icon-view chat-id group-chat name online
   {:container              styles/container-message-status
    :online-view-wrapper    styles/online-view-wrapper
    :online-view            styles/online-view
    :online-dot-left        styles/online-dot-left
    :online-dot-right       styles/online-dot-right
    :pending-wrapper        styles/pending-wrapper
    :pending-outer-circle   styles/pending-outer-circle
    :pending-inner-circle   styles/pending-inner-circle
    :size                   64
    :chat-icon              styles/chat-icon-message-status
    :default-chat-icon      (styles/default-chat-icon-message-status color)
    :default-chat-icon-text styles/message-status-icon-text}])

(defn contact-icon-view [{:keys [photo-path name dapp?]} {:keys [container] :as styles}]
  (let [photo-path photo-path]
    [react/view container
     (if-not (string/blank? photo-path)
       [chat-icon photo-path styles]
       [default-chat-icon name styles])
     (when dapp?
       [dapp-badge styles])]))

(defn contact-icon-contacts-tab [contact]
  [contact-icon-view contact
   {:container              styles/container-chat-list
    :online-view-wrapper    styles/online-view-wrapper
    :online-view            styles/online-view
    :online-dot-left        styles/online-dot-left
    :online-dot-right       styles/online-dot-right
    :size                   40
    :chat-icon              styles/chat-icon-chat-list
    :default-chat-icon      (styles/default-chat-icon-chat-list components.styles/default-chat-color)
    :default-chat-icon-text styles/default-chat-icon-text}])

(defn dapp-icon-browser [contact]
  [contact-icon-view contact
   {:container              styles/container-chat-list
    :online-view-wrapper    styles/online-view-wrapper
    :online-view            styles/online-view
    :online-dot-left        styles/online-dot-left
    :online-dot-right       styles/online-dot-right
    :size                   36
    :chat-icon              styles/chat-icon-chat-list
    :default-chat-icon      (styles/default-chat-icon-chat-list components.styles/default-chat-color)
    :default-chat-icon-text styles/default-chat-icon-text}])

(defn profile-icon-view [photo-path name color edit? size]
  (let [styles {:container              {:width size :height size}
                :online-view            styles/online-view-profile
                :online-dot-left        styles/online-dot-left-profile
                :online-dot-right       styles/online-dot-right-profile
                :size                   size
                :chat-icon              styles/chat-icon-profile
                :default-chat-icon      (styles/default-chat-icon-profile color)
                :default-chat-icon-text styles/default-chat-icon-text}]
    [react/view (:container styles)
     (when edit?
       [react/view (styles/profile-icon-mask size)])
     (when edit?
       [react/view (styles/profile-icon-edit-text-containter size)
         [react/text {:style styles/profile-icon-edit-text}
          (i18n/label :t/edit)]])
     (if (and photo-path (seq photo-path))
       [chat-icon photo-path styles]
       [default-chat-icon name styles])]))

(defn my-profile-icon [{{:keys [photo-path name]} :account
                        edit?                     :edit?}]
  (let [color components.styles/default-chat-color
        size  56]
    [profile-icon-view photo-path name color edit? size]))