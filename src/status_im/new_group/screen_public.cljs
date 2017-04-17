(ns status-im.new-group.screen-public
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
            [status-im.components.styles :refer [color-blue
                                                 color-gray4]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.new-group.styles :as st]
            [status-im.new-group.validations :as v]
            [status-im.i18n :refer [label]]
            [cljs.spec :as s]))

(defview new-group-toolbar []
  [topic [:get :public-group/topic]]
  (let [create-btn-enabled? (s/valid? ::v/topic topic)]
    [view
     [status-bar]
     [toolbar
      {:title   (label :t/new-public-group-chat)
       :actions [{:image   {:source res/v                   ;; {:uri "icon_search"}
                            :style  (st/toolbar-icon create-btn-enabled?)}
                  :handler (when create-btn-enabled?
                             #(dispatch [:create-new-public-group topic]))}]}]]))

(defview group-name-input []
  [topic [:get :public-group/topic]]
  [view
   [text-field
    {:error           (cond
                        (not (s/valid? ::v/not-empty-string topic))
                        (label :t/empty-topic)

                        (not (s/valid? ::v/topic topic))
                        (label :t/topic-format))
     :wrapper-style   st/group-chat-name-wrapper
     :error-color     color-blue
     :line-color      color-gray4
     :label-hidden?   true
     :input-style     st/group-chat-topic-input
     :auto-focus      true
     :on-change-text  #(dispatch [:set :public-group/topic %])
     :value           topic
     :validator       #(re-matches #"[a-z\-]*" %)
     :auto-capitalize :none}]
   [text {:style st/topic-hash} "#"]])

(defn new-public-group []
  [view st/group-container
   [new-group-toolbar]
   [view st/chat-name-container
    [text {:style st/members-text
           :font  :medium}
     (label :t/public-group-topic)]
    [group-name-input]]])
