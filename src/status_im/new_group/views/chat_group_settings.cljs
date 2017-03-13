(ns status-im.new-group.views.chat-group-settings
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [dispatch]]
    [status-im.contacts.views.contact :refer [contact-view]]
    [status-im.components.react :refer [view
                                        scroll-view
                                        icon
                                        touchable-highlight]]
    [status-im.components.confirm-button :refer [confirm-button]]
    [status-im.new-group.styles :as st]
    [status-im.new-group.views.group :refer [group-toolbar
                                             group-chat-settings-btns
                                             group-name-view
                                             add-btn
                                             more-btn
                                             delete-btn
                                             separator]]
    [status-im.new-group.validations :as v]
    [status-im.i18n :refer [label]]
    [cljs.spec :as s]))

(def contacts-limit 3)

(defn save-chat-name []
  (dispatch [:set-chat-name]))

(defview chat-group-contacts-view [admin?]
  [contacts [:current-chat-contacts]]
  (let [limited-contacts (take contacts-limit contacts)
        contacts-count (count contacts)]
    [view
     (when (and admin? (pos? contacts-count))
       [separator])
     [view
      (doall
        (map (fn [row]
               ^{:key row}
               [view
                [contact-view
                 {:contact        row
                  :on-click       #()
                  :extend-options [{:value #(do
                                              (dispatch [:set :selected-participants #{(:whisper-identity row)}])
                                              (dispatch [:remove-participants]))
                                      :text (label :t/remove)}]
                  :extended?      admin?}]
                (when-not (= row (last limited-contacts))
                  [separator])])
             limited-contacts))]
     (when (< contacts-limit contacts-count)
       [more-btn contacts-limit contacts-count #(dispatch [:navigate-to :edit-chat-group-contact-list])])]))

(defview chat-group-members []
  [current-pk [:get :current-public-key]
   group-admin [:chat :group-admin]]
  (let [admin? (= current-pk group-admin)]
    [view
     (when admin?
       [add-btn #(dispatch [:navigate-to :add-participants-toggle-list])])
     [chat-group-contacts-view admin?]]))

(defview chat-group-settings []
  [new-chat-name [:get :new-chat-name]
   chat-name [:chat :name]
   type [:get :group-type]]
  (let [save-btn-enabled? (and (s/valid? ::v/name new-chat-name)
                               (not= new-chat-name chat-name))]
    [view st/group-container
     [view {:flex 1}
      [group-toolbar type true]
      [scroll-view {:keyboardShouldPersistTaps true}
       [group-name-view]
       [chat-group-members]
       [view st/separator]
       [group-chat-settings-btns]]]
     (when save-btn-enabled?
       [confirm-button (label :t/save) save-chat-name])]))
