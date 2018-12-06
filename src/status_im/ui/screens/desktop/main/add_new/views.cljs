(ns status-im.ui.screens.desktop.main.add-new.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.utils.config :as config]
            [status-im.ui.screens.add-new.new-public-chat.view :as public-chat]
            [status-im.ui.components.list.views :as list]
            [clojure.string :as string]
            [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.desktop.main.add-new.styles :as styles]
            [status-im.ui.screens.add-new.new-public-chat.view :refer [default-public-chats]]
            [status-im.ui.screens.add-new.new-public-chat.db :as public-chat.db]
            [taoensso.timbre :as log]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]))

(defn topic-input-placeholder []
  [react/text {:style styles/topic-placeholder} "#"])

(views/defview error-tooltip [text]
  [react/view {:style styles/tooltip-container}
   [react/view {:style styles/tooltip-icon-text}
    [react/text {:style {:font-size 14 :color colors/red}}
     text]]
   [react/view {:style styles/tooltip-triangle}]])

(views/defview new-one-to-one []
  (views/letsubs [new-contact-identity [:get :contacts/new-identity]
                  contacts             [:contacts/all-added-people-contacts]
                  chat-error           [:new-identity-error]]
    {:component-will-unmount #(re-frame/dispatch [:new-chat/set-new-identity nil])}
    [react/view {:style styles/new-view}
     [react/view {:style styles/new-contact-title}
      [react/text {:style styles/new-contact-title-text
                   :font  :medium}
       (i18n/label :new-chat)]]
     [react/text {:style styles/new-contact-subtitle} (i18n/label :enter-ens-or-contact-code)]
     (let [disable?            (or (not (string/blank? chat-error))
                                   (string/blank? new-contact-identity))
           show-error-tooltip? (and chat-error
                                    (not (string/blank? new-contact-identity)))
           create-1to1-chat #(re-frame/dispatch [:contact.ui/contact-code-submitted new-contact-identity])]
       [react/view {:style styles/add-contact-edit-view}
        [react/view {:style (styles/add-contact-input show-error-tooltip?)}
         (when show-error-tooltip?
           [error-tooltip chat-error])
         [react/text-input {:placeholder       "name.stateofus.eth"
                            :auto-focus        true
                            :flex              1
                            :selection-color   colors/blue
                            :font              :default
                            :on-change         (fn [e]
                                                 (let [native-event (.-nativeEvent e)
                                                       text         (.-text native-event)]
                                                   (re-frame/dispatch [:new-chat/set-new-identity text])))
                            :on-submit-editing (when-not disable? create-1to1-chat)}]]
        [react/touchable-highlight {:disabled disable? :on-press create-1to1-chat}
         [react/view
          {:style (styles/add-contact-button disable?)}
          [react/text
           {:style (styles/add-contact-button-text disable?)}
           (i18n/label :start-chat)]]]])
     (when (seq contacts) [react/text {:style styles/new-contact-subtitle} (i18n/label :or-choose-a-contact)])
     [react/scroll-view
      [react/view {:style styles/suggested-contacts}
       (doall
        (for [c contacts]
          ^{:key (:public-key c)}
          [react/touchable-highlight {:on-press #(do
                                                   (re-frame/dispatch [:set :contacts/new-identity (:public-key c)])
                                                   (re-frame/dispatch [:contact.ui/contact-code-submitted (:public-key c)]))}
           [react/view {:style styles/suggested-contact-view}
            [react/image {:style  styles/suggested-contact-image
                          :source {:uri (:photo-path c)}}]
            [react/text {:style styles/new-contact-subtitle} (:name c)]]]))]]]))

(views/defview new-group-chat []
  [react/view {:style styles/new-view}
   [react/view {:style styles/new-contact-title}
    [react/text {:style styles/new-contact-title-text
                 :font  :medium}
     (i18n/label :new-group-chat)]]
   [react/touchable-highlight {:on-press #(re-frame/dispatch [:contact.ui/start-group-chat-pressed])}
    [react/view
     {:style (styles/add-contact-button nil)}
     [react/text
      {:style (styles/add-contact-button-text nil)}
      (i18n/label :start-chat)]]]])

(views/defview new-public-chat []
  (views/letsubs [topic                [:get :public-group-topic]
                  topic-error          [:public-chat.new/topic-error-message]]
    [react/view {:style styles/new-view}
     [react/view {:style styles/new-contact-title}
      [react/text {:style styles/new-contact-title-text
                   :font  :medium}
       (i18n/label :new-public-group-chat)]]
     [react/text {:style styles/new-contact-subtitle} (i18n/label :public-group-topic)]
     (let [disable?            (or topic-error
                                   (string/blank? topic))
           show-error-tooltip? topic-error
           create-public-chat #(when (public-chat.db/valid-topic? topic)
                                 (re-frame/dispatch [:set :public-group-topic nil])
                                 (re-frame/dispatch [:chat.ui/start-public-chat topic {:navigation-reset? true}]))]
       [react/view {:style styles/add-contact-edit-view}
        [react/view {:flex  1
                     :style (styles/add-pub-chat-input show-error-tooltip?)}
         (when show-error-tooltip?
           [error-tooltip topic-error])

         [react/text-input {:flex            1
                            :auto-focus      true
                            :font            :default
                            :selection-color colors/blue
                            :placeholder     ""
                            :on-change       (fn [e]
                                               (let [text (.. e -nativeEvent -text)]
                                                 (re-frame/dispatch [:set :public-group-topic text])))
                            :on-submit-editing (when-not disable?
                                                 create-public-chat)}]]
        [react/touchable-highlight {:disabled disable?
                                    :on-press create-public-chat}
         [react/view {:style (styles/add-contact-button disable?)}
          [react/text {:style (styles/add-contact-button-text disable?)}
           (i18n/label :new-public-group-chat)]]]])
     [topic-input-placeholder]
     [react/text {:style styles/new-contact-subtitle} (i18n/label :selected-for-you)]
     [react/scroll-view
      [react/view {:style styles/suggested-contacts}
       (doall
        (for [topic public-chat/default-public-chats]
          ^{:key topic}
          [react/touchable-highlight {:on-press #(do
                                                   (re-frame/dispatch [:set :public-group-topic nil])
                                                   (re-frame/dispatch [:chat.ui/start-public-chat topic]))}
           [react/view {:style styles/suggested-contact-view}
            [react/view {:style styles/suggested-topic-image}
             [react/text {:style styles/suggested-topic-text} (string/capitalize (first topic))]]
            [react/text {:style styles/new-contact-subtitle} topic]]]))]]]))
