(ns status-im.ui.screens.group.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [cljs.spec.alpha :as spec]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.contacts.styles :as cstyles]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.action-button.action-button :refer [action-button action-separator]]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vi]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.text-input-with-label.view :refer [text-input-with-label]]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.utils.platform :as platform :refer [platform-specific ios?]]
            [status-im.ui.components.sticky-button :refer [sticky-button]]
            [status-im.ui.components.contact.contact :refer [contact-view]]
            [status-im.ui.screens.add-new.styles :as add-new.styles]
            [status-im.ui.screens.group.styles :as styles]
            [status-im.ui.screens.group.db :as v]
            [status-im.utils.utils :as utils]))

(defn group-toolbar [group-type edit?]
  [react/view
   [status-bar/status-bar]
   [toolbar/simple-toolbar
    (i18n/label
      (if (= group-type :contact-group)
        (if edit? :t/edit-group :t/new-group)
        (if edit? :t/chat-settings :t/group-chat)))]])

(defview group-name-view []
  (letsubs [new-group-name [:get :new-chat-name]]
    [react/view add-new.styles/input-container
     [react/text-input
      {:auto-focus     true
       :on-change-text #(re-frame/dispatch [:set :new-chat-name %])
       :default-value  new-group-name
       :placeholder    (i18n/label :t/set-a-topic)
       :style          add-new.styles/input}]]))

(defn add-btn [on-press]
  [action-button {:label     (i18n/label :t/add-members)
                  :icon      :icons/add
                  :icon-opts {:color :blue}
                  :on-press  on-press}])

(defn delete-btn [on-press]
  [react/view styles/settings-group-container
   [react/touchable-highlight {:on-press on-press}
    [react/view styles/settings-group-item
     [react/view styles/delete-icon-container
      [vi/icon :icons/close {:color :red}]]
     [react/view styles/settings-group-text-container
      [react/text {:style styles/delete-group-text}
       (i18n/label :t/delete-group)]
      [react/text {:style styles/delete-group-prompt-text}
       (i18n/label :t/delete-group-prompt)]]]]])

(defn group-chat-settings-btns []
  [react/view styles/settings-group-container
   [react/view {:opacity 0.4}
    [react/touchable-highlight {:on-press #()}
     [react/view styles/settings-group-item
      [react/view styles/settings-icon-container
       [vi/icon :icons/speaker {:color :blue}]]
      [react/view styles/settings-group-text-container
       [react/text {:style styles/settings-group-text}
        (i18n/label :t/mute-notifications)]]]]]
   [action-separator]
   [action-button {:label     (i18n/label :t/clear-history)
                   :icon      :icons/close
                   :icon-opts {:color :blue}
                   :on-press  #(re-frame/dispatch [:clear-history])}]
   [action-separator]
   [react/touchable-highlight {:on-press #(re-frame/dispatch [:leave-group-chat])}
    [react/view styles/settings-group-item
     [react/view styles/delete-icon-container
      [vi/icon :icons/arrow-right {:color :red}]]
     [react/view styles/settings-group-text-container
      [react/text {:style styles/delete-group-text}
       (i18n/label :t/leave-chat)]]]]])

(defn more-btn [contacts-limit contacts-count on-press]
  [react/view
   [common/list-separator]
   [react/view cstyles/show-all
    [react/touchable-highlight {:on-press on-press}
     [react/view
      [react/text {:style cstyles/show-all-text
                   :uppercase? (components.styles/uppercase?)
                   :font (if ios? :default :medium)}
       (str (- contacts-count contacts-limit) " " (i18n/label :t/more))]]]]])

(def ^:const contacts-limit 3)

(defview group-contacts-view [group]
  (letsubs [contacts [:all-added-group-contacts-with-limit (:group-id group) contacts-limit]
            contacts-count [:all-added-group-contacts-count (:group-id group)]]
    [react/view
     (when (pos? contacts-count)
       [common/list-separator])
     [react/view
      (doall
        (map (fn [row]
               ^{:key row}
               [react/view
                [contact-view
                 {:contact        row
                  :extend-options [{:action #(re-frame/dispatch [:remove-contact-from-group
                                                                 (:whisper-identity row)
                                                                 (:group-id group)])
                                    :label (i18n/label :t/remove-from-group)}]
                  :extended?      true}]
                (when-not (= row (last contacts))
                  [common/list-separator])])
             contacts))]
     (when (< contacts-limit contacts-count)
       [more-btn contacts-limit contacts-count #(re-frame/dispatch [:navigate-to :edit-group-contact-list])])]))

(defview edit-contact-group []
  (letsubs [{:keys [group group-type]} [:get-screen-params]]
    (let [group-name        (:name group)
          save-btn-enabled? (and (spec/valid? ::v/name group-name)
                                 (not= group-name (:name group)))]
      [react/keyboard-avoiding-view {:style styles/group-container}
       [group-toolbar group-type true]
       [group-name-view]
       [react/view styles/list-view-container
        [add-btn #(re-frame/dispatch [:navigate-to :add-contacts-toggle-list])]
        [group-contacts-view group]
        [react/view styles/separator]
        [delete-btn #(utils/show-confirmation
                       (str (i18n/label :t/delete-group) "?") (i18n/label :t/delete-group-confirmation) (i18n/label :t/delete)
                       (fn[]
                         (re-frame/dispatch [:delete-contact-group])
                         (re-frame/dispatch [:navigate-to-clean :contact-list])))]]
       (when save-btn-enabled?
         [sticky-button (i18n/label :t/save) #(re-frame/dispatch [:set-contact-group-name])])])))

(defn- render-contact [contact]
  [contact-view {:contact contact :style styles/contact}])

(defn- toolbar [group-name save-btn-enabled?]
  [toolbar/toolbar {}
   toolbar/default-nav-back
   [toolbar/content-title (i18n/label :t/group-chat)]
   (when save-btn-enabled?
     (let [handler #(re-frame/dispatch [:create-new-group-chat-and-open group-name])]
       (if platform/android?
         [toolbar/actions [{:icon      :icons/ok
                            :icon-opts {:color :blue}
                            :handler   handler}]]
         [toolbar/text-action {:handler handler}
          (i18n/label :t/create)])))])

(defview new-group []
  (letsubs [contacts   [:selected-group-contacts]
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
                         :render-fn                 render-contact
                         :bounces                   false
                         :keyboardShouldPersistTaps :always
                         :enableEmptySections       true}]]])))
