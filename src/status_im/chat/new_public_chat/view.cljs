(ns status-im.chat.new-public-chat.view
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            status-im.utils.db
            [status-im.ui.components.react :as react :refer [text]]
            [status-im.ui.components.text-field.view :refer [text-field]]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.status-bar :refer [status-bar]]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.chat.new-public-chat.styles :as styles]
            [status-im.chat.new-public-chat.db :as v]
            [status-im.i18n :refer [label]]
            [cljs.spec.alpha :as spec]))

(defview new-public-chat-toolbar []
  (letsubs [topic [:get :public-group-topic]]
    (let [create-btn-enabled? (spec/valid? ::v/topic topic)]
      [react/view
       [status-bar]
       [toolbar/toolbar {}
        toolbar/default-nav-back
        [toolbar/content-title (label :t/new-public-group-chat)]
        [toolbar/actions [{:icon      :icons/ok
                           :icon-opts {:color (if create-btn-enabled? components.styles/color-blue4 components.styles/color-gray11)}
                           :handler   (when create-btn-enabled?
                                        #(dispatch [:create-new-public-chat topic]))}]]]])))

(defview chat-name-input []
  (letsubs [topic [:get :public-group-topic]]
    [react/view
     [text-field
      {:error           (cond
                          (not (spec/valid? :global/not-empty-string topic))
                          (label :t/empty-topic)

                          (not (spec/valid? ::v/topic topic))
                          (label :t/topic-format))
       :wrapper-style   styles/group-chat-name-wrapper
       :error-color     components.styles/color-blue
       :line-color      components.styles/color-gray4
       :label-hidden?   true
       :input-style     styles/group-chat-topic-input
       :auto-focus      true
       :on-change-text  #(dispatch [:set :public-group-topic %])
       :value           topic
       :validator       #(re-matches #"[a-z\-]*" %)
       :auto-capitalize :none}]
     [react/text {:style styles/topic-hash} "#"]]))

(defn new-public-chat []
  [react/view styles/group-container
   [new-public-chat-toolbar]
   [react/view styles/chat-name-container
    [react/text {:style styles/members-text
                 :font  :medium}
     (label :t/public-group-topic)]
    [chat-name-input]]])
