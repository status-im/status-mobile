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
            [status-im.constants :refer [console-chat-id]]
            [clojure.string :as s]))

(defn default-chat-icon [name styles]
  [view (:default-chat-icon styles)
   [text {:style (:default-chat-icon-text styles)}
    (first name)]])

(defn chat-icon [photo-path styles]
  [image {:source {:uri photo-path}
          :style  (:chat-icon styles)}])

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

(defview chat-icon-view [chat-id group-chat name online styles]
  [photo-path [:chat-photo chat-id]]
  [view (:container styles)
   (if-not (or (s/blank? photo-path) (= chat-id console-chat-id))
     [chat-icon photo-path styles]
     [default-chat-icon name styles])
   (when-not group-chat
     [contact-badge (if online :online :blank) styles])])

(defn chat-icon-view-chat-list [chat-id group-chat name color online]
  [chat-icon-view chat-id group-chat name online
   {:container              st/container-chat-list
    :online-view            st/online-view
    :online-dot-left        st/online-dot-left
    :online-dot-right       st/online-dot-right
    :chat-icon              st/chat-icon-chat-list
    :default-chat-icon      (st/default-chat-icon-chat-list color)
    :default-chat-icon-text st/default-chat-icon-text}])

(defn chat-icon-view-action [chat-id group-chat name color online]
  [chat-icon-view chat-id group-chat name online
   {:container              st/container
    :online-view            st/online-view
    :online-dot-left        st/online-dot-left
    :online-dot-right       st/online-dot-right
    :chat-icon              st/chat-icon
    :default-chat-icon      (st/default-chat-icon color)
    :default-chat-icon-text st/default-chat-icon-text}])

(defn chat-icon-view-menu-item [chat-id group-chat name color online]
  [chat-icon-view chat-id group-chat name online
   {:container              st/container-menu-item
    :online-view            st/online-view-menu-item
    :online-dot-left        st/online-dot-left-menu-item
    :online-dot-right       st/online-dot-right-menu-item
    :chat-icon              st/chat-icon-menu-item
    :default-chat-icon      (st/default-chat-icon-menu-item color)
    :default-chat-icon-text st/default-chat-icon-text}])

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
    :chat-icon              st/chat-icon-chat-list
    :default-chat-icon      (st/default-chat-icon-chat-list default-chat-color)
    :default-chat-icon-text st/default-chat-icon-text}])

(defn profile-icon-view [photo-path name color badge-type]
  (let [styles {:container              st/container-profile
                :online-view            st/online-view-profile
                :online-dot-left        st/online-dot-left-profile
                :online-dot-right       st/online-dot-right-profile
                :chat-icon              st/chat-icon-profile
                :default-chat-icon      (st/default-chat-icon-profile color)
                :default-chat-icon-text st/default-chat-icon-text}]
    [view (:container styles)
     (if (and photo-path (not (empty? photo-path)))
       [chat-icon photo-path styles]
       [default-chat-icon name styles])
     [contact-badge badge-type styles]]))

(defview profile-icon []
  [contact [:contact]]
  (let [;; TODO: stub
        type    :online
        color   default-chat-color]
    [profile-icon-view (:photo-path @contact) (:name @contact) color type]))

(defn my-profile-icon [{{:keys [photo-path name]} :account
                        edit?                     :edit?}]
  (let [type  (if edit? :edit :blank)
        color default-chat-color]
    [profile-icon-view photo-path name color type]))
