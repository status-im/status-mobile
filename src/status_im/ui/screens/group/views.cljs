(ns status-im.ui.screens.group.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.ui.screens.contacts.styles :as cstyles]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.action-button.action-button :refer [action-button action-separator]]
            [status-im.ui.components.react :refer [view text touchable-highlight
                                                   keyboard-avoiding-view]]
            [status-im.ui.components.icons.vector-icons :as vi]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.text-input-with-label.view :refer [text-input-with-label]]
            [status-im.ui.components.status-bar.view :refer [status-bar]]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.utils.platform :refer [platform-specific ios?]]
            [status-im.ui.components.sticky-button :refer [sticky-button]]
            [status-im.ui.components.contact.contact :refer [contact-view]]
            [status-im.ui.screens.group.styles :as styles]
            [status-im.i18n :refer [label]]
            [cljs.spec.alpha :as spec]
            [status-im.ui.screens.group.db :as v]
            [status-im.utils.utils :as utils]))

(defn group-toolbar [group-type edit?]
  [view
   [status-bar]
   [toolbar/simple-toolbar
    (label
      (if (= group-type :contact-group)
        (if edit? :t/edit-group :t/new-group)
        (if edit? :t/chat-settings :t/new-group-chat)))]])

(defview group-name-view []
  (letsubs [new-group-name [:get :new-chat-name]]
    [view styles/group-name-container
     [text-input-with-label
      {:auto-focus        true
       :label             (label :t/name)
       :on-change-text    #(dispatch [:set :new-chat-name %])
       :default-value     new-group-name}]]))

(defn add-btn [on-press]
  [action-button {:label     (label :t/add-members)
                  :icon      :icons/add
                  :icon-opts {:color :blue}
                  :on-press  on-press}])

(defn delete-btn [on-press]
  [view styles/settings-group-container
   [touchable-highlight {:on-press on-press}
    [view styles/settings-group-item
     [view styles/delete-icon-container
      [vi/icon :icons/close {:color :red}]]
     [view styles/settings-group-text-container
      [text {:style styles/delete-group-text}
       (label :t/delete-group)]
      [text {:style styles/delete-group-prompt-text}
       (label :t/delete-group-prompt)]]]]])

(defn group-chat-settings-btns []
  [view styles/settings-group-container
   [view {:opacity 0.4}
    [touchable-highlight {:on-press #()}
     [view styles/settings-group-item
      [view styles/settings-icon-container
       [vi/icon :icons/speaker {:color :blue}]]
      [view styles/settings-group-text-container
       [text {:style styles/settings-group-text}
        (label :t/mute-notifications)]]]]]
   [action-separator]
   [action-button {:label     (label :t/clear-history)
                   :icon      :icons/close
                   :icon-opts {:color :blue}
                   :on-press  #(dispatch [:clear-history])}]
   [action-separator]
   [touchable-highlight {:on-press #(dispatch [:leave-group-chat])}
    [view styles/settings-group-item
     [view styles/delete-icon-container
      [vi/icon :icons/arrow-right {:color :red}]]
     [view styles/settings-group-text-container
      [text {:style styles/delete-group-text}
       (label :t/leave-chat)]]]]])

(defn more-btn [contacts-limit contacts-count on-press]
  [view
   [common/list-separator]
   [view cstyles/show-all
    [touchable-highlight {:on-press on-press}
     [view
      [text {:style cstyles/show-all-text
             :uppercase? (get-in platform-specific [:uppercase?])
             :font (if ios? :default :medium)}
       (str (- contacts-count contacts-limit) " " (label :t/more))]]]]])

(def ^:const contacts-limit 3)

(defview group-contacts-view [group]
  (letsubs [contacts [:all-added-group-contacts-with-limit (:group-id group) contacts-limit]
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
                  :extend-options [{:action #(dispatch [:remove-contact-from-group
                                                        (:whisper-identity row)
                                                        (:group-id group)])
                                    :label (label :t/remove-from-group)}]
                  :extended?      true}]
                (when-not (= row (last contacts))
                  [common/list-separator])])
             contacts))]
     (when (< contacts-limit contacts-count)
       [more-btn contacts-limit contacts-count #(dispatch [:navigate-to :edit-group-contact-list])])]))

(defview edit-contact-group []
  (letsubs [group-name [:get :new-chat-name]
            group [:get-contact-group]
            type [:get-group-type]]
    (let [save-btn-enabled? (and (spec/valid? ::v/name group-name)
                                 (not= group-name (:name group)))]
      [keyboard-avoiding-view {:style styles/group-container}
       [group-toolbar type true]
       [group-name-view]
       [view styles/list-view-container
        [add-btn #(dispatch [:navigate-to :add-contacts-toggle-list])]
        [group-contacts-view group]
        [view styles/separator]
        [delete-btn #(utils/show-confirmation
                       (str (label :t/delete-group) "?") (label :t/delete-group-confirmation) (label :t/delete)
                       (fn[]
                         (dispatch [:delete-contact-group])
                         (dispatch [:navigate-to-clean :contact-list])))]]
       (when save-btn-enabled?
         [sticky-button (label :t/save) #(dispatch [:set-contact-group-name])])])))

(defn- render-contact [contact]
  [contact-view {:contact contact}])


(defview new-group []
  (letsubs [contacts [:selected-group-contacts]
            group-name [:get :new-chat-name]
            group-type [:get-group-type]]
    (let [save-btn-enabled? (and (spec/valid? ::v/name group-name) (pos? (count contacts)))]
      [keyboard-avoiding-view (merge {:behavior :padding}
                                     styles/group-container)
       [group-toolbar group-type false]
       [group-name-view]
       [view styles/list-view-container
        [list/flat-list {:data contacts
                         :render-fn                 render-contact
                         :bounces                   false
                         :keyboardShouldPersistTaps :always
                         :enableEmptySections       true}]]
       (when save-btn-enabled?
         [sticky-button (label :t/save)
          (if (= group-type :contact-group)
            #(do
               (dispatch [:create-new-contact-group group-name])
               (dispatch [:navigate-to-clean :contact-list]))
            #(dispatch [:create-new-group-chat-and-open group-name]))
          true])])))
