(ns status-im.ui.screens.group.views
  (:require-macros [status-im.utils.views :as views])
  (:require [cljs.spec.alpha :as spec]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.contact.contact :as contact]
            [status-im.ui.screens.add-new.styles :as add-new.styles]
            [status-im.ui.screens.group.styles :as styles]
            [status-im.ui.screens.group.db :as v]))

(views/defview group-name-view []
  (views/letsubs [new-group-name [:get :new-chat-name]]
    [react/view add-new.styles/input-container
     [react/text-input
      {:auto-focus          true
       :on-change-text      #(re-frame/dispatch [:set :new-chat-name %])
       :default-value       new-group-name
       :placeholder         (i18n/label :t/set-a-topic)
       :style               add-new.styles/input
       :accessibility-label :chat-name-input}]]))

(defn- render-contact [contact]
  [contact/contact-view {:contact             contact
                         :style               styles/contact
                         :accessibility-label :chat-member-item}])

(defn- toolbar [group-name save-btn-enabled?]
  [toolbar/toolbar {}
   toolbar/default-nav-back
   [toolbar/content-title (i18n/label :t/group-chat)]
   (when save-btn-enabled?
     (let [handler #(re-frame/dispatch [:create-new-group-chat-and-open group-name])]
       (if platform/android?
         [toolbar/actions [{:icon      :icons/ok
                            :icon-opts {:color               :blue
                                        :accessibility-label :create-button}
                            :handler   handler}]]
         [toolbar/text-action {:handler             handler
                               :accessibility-label :create-button}
          (i18n/label :t/create)])))])

(views/defview new-group []
  (views/letsubs [contacts   [:selected-group-contacts]
                  group-name [:get :new-chat-name]]
    (let [save-btn-enabled? (and (spec/valid? ::v/name group-name) (pos? (count contacts)))]
      [react/keyboard-avoiding-view (merge {:behavior :padding}
                                           styles/group-container)
       [status-bar/status-bar]
       [toolbar group-name save-btn-enabled?]
       [group-name-view]
       [list/list-with-label {:flex 1}
        (i18n/label :t/members-title)
        [list/flat-list {:data                      contacts
                         :key-fn                    :address
                         :render-fn                 render-contact
                         :bounces                   false
                         :keyboardShouldPersistTaps :always
                         :enableEmptySections       true}]]])))
