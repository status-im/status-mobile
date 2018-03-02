(ns status-im.ui.screens.group.views
  (:require-macros [status-im.utils.views :as views])
  (:require [cljs.spec.alpha :as spec]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.contact.contact :as contact]
            [status-im.ui.screens.add-new.styles :as add-new.styles]
            [status-im.ui.screens.group.styles :as styles]
            [status-im.ui.screens.group.db :as v]))

(defn group-toolbar [group-type edit?]
  [react/view
   [toolbar/simple-toolbar
    (i18n/label
      (if (= group-type :contact-group)
        (if edit? :t/edit-group :t/new-group)
        (if edit? :t/chat-settings :t/group-chat)))]])

(views/defview group-name-view []
  (views/letsubs [new-group-name [:get :new-chat-name]]
    [react/view add-new.styles/input-container
     [react/text-input
      {:auto-focus     true
       :on-change-text #(re-frame/dispatch [:set :new-chat-name %])
       :default-value  new-group-name
       :placeholder    (i18n/label :t/set-a-topic)
       :style          add-new.styles/input}]]))

(defn- render-contact [contact]
  [contact/contact-view {:contact contact :style styles/contact}])

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

(views/defview ^:theme ^:avoid-keyboard? new-group []
  (views/letsubs [contacts   [:selected-group-contacts]
                  group-name [:get :new-chat-name]]
    (let [save-btn-enabled? (and (spec/valid? ::v/name group-name) (pos? (count contacts)))]
      [react/view (merge {:behavior :padding}
                         components.styles/flex)
       [toolbar group-name save-btn-enabled?]
       [group-name-view]
       [list/list-with-label {:flex 1}
        (i18n/label :t/members-title)
        [list/flat-list {:data                      contacts
                         :render-fn                 render-contact
                         :bounces                   false
                         :keyboardShouldPersistTaps :always
                         :enableEmptySections       true}]]])))
