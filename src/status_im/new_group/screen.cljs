(ns status-im.new-group.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.resources :as res]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                icon
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.components.text-field.view :refer [text-field]]
            [status-im.components.styles :refer [color-purple]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.new-group.views.contact :refer [new-group-contact]]
            [status-im.new-group.styles :as st]
            [status-im.new-group.validations :as v]
            [status-im.i18n :refer [label]]
            [cljs.spec :as s]))

(defview new-group-toolbar []
  [new-chat-name [:get :new-chat-name]]
  (let [create-btn-enabled? (s/valid? ::v/name new-chat-name)]
    [view
     [status-bar]
     [toolbar
      {:title   (label :t/new-group-chat)
       :actions [{:image   {:source res/v                   ;; {:uri "icon_search"}
                            :style  (st/toolbar-icon create-btn-enabled?)}
                  :handler (when create-btn-enabled?
                             #(dispatch [:create-new-group new-chat-name]))}]}]]))

(defview group-name-input []
  [new-chat-name [:get :new-chat-name]]
  [view
   [text-field
    {:error          (cond
                       (not (s/valid? ::v/not-empty-string new-chat-name))
                       (label :t/empty-group-chat-name)
                       (not (s/valid? ::v/not-illegal-name new-chat-name))
                       (label :t/illegal-group-chat-name))
     :wrapper-style  st/group-chat-name-wrapper
     :error-color    "#7099e6"
     :line-color     "#0000001f"
     :label-hidden?  true
     :input-style    st/group-chat-name-input
     :auto-focus     true
     :on-change-text #(dispatch [:set :new-chat-name %])
     :value          new-chat-name}]])

(defview new-group []
  [contacts [:all-added-contacts]]
  [view st/new-group-container
   [new-group-toolbar]
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
