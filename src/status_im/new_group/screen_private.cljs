(ns status-im.new-group.screen-private
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.components.contact.contact :refer [contact-view]]
            [status-im.components.common.common :as common]
            [status-im.components.react :refer [view
                                                scroll-view
                                                keyboard-avoiding-view
                                                list-view
                                                list-item]]
            [status-im.components.renderers.renderers :as renderers]
            [status-im.components.sticky-button :refer [sticky-button]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.new-group.styles :as st]
            [status-im.new-group.views.group :refer [group-toolbar
                                                     group-chat-settings-btns
                                                     group-name-view
                                                     add-btn
                                                     more-btn
                                                     delete-btn]]
            [status-im.new-group.validations :as v]
            [status-im.i18n :refer [label]]
            [status-im.utils.platform :refer [ios?]]
            [cljs.spec :as s]))

(def contacts-limit 3)

(defview group-contacts-view [group]
  [contacts [:all-added-group-contacts-with-limit (:group-id group) contacts-limit]
   contacts-count [:all-added-group-contacts-count (:group-id group)]]
  [view
   (when (pos? contacts-count)
     [common/list-separator])
   [view
    (doall
      (map (fn [row]
             ^{:key row}
             [view
              [contact-view
               {:contact        row
                :extend-options [{:value #(dispatch [:remove-contact-from-group
                                                     (:whisper-identity row)
                                                     (:group-id group)])
                                  :text (label :t/remove-from-group)}]
                :extended?      true}]
              (when-not (= row (last contacts))
                [common/list-separator])])
           contacts))]
   (when (< contacts-limit contacts-count)
     [more-btn contacts-limit contacts-count #(dispatch [:navigate-to :edit-group-contact-list])])])

(defview edit-group []
  [group-name [:get :new-chat-name]
   group [:get-contact-group]
   type [:get :group-type]]
  (let [save-btn-enabled? (and (s/valid? ::v/name group-name)
                               (not= group-name (:name group)))]
    [keyboard-avoiding-view {:style st/group-container}
     [group-toolbar type true]
     [group-name-view]
     [view st/list-view-container
      [add-btn #(dispatch [:navigate-to :add-contacts-toggle-list])]
      [group-contacts-view group]
      [view st/separator]
      [delete-btn #(do
                     (dispatch [:delete-group])
                     (dispatch [:navigate-to-clean :contact-list]))]]
     (when save-btn-enabled?
       [sticky-button (label :t/save) #(dispatch [:set-group-name])])]))

(defn render-row [row _ _]
  (list-item
    ^{:key row}
    [contact-view {:contact row}]))

(defview new-group []
  [contacts [:selected-group-contacts]
   group-name [:get :new-chat-name]
   group-type [:get :group-type]]
  (let [save-btn-enabled? (and (s/valid? ::v/name group-name) (pos? (count contacts)))]
    [(if ios? keyboard-avoiding-view view) (merge {:behavior :padding}
                                                  st/group-container)
     [group-toolbar group-type false]
     [group-name-view]
     [view st/list-view-container
      [list-view {:dataSource                (to-datasource contacts)
                  :enableEmptySections       true
                  :renderRow                 render-row
                  :bounces                   false
                  :keyboardShouldPersistTaps true
                  :renderSeparator           renderers/list-separator-renderer}]]
     (when save-btn-enabled?
       [sticky-button (label :t/save)
        (if (= group-type :contact-group)
          #(dispatch [:create-new-group group-name])
          #(dispatch [:create-new-group-chat group-name]))])]))
