(ns status-im.discovery.views.discovery-list-item
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as str]
            [status-im.components.react :refer [view text image]]
            [status-im.discovery.styles :as st]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.i18n :refer [label]]))

(defview discovery-list-item [{:keys [name photo-path status whisper-id]}]
  [{contact-name       :name
    contact-photo-path :photo-path} [:get-in [:contacts whisper-id]]]
  [view st/popular-list-item
   [view st/popular-list-item-name-container
    [text {:style           st/popular-list-item-name
           :font            :medium
           :number-of-lines 1}
     (cond
       (not (str/blank? contact-name)) contact-name
       (not (str/blank? name)) name
       :else (label :t/user-anonymous))]
    [text {:style           st/popular-list-item-status
           :font            :default
           :number-of-lines 2}
     status]]
   [view st/popular-list-item-avatar-container
    [image {:style  st/popular-list-item-avatar
            :source {:uri (cond
                            (not (str/blank? contact-photo-path)) contact-photo-path
                            (not (str/blank? photo-path)) photo-path
                            :else (identicon whisper-id))}}]]])
