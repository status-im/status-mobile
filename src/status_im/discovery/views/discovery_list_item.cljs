(ns status-im.discovery.views.discovery-list-item
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as str]
            [status-im.components.react :refer [view text image touchable-highlight]]
            [status-im.discovery.styles :as st]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.i18n :refer [label]]
            [status-im.components.chat-icon.screen :as ci]
            [status-im.utils.platform :refer [platform-specific]]
            [taoensso.timbre :as log]))

(defn tag-view [tag]
  [text {:style {:color "#7099e6"}
         :font :medium}
   (str tag " ")])

(defn status-view [item-style {:keys [message-id status]}]
  [text {:style (:status-text item-style)
         :font  :default}
   (for [[i status] (map-indexed vector (str/split status #" "))]
     (if (.startsWith status "#")
       ^{:key (str "item-" message-id "-" i)}
       [tag-view status]
       ^{:key (str "item-" message-id "-" i)}
       (str status " ")))])

(defview discovery-list-item [{:keys [name photo-path whisper-id] :as message}
                              show-separator?]
  [{contact-name       :name
    contact-photo-path :photo-path} [:get-in [:contacts whisper-id]]]
  (let [item-style (get-in platform-specific [:component-styles :discovery :item])]
    [view
     [view st/popular-list-item
      [view st/popular-list-item-name-container
       [text {:style           st/popular-list-item-name
              :font            :medium
              :number-of-lines 1}
        (cond
          (not (str/blank? contact-name)) contact-name
          (not (str/blank? name)) name
          :else (generate-gfy))]
       [status-view item-style message]]
      [view (merge st/popular-list-item-avatar-container
                   (:icon item-style))
       [touchable-highlight {:on-press #(dispatch [:start-chat whisper-id])}
        [view
         [ci/chat-icon (cond
                         (not (str/blank? contact-photo-path)) contact-photo-path
                         (not (str/blank? photo-path)) photo-path
                         :else (identicon whisper-id))
          {:size 36}]]]]]
     (when show-separator?
       [view st/separator])]))
