(ns status-im.ui.components.chat-icon.screen
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame.core]
            [status-im.ui.components.chat-icon.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn default-chat-icon [name styles]
  (when-not (string/blank? name)
    [react/view (:default-chat-icon styles)
     [react/text {:style (:default-chat-icon-text styles)}
      ;; TODO: for now we check if the first letter is a #
      ;; which means it is most likely a public chat and
      ;; use the second letter if that is the case
      ;; a broader refactoring should clean up upstream params
      ;; for default-chat-icon
      (string/capitalize (if (and (= "#" (first name))
                                  (< 1 (count name)))
                           (second name)
                           (first name)))]]))

(defn default-browser-icon [name]
  (default-chat-icon name {:default-chat-icon (styles/default-chat-icon-chat-list colors/default-chat-color)
                           :default-chat-icon-text styles/default-chat-icon-text}))

(defn dapp-badge [{:keys [online-view-wrapper online-view online-dot-left online-dot-right]}]
  [react/view online-view-wrapper
   [react/view online-view
    [react/view
     [react/view online-dot-left]
     [react/view online-dot-right]]]])

(defn pending-contact-badge
  [{:keys [pending-wrapper pending-outer-circle pending-inner-circle]}]
  [react/view pending-wrapper
   [react/view pending-outer-circle
    [react/view pending-inner-circle]]])

(defn chat-icon-view
  [{:keys [photo-path added?] :as contact} _group-chat name _online styles & [hide-dapp?]]
  [react/view (:container styles)
   (if-not (string/blank? photo-path)
     [photos/photo photo-path styles]
     [default-chat-icon name styles])
   (when (and contact (not added?))
     [pending-contact-badge styles])])

(defn chat-icon-view-toolbar
  [contact group-chat name color online]
  [chat-icon-view contact group-chat name online
   {:container              styles/container-chat-toolbar
    :online-view-wrapper    styles/online-view-wrapper
    :online-view            styles/online-view
    :online-dot-left        styles/online-dot-left
    :online-dot-right       styles/online-dot-right
    :pending-wrapper        styles/pending-wrapper
    :pending-outer-circle   styles/pending-outer-circle
    :pending-inner-circle   styles/pending-inner-circle
    :size                   36
    :chat-icon              styles/chat-icon-chat-toolbar
    :default-chat-icon      (styles/default-chat-icon-chat-toolbar color)
    :default-chat-icon-text styles/default-chat-icon-text}])

(defn chat-icon-view-chat-list
  [contact group-chat name color online & [hide-dapp?]]
  [chat-icon-view contact group-chat name online
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
    :default-chat-icon-text styles/default-chat-icon-text}
   hide-dapp?])

(defn contact-icon-view
  [{:keys [photo-path name dapp?]} {:keys [container] :as styles}]
  [react/view container
   (if-not (string/blank? photo-path)
     [photos/photo photo-path styles]
     [default-chat-icon name styles])
   (when dapp?
     [dapp-badge styles])])

(defn contact-icon-contacts-tab [contact]
  [contact-icon-view contact
   {:container              styles/container-chat-list
    :online-view-wrapper    styles/online-view-wrapper
    :online-view            styles/online-view
    :online-dot-left        styles/online-dot-left
    :online-dot-right       styles/online-dot-right
    :size                   40
    :chat-icon              styles/chat-icon-chat-list
    :default-chat-icon      (styles/default-chat-icon-chat-list colors/default-chat-color)
    :default-chat-icon-text styles/default-chat-icon-text}])

(defn dapp-icon-browser [contact size]
  [contact-icon-view contact
   {:container              {:width size :height size :top 3 :margin-left 2}
    :online-view-wrapper    styles/online-view-wrapper
    :size                   size
    :chat-icon              (styles/custom-size-icon size)
    :default-chat-icon      (styles/default-chat-icon-chat-list colors/default-chat-color)
    :default-chat-icon-text styles/default-chat-icon-text}])

(defn dapp-icon-permission [contact size]
  [contact-icon-view contact
   {:container              {:width size :height size}
    :online-view-wrapper    styles/online-view-wrapper
    :online-view            styles/online-view
    :online-dot-left        styles/online-dot-left
    :online-dot-right       styles/online-dot-right
    :size                   size
    :chat-icon              (styles/custom-size-icon size)
    :default-chat-icon      (styles/default-chat-icon-profile colors/default-chat-color size)
    :default-chat-icon-text styles/default-chat-icon-text}])

(defn chat-intro-icon-view [icon-text chat-id styles]
  (let [photo-path (re-frame.core/subscribe [:contacts/chat-photo chat-id])]
    (if-not (string/blank? @photo-path)
      [photos/photo @photo-path styles]
      [default-chat-icon icon-text styles])))

(defn profile-icon-view [photo-path name color edit? size override-styles]
  (let [styles (merge {:container              {:width size :height size}
                       :online-view            styles/online-view-profile
                       :online-dot-left        styles/online-dot-left-profile
                       :online-dot-right       styles/online-dot-right-profile
                       :size                   size
                       :chat-icon              styles/chat-icon-profile
                       :default-chat-icon      (styles/default-chat-icon-profile color size)
                       :default-chat-icon-text styles/default-chat-icon-text} override-styles)]
    [react/view (:container styles)
     (when (and edit? (not platform/desktop?))
       [react/view (styles/profile-icon-mask size)])
     (when (and edit? (not platform/desktop?))
       [react/view (styles/profile-icon-edit-text-containter size)
        [react/i18n-text {:style styles/profile-icon-edit-text :key :edit}]])
     (if (and photo-path (seq photo-path))
       [photos/photo photo-path styles]
       [default-chat-icon name styles])]))

(defn my-profile-icon [{{:keys [photo-path name]} :account
                        edit?                     :edit?}]
  (let [color colors/default-chat-color
        size  56]
    [profile-icon-view photo-path name color edit? size {}]))
