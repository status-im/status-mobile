(ns status-im.contacts.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.common.common :refer [separator top-shaddow bottom-shaddow]]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                icon
                                                touchable-highlight
                                                scroll-view
                                                list-view
                                                list-item] :as react]
            [status-im.components.native-action-button :refer [native-action-button
                                                               native-action-button-item]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar-new.view :refer [toolbar]]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.components.toolbar-new.styles :as tst]
            [status-im.components.drawer.view :refer [open-drawer]]
            [status-im.components.icons.custom-icons :refer [ion-icon]]
            [status-im.components.context-menu :refer [context-menu]]
            [status-im.contacts.views.contact :refer [contact-view]]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.i18n :refer [label]]
            [status-im.contacts.styles :as st]
            [status-im.components.styles :refer [color-blue
                                                 create-icon]]))

(def contacts-limit 5)

(def toolbar-options
  [{:text (label :t/new-contact)    :value #(dispatch [:navigate-to :new-contact])}
   {:text (label :t/edit)           :value #(dispatch [:set-in [:contacts-ui-props :edit?] true])}
   {:text (label :t/new-group)      :value #(dispatch [:open-contact-toggle-list :contact-group])}
   {:text (label :t/reorder-groups) :value #(dispatch [:navigate-to :reorder-groups])}])

(defn toolbar-actions []
  (let [new-contact? (get-in platform-specific [:contacts :new-contact-in-toolbar?])]
    [(act/search #(dispatch [:navigate-to :group-contacts nil :show-search]))
     (act/opts (if new-contact? toolbar-options (rest toolbar-options)))]))

(defn toolbar-view []
 [toolbar {:title          (label :t/contacts)
           :nav-action     (act/hamburger open-drawer)
           :actions        (toolbar-actions)}])

(defn toolbar-edit []
  [toolbar {:nav-action     (act/back #(dispatch [:set-in [:contacts-ui-props :edit?] false]))
            :actions        [{:image :blank}]
            :title          (label :t/edit-contacts)}])

(defn options-btn [group]
  (let [options [{:value #(dispatch [:navigate-to :edit-group group :contact-group])
                  :text (label :t/edit-group)}]]
    [view st/more-btn
     [context-menu
      [icon :options_gray]
      options]]))

(defn subtitle-view [subtitle contacts-count group extended?]
  [view (get-in platform-specific [:component-styles :contacts :group-header])
   [text {:style      (get-in platform-specific [:component-styles :contacts :subtitle])
          :font       :medium}
    subtitle]
   [text {:style      (merge st/contact-group-count
                             (get-in platform-specific [:component-styles :contacts :subtitle-count]))
          :uppercase? (get-in platform-specific [:contacts :uppercase-subtitles?])
          :font       :medium}
    (str contacts-count)]
   [view {:flex 1}]
   (when extended?
     [options-btn group])])

(defn contact-group-form [{:keys [contacts contacts-count group edit?]}]
  (let [subtitle (:name group)]
    [view st/contact-group
     (when subtitle
       [subtitle-view subtitle contacts-count group edit?])
     (when subtitle
       [top-shaddow])
     [view st/contacts-list
      [view st/contact-list-spacing]
      (doall
        (map (fn [contact]
               ^{:key contact}
               [view
                [contact-view
                 {:contact        contact
                  :extended?      edit?
                  :extend-options (when group
                                   [{:value        #(dispatch [:hide-contact contact])
                                     :text         (label :t/delete-contact)
                                     :destructive? true}
                                    {:value #(dispatch [:remove-contact-from-group
                                                        (:whisper-identity contact)
                                                        (:group-id group)])
                                     :text (label :t/remove-from-group)}])}]
                (when-not (= contact (last contacts))
                  [separator st/contact-item-separator])])
             contacts))]
     (when (< contacts-limit contacts-count)
       [view
        [separator st/contact-item-separator]
        [view st/show-all
         [touchable-highlight {:on-press #(do
                                            (when edit?
                                              (dispatch [:set-in [:contact-list-ui-props :edit?] true]))
                                            (dispatch [:navigate-to :group-contacts group]))}
          [view
           [text {:style st/show-all-text
                  :uppercase? (get-in platform-specific [:uppercase?])
                  :font (get-in platform-specific [:component-styles :contacts :show-all-text-font])}
            (str (- contacts-count contacts-limit) " " (label :t/more))]]]]])
     [bottom-shaddow]]))

(defview contact-group-view [{:keys [group] :as params}]
  [contacts [:all-added-group-contacts-with-limit (:group-id group) contacts-limit]
   contacts-count [:all-added-group-contacts-count (:group-id group)]]
  [contact-group-form (merge params {:contacts contacts
                                     :contacts-count contacts-count})])

(defn contacts-action-button []
  [native-action-button {:button-color color-blue
                         :offset-x            16
                         :offset-y            22
                         :hide-shadow         true
                         :spacing             13}
   [native-action-button-item
    {:title       (label :t/new-contact)
     :buttonColor :#9b59b6
     :onPress     #(dispatch [:navigate-to :new-contact])}
    [ion-icon {:name  :md-create
               :style create-icon}]]])

(defview contact-list [current-view?]
  [contacts             [:get-added-contacts-with-limit contacts-limit]
   contacts-count       [:added-contacts-count]
   edit?                [:get-in [:contacts-ui-props :edit?]]
   groups               [:all-added-groups]
   tabs-hidden?         [:tabs-hidden?]]
  [view {:flex 1}
   [view (st/contacts-list-container tabs-hidden?)
    (if edit?
      [toolbar-edit]
      [toolbar-view])
    (if (pos? (+ (count groups) contacts-count))
      [scroll-view {:style    st/contact-groups}
       (when (pos? contacts-count)
         [contact-group-form {:contacts       contacts
                              :contacts-count contacts-count
                              :edit?          edit?}])
       (for [group groups]
         ^{:key group}
         [contact-group-view {:group          group
                              :edit?          edit?}])]
      [view st/empty-contact-groups
       [react/icon :group_big st/empty-contacts-icon]
       [text {:style st/empty-contacts-text} (label :t/no-contacts)]])]
   (when (and (not edit?) (get-in platform-specific [:contacts :action-button?]))
       [contacts-action-button])])
