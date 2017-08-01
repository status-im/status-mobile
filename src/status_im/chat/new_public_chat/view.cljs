(ns status-im.chat.new-public-chat.view
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.components.react :refer [view text]]
            [status-im.components.text-field.view :refer [text-field]]
            [status-im.components.styles :as common]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.chat.new-public-chat.styles :as styles]
            [status-im.chat.new-public-chat.db :as v]
            [status-im.i18n :refer [label]]
            [cljs.spec.alpha :as spec]))

(defview new-public-chat-toolbar []
  (letsubs [topic [:get :public-group-topic]]
    (let [create-btn-enabled? (spec/valid? ::v/topic topic)]
      [view
       [status-bar]
       [toolbar
        {:title   (label :t/new-public-group-chat)
         :actions [{:image   {:source {:uri (if create-btn-enabled?
                                              :icon_ok_blue
                                              :icon_ok_disabled)}
                              :style  common/icon-ok}
                    :handler (when create-btn-enabled?
                               #(dispatch [:create-new-public-chat topic]))}]}]])))

(defview chat-name-input []
  (letsubs [topic [:get :public-group-topic]]
    [view
     [text-field
      {:error           (cond
                          (not (spec/valid? ::v/not-empty-string topic))
                          (label :t/empty-topic)

                          (not (spec/valid? ::v/topic topic))
                          (label :t/topic-format))
       :wrapper-style   styles/group-chat-name-wrapper
       :error-color     common/color-blue
       :line-color      common/color-gray4
       :label-hidden?   true
       :input-style     styles/group-chat-topic-input
       :auto-focus      true
       :on-change-text  #(dispatch [:set :public-group-topic %])
       :value           topic
       :validator       #(re-matches #"[a-z\-]*" %)
       :auto-capitalize :none}]
     [text {:style styles/topic-hash} "#"]]))

(defn new-public-chat []
  [view styles/group-container
   [new-public-chat-toolbar]
   [view styles/chat-name-container
    [text {:style styles/members-text
           :font  :medium}
     (label :t/public-group-topic)]
    [chat-name-input]]])
