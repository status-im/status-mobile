(ns status-im.ui.screens.contacts.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.react :refer [view text touchable-highlight scroll-view]]
            [status-im.ui.components.icons.vector-icons :as vi]
            [status-im.ui.components.native-action-button :refer [native-action-button
                                                                  native-action-button-item]]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as act]
            [status-im.ui.components.icons.custom-icons :refer [ion-icon]]
            [status-im.ui.components.contact.contact :refer [contact-view]]
            [status-im.utils.platform :refer [platform-specific ios? android?]]
            [status-im.utils.utils :as u]
            [status-im.i18n :refer [label]]
            [status-im.ui.screens.contacts.styles :as st]
            [status-im.ui.screens.home.styles :as home.styles]
            [status-im.ui.components.styles :refer [color-blue]]))

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
  [toolbar/toolbar {}
   [toolbar/content-title (label :t/contacts)]
   [toolbar/actions
    (toolbar-actions)]])

(defn toolbar-edit []
  [toolbar/toolbar {}
   [toolbar/nav-button (act/back #(dispatch [:set-in [:contacts/ui-props :edit?] false]))]
   [toolbar/content-title (label :t/edit-contacts)]])

(defn contact-options [{:keys [unremovable?] :as contact} group]
  (let [delete-contact-opt {:action       #(u/show-confirmation
                                            (str (label :t/delete-contact) "?") (label :t/delete-contact-confirmation)
                                            (label :t/delete)
                                            (fn [] (dispatch [:hide-contact contact])))
                            :label        (label :t/delete-contact)
                            :destructive? true}
        options (if unremovable? [] [delete-contact-opt])]
    (if group
      (conj options
            {:action #(dispatch [:remove-contact-from-group
                                 (:whisper-identity contact)
                                 (:group-id group)])
             :label  (label :t/remove-from-group)})
      options)))

(defn contact-group-form [{:keys [contacts contacts-count group edit? click-handler]}]
  (let [subtitle (:name group)]
    [view
     (when subtitle
       [common/form-title subtitle
        {:count-value contacts-count
         :extended?   edit?
         :options     [{:action #(dispatch [:navigate-to :edit-contact-group group :contact-group])
                        :label  (label :t/edit-group)}]}])
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
                                            (dispatch [:navigate-to :group-contacts (:group-id group)]))}
          [view
           [text {:style      st/show-all-text
                  :uppercase? (get-in platform-specific [:uppercase?])
                  :font       (if ios? :default :medium)}
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
    {:title               (label :t/new-contact)
     :accessibility-label :new-contact
     :buttonColor         :#9b59b6
     :onPress             #(dispatch [:navigate-to :new-contact])}
    [ion-icon {:name  :md-create
               :style home.styles/create-icon}]]])

(defview contact-groups-list [_]
  (letsubs [contacts       [:get-added-contacts-with-limit contacts-limit]
            contacts-count [:added-contacts-count]
            edit?          [:get-in [:contacts/ui-props :edit?]]
            groups         [:all-added-groups]
            tabs-hidden?   [:tabs-hidden?]]
    [view {:flex 1}
     [view st/contacts-list-container
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
         [vi/icon :icons/group-big {:style st/empty-contacts-icon}]
         [text {:style st/empty-contacts-text} (label :t/no-contacts)]])]
     (when (and android? (not edit?))
       [contacts-action-button])]))
