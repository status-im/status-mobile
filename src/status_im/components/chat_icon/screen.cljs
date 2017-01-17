(ns status-im.components.chat-icon.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                icon]]
            [status-im.components.icons.custom-icons :refer [oct-icon]]
            [status-im.components.chat-icon.styles :as st]
            [status-im.components.styles :refer [default-chat-color]]
            [status-im.resources :as resources]
            [status-im.constants :refer [console-chat-id]]
            [clojure.string :as s]))

(defn default-chat-icon [name styles]
  [view (:default-chat-icon styles)
   [text {:style (:default-chat-icon-text styles)}
    (first name)]])

(defn chat-icon [photo-path {:keys [size]}]
  (let [photo (if (s/starts-with? photo-path "contacts://")
                (->> (s/replace photo-path #"contacts://" "")
                     (keyword)
                     (get resources/contacts))
                {:uri photo-path})]
    [image {:source photo
            :style  (merge st/default-image-style
                           (st/image-style size))}]))

(defn dapp-badge [styles]
  [view (:online-view-wrapper styles)
   [view (:online-view styles)
    [view
     [view (:online-dot-left styles)]
     [view (:online-dot-right styles)]]]])

(defn contact-badge [type styles]
  (when (= type :edit)
    [view (:online-view styles)
     (case type
       :online [view
                [view (:online-dot-left styles)]
                [view (:online-dot-right styles)]]
       :edit [view
              [oct-icon {:name  :pencil
                         :style st/photo-pencil}]])]))

(defview pending-contact-badge
  [chat-id {:keys [pending-wrapper pending-outer-circle pending-inner-circle]}]
  [pending-contact? [:get-in [:contacts chat-id :pending?]]]
  (when pending-contact?
    [view pending-wrapper
     [view pending-outer-circle
      [view pending-inner-circle]]]))

(defview chat-icon-view [chat-id group-chat name online styles]
  [photo-path [:chat-photo chat-id]
   dapp? [:get-in [:contacts chat-id :dapp?]]]
  [view (:container styles)
   (if-not (s/blank? photo-path)
     [chat-icon photo-path styles]
     [default-chat-icon name styles])
   (when dapp?
     [dapp-badge styles])
   [pending-contact-badge chat-id styles]])

(defn chat-icon-view-chat-list [chat-id group-chat name color online]
  [chat-icon-view chat-id group-chat name online
   {:container              st/container-chat-list
    :online-view-wrapper    st/online-view-wrapper
    :online-view            st/online-view
    :online-dot-left        st/online-dot-left
    :online-dot-right       st/online-dot-right
    :pending-wrapper        st/pending-wrapper
    :pending-outer-circle   st/pending-outer-circle
    :pending-inner-circle   st/pending-inner-circle
    :size                   40
    :chat-icon              st/chat-icon-chat-list
    :default-chat-icon      (st/default-chat-icon-chat-list color)
    :default-chat-icon-text st/default-chat-icon-text}])

(defn chat-icon-view-action [chat-id group-chat name color online]
  ^{:key chat-id}
  [chat-icon-view chat-id group-chat name online
   {:container              st/container
    :online-view-wrapper    st/online-view-wrapper
    :online-view            st/online-view
    :online-dot-left        st/online-dot-left
    :online-dot-right       st/online-dot-right
    :pending-wrapper        st/pending-wrapper
    :pending-outer-circle   st/pending-outer-circle
    :pending-inner-circle   st/pending-inner-circle
    :size                   36
    :chat-icon              st/chat-icon-view-action
    :default-chat-icon      (st/default-chat-icon-view-action color)
    :default-chat-icon-text st/default-chat-icon-text}])

(defn chat-icon-view-menu-item [chat-id group-chat name color online]
  ^{:key chat-id}
  [chat-icon-view chat-id group-chat name online
   {:container              st/container-menu-item
    :online-view-wrapper    st/online-view-menu-wrapper
    :online-view            st/online-view-menu-item
    :online-dot-left        st/online-dot-left-menu-item
    :online-dot-right       st/online-dot-right-menu-item
    :pending-wrapper        st/pending-view-menu-wrapper
    :pending-outer-circle   st/pending-outer-circle
    :pending-inner-circle   st/pending-inner-circle
    :size                   24
    :chat-icon              st/chat-icon-menu-item
    :default-chat-icon      (st/default-chat-icon-view-action color)
    :default-chat-icon-text st/default-chat-icon-text}])

(defn chat-icon-message-status [chat-id group-chat name color online]
  ^{:key chat-id}
  [chat-icon-view chat-id group-chat name online
   {:container              st/container-message-status
    :online-view-wrapper    st/online-view-wrapper
    :online-view            st/online-view
    :online-dot-left        st/online-dot-left
    :online-dot-right       st/online-dot-right
    :pending-wrapper        st/pending-wrapper
    :pending-outer-circle   st/pending-outer-circle
    :pending-inner-circle   st/pending-inner-circle
    :size                   64
    :chat-icon              st/chat-icon-message-status
    :default-chat-icon      (st/default-chat-icon-message-status color)
    :default-chat-icon-text st/message-status-icon-text}])

(defn contact-icon-view [contact styles]
  (let [photo-path (:photo-path contact)
        ;; TODO: stub
        type       :online]
    [view (:container styles)
     (if-not (s/blank? photo-path)
       [chat-icon photo-path styles]
       [default-chat-icon (:name contact) styles])
     [contact-badge type styles]]))

(defn contact-icon-contacts-tab [contact]
  [contact-icon-view contact
   {:container              st/container-chat-list
    :online-view            st/online-view
    :online-dot-left        st/online-dot-left
    :online-dot-right       st/online-dot-right
    :size                   40
    :chat-icon              st/chat-icon-chat-list
    :default-chat-icon      (st/default-chat-icon-chat-list default-chat-color)
    :default-chat-icon-text st/default-chat-icon-text}])

(defn profile-icon-view [photo-path name color badge-type]
  (let [styles {:container              st/container-profile
                :online-view            st/online-view-profile
                :online-dot-left        st/online-dot-left-profile
                :online-dot-right       st/online-dot-right-profile
                :size                   64
                :chat-icon              st/chat-icon-profile
                :default-chat-icon      (st/default-chat-icon-profile color)
                :default-chat-icon-text st/default-chat-icon-text}]
    [view (:container styles)
     (if (and photo-path (seq photo-path))
       [chat-icon photo-path styles]
       [default-chat-icon name styles])
     [contact-badge badge-type styles]]))

(defn my-profile-icon [{{:keys [photo-path name]} :account
                        edit?                     :edit?}]
  (let [type  (if edit? :edit :blank)
        color default-chat-color]
    [profile-icon-view photo-path name color type]))
