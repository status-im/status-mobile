(ns syng-im.chats-list.views.inner-item
  (:require [clojure.string :as s]
            [syng-im.components.react :refer [view image icon text]]
            [syng-im.chats-list.styles :as st]
            [syng-im.resources :as res]))


(defn contact-photo [photo-path]
  [view st/contact-photo-container
   [image {:source (if (s/blank? photo-path)
                     res/user-no-photo
                     {:uri photo-path})
           :style  st/contact-photo-image}]])

(defn contact-online [online]
  (when online
    [view st/online-container
     [view st/online-dot-left]
     [view st/online-dot-right]]))

(defn chat-list-item-inner-view
  [{:keys [name photo-path delivery-status timestamp new-messages-count online
           group-chat contacts]}]
  [view st/chat-container
   [view st/photo-container
    [contact-photo photo-path]
    [contact-online online]]
   [view st/item-container
    [view st/name-view
     [text {:style st/name-text} name]
     (when group-chat
       [icon :group st/group-icon])
     (when group-chat
       [text {:style st/memebers-text}
        (if (< 1 (count contacts))
          (str (count contacts) " members")
          "1 member")])]
    [text {:style         st/last-message-text
           :numberOfLines 2}
     (repeatedly 5 #(str "Hi, I'm " name "! "))]]
   [view
    [view st/status-container
     (when delivery-status
       [image {:source (if (= (keyword delivery-status) :seen)
                         {:uri :icon_ok_small}
                         ;; todo change icon
                         {:uri :icon_ok_small})
               :style  st/status-image}])
     [text {:style st/datetime-text} timestamp]]
    (when (pos? new-messages-count)
      [view st/new-messages-container
       [text {:style st/new-messages-text} new-messages-count]])]])
