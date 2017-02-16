(ns status-im.new-group.screen-private
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.resources :as res]
            [status-im.contacts.views.contact :refer [contact-view]]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                icon
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.components.text-field.view :refer [text-field]]
            [status-im.components.confirm-button :refer [confirm-button]]
            [status-im.components.styles :refer [color-blue
                                                 separator-color]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar-with-search toolbar]]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.new-group.views.contact :refer [new-group-contact]]
            [status-im.new-group.styles :as st]
            [status-im.new-group.validations :as v]
            [status-im.i18n :refer [label]]
            [cljs.spec :as s]))

(defview new-chat-group-toolbar []
  [new-chat-name [:get :new-chat-name]]
  (let [create-btn-enabled? (s/valid? ::v/name new-chat-name)]
    [view
     [status-bar]
     [toolbar
      {:title   (label :t/new-group-chat)
       :actions [{:image   {:source res/v                   ;; {:uri "icon_search"}
                            :style  (st/toolbar-icon create-btn-enabled?)}
                  :handler (when create-btn-enabled?
                             #(dispatch [:create-new-group-chat new-chat-name]))}]}]]))

(defview group-name-input []
  [new-chat-name [:get :new-chat-name]]
  [view
   [text-field
    {:error          (when
                       (not (s/valid? ::v/not-illegal-name new-chat-name))
                       (label :t/illegal-group-chat-name))
     :wrapper-style  st/group-chat-name-wrapper
     :error-color    color-blue
     :line-color     separator-color
     :label-hidden?  true
     :input-style    st/group-chat-name-input
     :auto-focus     true
     :on-change-text #(dispatch [:set :new-chat-name %])
     :value          new-chat-name}]])

(defview new-group []
  [contacts [:all-added-contacts]]
  [view st/new-group-container
   [new-chat-group-toolbar]
   [view st/chat-name-container
    [text {:style st/members-text
           :font  :medium}
     (label :t/group-chat-name)]
    [group-name-input]
    [text {:style st/members-text
           :font  :medium}
     (label :t/members-title)]
    #_[touchable-highlight {:on-press (fn [])}
       [view st/add-container
        [icon :add_gray st/add-icon]
        [text {:style st/add-text} (label :t/add-members)]]]
    [list-view
     {:dataSource (to-datasource contacts)
      :renderRow  (fn [row _ _]
                    (list-item [new-group-contact row]))
      :style      st/contacts-list}]]])

(defview new-contacts-group-toolbar [edit?]
  [view
   [status-bar]
   [toolbar
    {:title (label (if edit? :t/edit-group :t/new-group))}]])

(defn chat-name-view [contacts-count]
  [view st/chat-name-container
   [text {:style st/group-name-text
          :font  :medium}
    (label :t/group-name)]
   [group-name-input]
   [text {:style st/members-text
          :font  :medium}
    (str (label :t/group-members) " " contacts-count)]
   [touchable-highlight {:on-press #(dispatch [:navigate-forget :contact-group-list])}
    [view st/add-container
     [icon :add_blue st/add-icon]
     [text {:style st/add-text} (label :t/add-members)]]]])

(defn delete-btn [on-press]
  [touchable-highlight {:on-press on-press}
   [view st/delete-group-container
    [text {:style st/delete-group-text} (label :t/delete-group)]
    [text {:style st/delete-group-prompt-text} (label :t/delete-group-prompt)]]])

;;TODO: should be refactored into one common function for group chats and contact groups
(defview contact-group []
  [contacts [:selected-group-contacts]
   group-name [:get :new-chat-name]
   group [:get :contact-group]]
  (let [save-btn-enabled? (and (s/valid? ::v/name group-name) (pos? (count contacts)))]
    [view st/new-group-container
     [new-contacts-group-toolbar (boolean group)]
     [chat-name-view (count contacts)]
     [list-view
      {:dataSource (to-datasource contacts)
       :renderRow  (fn [row _ _]
                     (list-item
                       ^{:key row}
                       [contact-view
                        {:contact       row
                         :extend-options [{:value #(dispatch [:deselect-contact (:whisper-identity row)])
                                           :text (label :t/remove-from-group)}]
                         :extended?     true}]))
       :style      st/contacts-list}]
     (when group
       [delete-btn #(dispatch [:update-group (assoc group :pending? true)])])
     (when save-btn-enabled?
       [confirm-button (label :t/save) (if group
                                         #(dispatch [:update-group-after-edit group group-name])
                                         #(dispatch [:create-new-group group-name]))])]))
