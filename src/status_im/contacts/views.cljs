(ns status-im.contacts.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.components.common.common :as common]
            [status-im.components.react :refer [view text icon touchable-highlight scroll-view]]
            [status-im.components.native-action-button :refer [native-action-button
                                                               native-action-button-item]]
            [status-im.components.toolbar-new.view :refer [toolbar]]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.components.drawer.view :refer [open-drawer]]
            [status-im.components.icons.custom-icons :refer [ion-icon]]
            [status-im.components.contact.contact :refer [contact-view]]
            [status-im.utils.platform :refer [platform-specific ios? android?]]
            [status-im.utils.utils :as u]
            [status-im.i18n :refer [label]]
            [status-im.contacts.styles :as st]
            [status-im.components.styles :refer [color-blue
                                                 create-icon]]))

(def ^:const contacts-limit 5)

(def toolbar-options
  [{:text (label :t/new-contact)    :value #(dispatch [:navigate-to :new-contact])}
   {:text (label :t/edit)           :value #(dispatch [:set-in [:contacts/ui-props :edit?] true])}
   {:text (label :t/new-group)      :value #(dispatch [:open-contact-toggle-list :contact-group])}
   {:text (label :t/reorder-groups) :value #(dispatch [:navigate-to :reorder-groups])}])

(defn toolbar-actions []
  [(act/search #(dispatch [:navigate-to :group-contacts nil :show-search]))
   (act/opts (if ios? toolbar-options (rest toolbar-options)))])

(defn toolbar-view []
  [toolbar {:title      (label :t/contacts)
            :nav-action (act/hamburger open-drawer)
            :actions    (toolbar-actions)}])

(defn toolbar-edit []
  [toolbar {:nav-action (act/back #(dispatch [:set-in [:contacts/ui-props :edit?] false]))
            :actions    [{:image :blank}]
            :title      (label :t/edit-contacts)}])

(defn contact-options [{:keys [unremovable?] :as contact} group]
  (let [delete-contact-opt {:value        #(u/show-confirmation
                                             (str (label :t/delete-contact) "?") (label :t/delete-contact-confirmation)
                                             (label :t/delete)
                                             (fn [] (dispatch [:hide-contact contact])))
                            :text         (label :t/delete-contact)
                            :destructive? true}
        options (if unremovable? [] [delete-contact-opt])]
    (if group
      (conj options
            {:value #(dispatch [:remove-contact-from-group
                                (:whisper-identity contact)
                                (:group-id group)])
             :text  (label :t/remove-from-group)})
      options)))

(defn contact-group-form [{:keys [contacts contacts-count group edit? click-handler]}]
  (let [subtitle (:name group)]
    [view
     (when subtitle
       [common/form-title subtitle
        {:count-value contacts-count
         :extended?   edit?
         :options     [{:value #(dispatch [:navigate-to :edit-group group :contact-group])
                        :text  (label :t/edit-group)}]}])
     [view st/contacts-list
      [common/list-footer]
      (doall
        (map (fn [contact]
               ^{:key contact}
               [view
                [contact-view
                 {:contact        contact
                  :extended?      edit?
                  :on-press       #(dispatch [:open-chat-with-contact %])
                  :extend-options (contact-options contact group)}]
                (when-not (= contact (last contacts))
                  [common/list-separator])])
             contacts))]
     (when (< contacts-limit contacts-count)
       [view
        [common/list-separator]
        [view st/show-all
         [touchable-highlight {:on-press #(do
                                            (when edit?
                                              (dispatch [:set-in [:contacts/list-ui-props :edit?] true]))
                                            (dispatch [:navigate-to :group-contacts group]))}
          [view
           [text {:style      st/show-all-text
                  :uppercase? (get-in platform-specific [:uppercase?])
                  :font       (get-in platform-specific [:component-styles :contacts :show-all-text-font])}
            (str (- contacts-count contacts-limit) " " (label :t/more))]]]]])
     [common/bottom-shadow]]))

(defview contact-group-view [{:keys [group] :as params}]
  (letsubs [contacts       [:all-added-group-contacts-with-limit (:group-id group) contacts-limit]
            contacts-count [:all-added-group-contacts-count (:group-id group)]]
    [contact-group-form (merge params {:contacts       contacts
                                       :contacts-count contacts-count})]))

(defn contacts-action-button []
  [native-action-button {:button-color color-blue
                         :offset-x     16
                         :offset-y     40
                         :hide-shadow  true
                         :spacing      13}
   [native-action-button-item
    {:title       (label :t/new-contact)
     :buttonColor :#9b59b6
     :onPress     #(dispatch [:navigate-to :new-contact])}
    [ion-icon {:name  :md-create
               :style create-icon}]]])

(defview contact-list [_]
  (letsubs [contacts       [:get-added-contacts-with-limit contacts-limit]
            contacts-count [:added-contacts-count]
            edit?          [:get-in [:contacts/ui-props :edit?]]
            groups         [:all-added-groups]
            tabs-hidden?   [:tabs-hidden?]]
    [view {:flex 1}
     [view (st/contacts-list-container tabs-hidden?)
      (if edit?
        [toolbar-edit]
        [toolbar-view])
      (if (pos? (+ (count groups) contacts-count))
        [scroll-view {:style st/contact-groups}
         (when (pos? contacts-count)
           [contact-group-form {:contacts       contacts
                                :contacts-count contacts-count
                                :edit?          edit?}])
         (for [group groups]
           ^{:key group}
           [contact-group-view {:group group
                                :edit? edit?}])]
        [view st/empty-contact-groups
         [icon :group_big st/empty-contacts-icon]
         [text {:style st/empty-contacts-text} (label :t/no-contacts)]])]
     (when (and android? (not edit?))
       [contacts-action-button])]))
