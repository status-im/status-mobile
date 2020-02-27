(ns status-im.ui.components.chat-icon.screen
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame.core]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.chat-icon.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

;;TODO REWORK THIS NAMESPACE

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
                           :default-chat-icon-text (styles/default-chat-icon-text 40)}))

(defn dapp-badge [{:keys [online-view-wrapper online-view online-dot-left online-dot-right]}]
  [react/view online-view-wrapper
   [react/view online-view
    [react/view
     [react/view online-dot-left]
     [react/view online-dot-right]]]])

(defn chat-icon-view
  [contact group-chat name _online styles]
  [react/view (:container styles)
   (if-not group-chat
     [photos/photo (multiaccounts/displayed-photo contact) styles]
     [default-chat-icon name styles])])

(defn chat-icon-view-toolbar
  [contact group-chat name color online]
  [chat-icon-view contact group-chat name online
   {:container              styles/container-chat-toolbar
    :online-view-wrapper    styles/online-view-wrapper
    :online-view            styles/online-view
    :online-dot-left        styles/online-dot-left
    :online-dot-right       styles/online-dot-right
    :size                   36
    :chat-icon              styles/chat-icon-chat-toolbar
    :default-chat-icon      (styles/default-chat-icon-chat-toolbar color)
    :default-chat-icon-text (styles/default-chat-icon-text 36)}])

(defn chat-icon-view-chat-list
  [contact group-chat name color online]
  [chat-icon-view contact group-chat name online
   {:container              styles/container-chat-list
    :online-view-wrapper    styles/online-view-wrapper
    :online-view            styles/online-view
    :online-dot-left        styles/online-dot-left
    :online-dot-right       styles/online-dot-right
    :size                   40
    :chat-icon              styles/chat-icon-chat-list
    :default-chat-icon      (styles/default-chat-icon-chat-list color)
    :default-chat-icon-text (styles/default-chat-icon-text 40)}])

(defn chat-icon-view-chat-sheet
  [contact group-chat name color online]
  [chat-icon-view contact group-chat name online
   {:container              styles/container-chat-list
    :online-view-wrapper    styles/online-view-wrapper
    :online-view            styles/online-view
    :online-dot-left        styles/online-dot-left
    :online-dot-right       styles/online-dot-right
    :size                   40
    :chat-icon              styles/chat-icon-chat-list
    :default-chat-icon      (styles/default-chat-icon-chat-list color)
    :default-chat-icon-text (styles/default-chat-icon-text 40)}])

(defn custom-icon-view-list
  [name color & [size]]
  [react/view (styles/container-list-size (or size 40))
   [default-chat-icon name {:default-chat-icon      (styles/default-chat-icon-profile color (or size 40))
                            :default-chat-icon-text (styles/default-chat-icon-text (or size 40))}]])

(defn contact-icon-view
  [{:keys [name dapp?] :as contact} {:keys [container] :as styles}]
  [react/view container
   (if dapp?
     [default-chat-icon name styles]
     [photos/photo (multiaccounts/displayed-photo contact) styles])
   (when dapp?
     [dapp-badge styles])])

(defn contact-icon-view-chat [contact]
  [contact-icon-view contact
   {:container              styles/container-chat-list
    :online-view-wrapper    styles/online-view-wrapper
    :online-view            styles/online-view
    :online-dot-left        styles/online-dot-left
    :online-dot-right       styles/online-dot-right
    :size                   60
    :chat-icon              styles/chat-icon-chat-list
    :default-chat-icon      (styles/default-chat-icon-chat-list colors/default-chat-color)
    :default-chat-icon-text (styles/default-chat-icon-text 60)}])

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
    :default-chat-icon-text (styles/default-chat-icon-text 40)}])

(defn dapp-icon-browser [contact size]
  [contact-icon-view contact
   {:container              {:width size :height size :top 3 :margin-left 2}
    :online-view-wrapper    styles/online-view-wrapper
    :size                   size
    :chat-icon              (styles/custom-size-icon size)
    :default-chat-icon      (styles/default-chat-icon-chat-list colors/default-chat-color)
    :default-chat-icon-text (styles/default-chat-icon-text size)}])

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
    :default-chat-icon-text (styles/default-chat-icon-text size)}])

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
                       :default-chat-icon-text (styles/default-chat-icon-text size)} override-styles)]
    [react/view (:container styles)
     (when (and edit? (not platform/desktop?))
       [react/view (styles/profile-icon-mask size)])
     (when (and edit? (not platform/desktop?))
       [react/view (styles/profile-icon-edit-text-containter size)
        [react/i18n-text {:style styles/profile-icon-edit-text :key :edit}]])
     (if (and photo-path (seq photo-path))
       [photos/photo photo-path styles]
       [default-chat-icon name styles])]))

(defn my-profile-icon [{multiaccount :multiaccount
                        edit?        :edit?}]
  (let [color colors/default-chat-color
        size  64]
    [profile-icon-view
     (multiaccounts/displayed-photo multiaccount)
     (multiaccounts/displayed-name multiaccount)
     color
     edit?
     size {}]))

(defn my-profile-header-icon [{multiaccount :multiaccount
                               edit?        :edit?}]
  (let [color colors/default-chat-color
        size  40]
    [profile-icon-view
     (multiaccounts/displayed-photo multiaccount)
     (multiaccounts/displayed-name multiaccount)
     color
     edit?
     size {}]))
