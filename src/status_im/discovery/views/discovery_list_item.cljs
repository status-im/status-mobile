(ns status-im.discovery.views.discovery-list-item
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as str]
            [status-im.components.react :refer [view text touchable-highlight]]
            [status-im.discovery.styles :as st]
            [status-im.components.status-view.view :refer [status-view]]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.components.chat-icon.screen :as ci]
            [status-im.utils.platform :refer [platform-specific]]))

(defview discovery-list-item [{{:keys [name
                                       photo-path
                                       whisper-id
                                       message-id
                                       status]
                                :as   message}                   :message
                               show-separator?                   :show-separator?
                               {account-photo-path :photo-path
                                account-address    :public-key
                                account-name       :name
                                :as                current-account} :current-account}]
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
          (= account-address whisper-id) account-name
          (not (str/blank? contact-name)) contact-name
          (not (str/blank? name)) name
          :else (generate-gfy))]
       [status-view {:id     message-id
                     :style  (:status-text item-style)
                     :status status}]]
      [view (merge st/popular-list-item-avatar-container
                   (:icon item-style))
       [touchable-highlight {:on-press #(dispatch [:start-chat whisper-id])}
        [view
         [ci/chat-icon (cond
                         (= account-address whisper-id) account-photo-path
                         (not (str/blank? contact-photo-path)) contact-photo-path
                         (not (str/blank? photo-path)) photo-path
                         :else (identicon whisper-id))
          {:size 36}]]]]]
     (when show-separator?
       [view st/separator])]))
