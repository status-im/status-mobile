(ns syng-im.components.chat-icon.screen
  (:require-macros [syng-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [syng-im.components.react :refer [view
                                              text
                                              image
                                              icon]]
            [syng-im.components.chat-icon.styles :as st]))

(defn default-chat-icon [name color styles]
  [view (:default-chat-icon styles)
   [text {:style (:default-chat-icon-text styles)}
    (nth name 0)]])

(defn chat-icon [photo-path styles]
  [image {:source {:uri photo-path}
          :style  (:chat-icon styles)}])

(defn contact-online [online styles]
  (when online
    [view (:online-view styles)
     [view (:online-dot-left styles)]
     [view (:online-dot-right styles)]]))

(defview chat-icon-view [chat-id group-chat name color online styles]
  [photo-path [:chat-photo chat-id]]
  [view (:container styles)
   (if photo-path
     [chat-icon photo-path styles]
     [default-chat-icon name color styles])
   (when (not group-chat)
     [contact-online online styles])])

(defn chat-icon-view-chat-list [chat-id group-chat name color online]
  [chat-icon-view chat-id group-chat name color online
   {:container              st/container-chat-list
    :online-view            st/online-view
    :online-dot-left        st/online-dot-left
    :online-dot-right       st/online-dot-right
    :chat-icon              st/chat-icon-chat-list
    :default-chat-icon      (st/default-chat-icon-chat-list color)
    :default-chat-icon-text st/default-chat-icon-text}])

(defn chat-icon-view-action [chat-id group-chat name color online]
  [chat-icon-view chat-id group-chat name color online
   {:container              st/container
    :online-view            st/online-view
    :online-dot-left        st/online-dot-left
    :online-dot-right       st/online-dot-right
    :chat-icon              st/chat-icon
    :default-chat-icon      (st/default-chat-icon color)
    :default-chat-icon-text st/default-chat-icon-text}])

(defn chat-icon-view-menu-item [chat-id group-chat name color online]
  [chat-icon-view chat-id group-chat name color online
   {:container              st/container-menu-item
    :online-view            st/online-view-menu-item
    :online-dot-left        st/online-dot-left-menu-item
    :online-dot-right       st/online-dot-right-menu-item
    :chat-icon              st/chat-icon-menu-item
    :default-chat-icon      (st/default-chat-icon-menu-item color)
    :default-chat-icon-text st/default-chat-icon-text}])
