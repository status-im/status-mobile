(ns status-im.chat.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.ui.components.react :refer [view
                                                   animated-view
                                                   text
                                                   modal
                                                   touchable-highlight
                                                   list-view
                                                   list-item]]
            [status-im.ui.components.icons.vector-icons :as vi]
            [status-im.ui.components.status-bar :refer [status-bar]]
            [status-im.ui.components.chat-icon.screen :refer [chat-icon-view-action
                                                              chat-icon-view-menu-item]]
            [status-im.chat.styles.screen :as st]
            [status-im.utils.listview :refer [to-datasource-inverted]]
            [status-im.utils.utils :refer [truncate-str]]
            [status-im.utils.datetime :as time]
            [status-im.utils.platform :as platform :refer [platform-specific]]
            [status-im.ui.components.invertible-scroll-view :refer [invertible-scroll-view]]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.chat.views.toolbar-content :refer [toolbar-content-view]]
            [status-im.chat.views.message.message :refer [chat-message]]
            [status-im.chat.views.message.datemark :refer [chat-datemark]]
            [status-im.chat.views.input.input :as input]
            [status-im.chat.views.actions :refer [actions-view]]
            [status-im.chat.views.bottom-info :refer [bottom-info-view]]
            [status-im.chat.constants :as chat-const]
            [status-im.i18n :refer [label label-pluralize]]
            [status-im.ui.components.animation :as anim]
            [status-im.ui.components.sync-state.offline :refer [offline-view]]
            [status-im.constants :refer [content-type-status]]
            [taoensso.timbre :as log]
            [clojure.string :as str]))

(defn contacts-by-identity [contacts]
  (->> contacts
       (map (fn [{:keys [identity] :as contact}]
              [identity contact]))
       (into {})))

(defn add-message-color [{:keys [from] :as message} contact-by-identity]
  (if (= "system" from)
    (assoc message :text-color :#4A5258
                   :background-color :#D3EEEF)
    (let [{:keys [text-color background-color]} (get contact-by-identity from)]
      (assoc message :text-color text-color
                     :background-color background-color))))

(defview chat-icon []
  [chat-id [:chat :chat-id]
   group-chat [:chat :group-chat]
   name [:chat :name]
   color [:chat :color]]
  ;; TODO stub data ('online' property)
  [chat-icon-view-action chat-id group-chat name color true])

(defn typing [member]
  [view st/typing-view
   [view st/typing-background
    [text {:style st/typing-text
           :font  :default}
     (str member " " (label :t/is-typing))]]])

(defn typing-all []
  [view st/typing-all
   ;; TODO stub data
   (for [member ["Geoff" "Justas"]]
     ^{:key member} [typing member])])

(defmulti message-row (fn [{{:keys [type]} :row}] type))

(defmethod message-row :datemark
  [{{:keys [value]} :row}]
  (list-item [chat-datemark value]))

(defmethod message-row :default
  [{:keys [contact-by-identity group-chat messages-count row index last-outgoing?]}]
  (let [message (-> row
                    (add-message-color contact-by-identity)
                    (assoc :group-chat group-chat)
                    (assoc :messages-count messages-count)
                    (assoc :index index)
                    (assoc :last-message (= (js/parseInt index) (dec messages-count)))
                    (assoc :last-outgoing? last-outgoing?))]
    (list-item [chat-message message])))

(defn toolbar-action []
  (let [show-actions (subscribe [:get-current-chat-ui-prop :show-actions?])]
    (fn []
      (let [show-actions @show-actions]
        [touchable-highlight
         {:on-press            #(dispatch [:set-chat-ui-props {:show-actions? (not show-actions)}])
          :accessibility-label :chat-menu}
         [view st/action
          (if show-actions
            [vi/icon :icons/dropdown-up]
            [chat-icon])]]))))

(defview add-contact-bar []
  [chat-id [:get :current-chat-id]
   pending-contact? [:current-contact :pending?]]
  (when pending-contact?
    [touchable-highlight
     {:on-press #(dispatch [:add-pending-contact chat-id])}
     [view st/add-contact
      [text {:style st/add-contact-text}
       (label :t/add-to-contacts)]]]))

(defview chat-toolbar []
  [show-actions? [:get-current-chat-ui-prop :show-actions?]
   accounts [:get-accounts]
   creating? [:get :accounts/creating-account?]]
  [view
   [status-bar]
   [toolbar/toolbar {:show-sync-bar? true}
    (when-not (or show-actions? creating?)
      (if (empty? accounts)
        [toolbar/nav-clear-text (label :t/recover) #(dispatch [:navigate-to-modal :recover-modal])]
        toolbar/default-nav-back))
    [toolbar-content-view]
    [toolbar-action]]
   [add-contact-bar]])

(defn get-intro-status-message [all-messages]
  (let [{:keys [timestamp content-type]} (last all-messages)]
    (when (not= content-type content-type-status)
      {:message-id   chat-const/intro-status-message-id
       :content-type content-type-status
       :timestamp    (or timestamp (time/now-ms))})))

(defn messages-with-timemarks [all-messages extras]
  (let [status-message (get-intro-status-message all-messages)
        all-messages   (if status-message
                         (concat all-messages [status-message])
                         all-messages)
        messages       (->> all-messages
                            (map #(merge % (get extras (:message-id %))))
                            (remove #(false? (:show? %)))
                            (sort-by :clock-value >)
                            (map #(assoc % :datemark (time/day-relative (:timestamp %))))
                            (group-by :datemark)
                            (vals)
                            (sort-by (comp :clock-value first) >)
                            (map (fn [v] [v {:type :datemark :value (:datemark (first v))}]))
                            (flatten))
        remove-last?   (some (fn [{:keys [content-type]}]
                               (= content-type content-type-status))
                             messages)]
    (if remove-last?
      (drop-last messages)
      messages)))

(defview messages-view [group-chat]
  [messages [:chat :messages]
   contacts [:chat :contacts]
   message-extras [:get :message-extras]
   loaded? [:all-messages-loaded?]
   current-chat-id [:get-current-chat-id]
   last-outgoing-message [:get-chat-last-outgoing-message @current-chat-id]]
  (let [contacts' (contacts-by-identity contacts)
        messages  (messages-with-timemarks messages message-extras)]
    [list-view {:renderRow                 (fn [row _ index]
                                             (message-row {:contact-by-identity contacts'
                                                           :group-chat          group-chat
                                                           :messages-count      (count messages)
                                                           :row                 row
                                                           :index               index
                                                           :last-outgoing?      (= (:message-id last-outgoing-message) (:message-id row))}))
                :renderScrollComponent     #(invertible-scroll-view (js->clj %))
                :onEndReached              (when-not loaded? #(dispatch [:load-more-messages]))
                :enableEmptySections       true
                :keyboardShouldPersistTaps (if platform/android? :always :handled)
                :dataSource                (to-datasource-inverted messages)}]))

(defview chat []
  [group-chat [:chat :group-chat]
   show-actions? [:get-current-chat-ui-prop :show-actions?]
   show-bottom-info? [:get-current-chat-ui-prop :show-bottom-info?]
   show-emoji? [:get-current-chat-ui-prop :show-emoji?]
   layout-height [:get :layout-height]
   input-text [:chat :input-text]]
  {:component-did-mount    #(dispatch [:check-and-open-dapp!])
   :component-will-unmount #(dispatch [:set-chat-ui-props {:show-emoji? false}])}
  [view {:style st/chat-view
         :on-layout (fn [event]
                      (let [height (.. event -nativeEvent -layout -height)]
                        (when (not= height layout-height)
                          (dispatch [:set-layout-height height]))))}
   [chat-toolbar]
   [messages-view group-chat]
   [input/container {:text-empty? (str/blank? input-text)}]
   (when show-actions?
     [actions-view])
   (when show-bottom-info?
     [bottom-info-view])
   [offline-view {:top (get-in platform-specific
                               [:component-styles :status-bar :default :height])}]])
