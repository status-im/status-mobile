(ns status-im.ui.screens.home.views.inner-item
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [clojure.string :as str]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vi]
            [status-im.ui.components.chat-icon.screen :as chat-icon-screen]
            [status-im.ui.components.context-menu :as context-menu]
            [status-im.ui.screens.home.styles :as st]
            [status-im.utils.utils :as utils]
            [status-im.commands.utils :as commands-utils]
            [status-im.i18n :as i18n]
            [status-im.utils.datetime :as time]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.constants :as const]
            [taoensso.timbre :as log]))

(defn message-content-text [{:keys [content] :as message}]
  (reagent/create-class
    {:display-name "message-content-text"
     :component-will-mount
     #(when (and (or (:command content)
                     (:content-command content))
                 (not (:short-preview content)))
        (re-frame/dispatch [:request-command-message-data message
                            {:data-type   :short-preview
                             :cache-data? true}]))
     :reagent-render
     (fn [{:keys [content] :as message}]
       [react/view st/last-message-container
        (cond

          (not message)
          [react/text {:style st/last-message-text}
           (i18n/label :t/no-messages)]

          (str/blank? content)
          [react/text {:style st/last-message-text}
           ""]

          (:content content)
          [react/text {:style           st/last-message-text
                       :number-of-lines 1}
           (:content content)]

          (and (:command content)
               (-> content :short-preview :markup))
          (commands-utils/generate-hiccup (-> content :short-preview :markup))

          :else
          [react/text {:style           st/last-message-text
                       :number-of-lines 1}
           content])])}))

(defview message-status [{:keys [chat-id contacts]}
                         {:keys [message-id message-status user-statuses message-type outgoing] :as msg}]
  (letsubs [app-db-message-status-value [:get-in [:message-data :statuses message-id :status]]]
    (let [delivery-status (get-in user-statuses [chat-id :status])]
      (when (and outgoing
                 (or (some #(= (keyword %) :seen) [delivery-status
                                                   message-status
                                                   app-db-message-status-value])
                     (and (= (keyword message-type) :group-user-message)
                          (and (= (count user-statuses) (count contacts))
                               (every? (fn [[_ {:keys [status]}]]
                                         (= (keyword status) :seen)) user-statuses)))
                     (= chat-id const/console-chat-id)))
        [react/image {:source {:uri :icon_ok_small}
                      :style  st/status-image}]))))

(defn message-timestamp [{:keys [timestamp]}]
  (when timestamp
    [react/text {:style st/datetime-text}
     (time/to-short-str timestamp)]))

(defview unviewed-indicator [chat-id]
  (letsubs [unviewed-messages-count [:unviewed-messages-count chat-id]]
    (when (pos? unviewed-messages-count)
      [react/view st/new-messages-container
       [react/text {:style st/new-messages-text
                    :font  :medium}
        unviewed-messages-count]])))

(defn options-btn [chat-id]
  (let [options [{:value        #(re-frame/dispatch [:remove-chat chat-id])
                  :text         (i18n/label :t/delete-chat)
                  :destructive? true}]]
    [react/view st/opts-btn-container
     [context-menu/context-menu
      [vi/icon :icons/options]
      options
      nil
      st/opts-btn]]))

(defn chat-list-item-name [name group-chat? public? public-key]
  (let [private-group? (and group-chat? (not public?))
        public-group?  (and group-chat? public?)
        chat-name      (if (str/blank? name)
                         (gfycat/generate-gfy public-key)
                         (utils/truncate-str name 30))]
    [react/view st/name-view
     (when public-group?
       [react/view st/public-group-icon-container
        [vi/icon :icons/public-chat {:style st/public-group-icon}]])
     (when private-group?
       [react/view st/private-group-icon-container
        [vi/icon :icons/group-chat {:style st/private-group-icon}]])
     [react/view {:flex-shrink 1}
      [react/text {:style st/name-text
                   :number-of-lines 1}
       (if public-group?
         (str "#" chat-name)
         chat-name)]]]))

(defview chat-list-item-inner-view [{:keys [chat-id name color online
                                            group-chat contacts public?
                                            public-key unremovable?] :as chat}
                                    edit?]
  (letsubs [last-message [:get-last-message chat-id]]
    (let [name (or (i18n/get-contact-translated chat-id :name name)
                   (gfycat/generate-gfy public-key))]
      [react/view st/chat-container
       [react/view st/chat-icon-container
        [chat-icon-screen/chat-icon-view-chat-list chat-id group-chat name color online]]
       [react/view st/chat-info-container
        [react/view st/item-upper-container
         [chat-list-item-name name group-chat public? public-key]
         (when (and (not edit?) last-message)
           [react/view st/message-status-container
            [message-status chat last-message]
            [message-timestamp last-message]])]
        [react/view st/item-lower-container
         [message-content-text last-message]
         (when-not edit? [unviewed-indicator chat-id])]]
       [react/view st/chat-options-container
        (when (and edit? (not unremovable?)) [options-btn chat-id])]])))
