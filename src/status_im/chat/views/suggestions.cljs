(ns status-im.chat.views.suggestions
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                scroll-view
                                                text
                                                icon
                                                image
                                                touchable-highlight
                                                list-view
                                                list-item
                                                animated-view]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.chat.styles.suggestions :as st]
            [status-im.chat.styles.dragdown :as ddst]
            [reagent.core :as r]
            [status-im.components.animation :as anim]
            [status-im.components.drag-drop :as drag]
            [status-im.utils.platform :refer [ios?]]
            [status-im.chat.suggestions-responder :as resp]
            [status-im.chat.constants :as chat-consts]
            [status-im.i18n :refer [label]]
            [status-im.chat.views.response :as response]))

(defn set-command-input [command]
  (dispatch [:set-chat-command command]))

(defview request-item [{:keys [type message-id]}]
  [{:keys     [color description]
    icon-path :icon
    :as       response} [:get-response type]
   {:keys [name chat-id]} [:get-current-chat]
   {:keys [added]} [:get-request message-id]]
  [touchable-highlight
   {:on-press #(dispatch [:set-response-chat-command message-id type])}
   [view st/request-container
    [view st/request-icon-container
     [view (st/request-icon-background color)
      (when icon-path
        [icon icon-path st/request-icon])]]
    [view st/request-info-container
     [text {:style st/request-info-description} description]
     (when added
       [text {:style st/request-message-info}
        (response/request-info-text name chat-id added)])]]])

(defn suggestion-list-item
  [[{:keys [bot] :as command-name}
    {:keys [title description bot]
     name  :name
     :as   command}]]
  (let [label (apply str
                     (if bot
                       [chat-consts/bot-char bot]
                       [chat-consts/command-char name]))]
    [touchable-highlight
     {:onPress #(set-command-input command)
      :style   st/suggestion-highlight}
     [view st/suggestion-container
      [view st/suggestion-sub-container
       [view st/command-description-container
        [text {:style st/value-text} title]
        [text {:style st/description-text} description]]
       [view st/command-label-container
        [view (st/suggestion-background command)
         [text {:style st/suggestion-text} label]]]]]]))

(defn title [s]
  [view st/title-container
   [text {:style st/title-text} s]])

(defview suggestions-view []
  [suggestions [:get-suggestions]
   requests [:get-requests]]
  [view {:flex 1}
   [scroll-view {:keyboardShouldPersistTaps true}
    (when (seq requests)
      [title (label :t/suggestions-requests)])
    (when (seq requests)
      (for [{:keys [chat-id message-id] :as request} requests]
        ^{:key [chat-id message-id]}
        [request-item request]))
    [title (label :t/suggestions-commands)]
    (for [suggestion (remove #(nil? (:title (second %))) suggestions)]
      ^{:key (first suggestion)}
      [suggestion-list-item suggestion])]])

(defn header [h]
  (let [layout-height (subscribe [:max-layout-height :default])
        pan-responder (resp/pan-responder h
                                          layout-height
                                          :fix-commands-suggestions-height)]
    (fn [_]
      [view
       (merge (drag/pan-handlers pan-responder)
              {:style ddst/drag-down-touchable})
       [view st/header-icon]])))

(defn container-animation-logic [{:keys [to-value val animate?]}]
  (when-let [to-value @to-value]
    (let [max-layout-height (subscribe [:max-layout-height :default])
          to-value          (min to-value (max 0 @max-layout-height))]
      (when-not (= to-value (.-_value val))
        (if (or (nil? @animate?) @animate?)
          (anim/start (anim/spring val {:toValue to-value}))
          (anim/set-value val to-value))))))

(defn container [h & _]
  (let [;; todo to-response-height, cur-response-height must be specific
        ;; for each chat
        to-response-height (subscribe [:command-suggestions-height])
        input-margin       (subscribe [:input-margin])
        changed            (subscribe [:animations :commands-height-changed])
        animate?           (subscribe [:animate?])
        context            {:to-value to-response-height
                            :val      h
                            :animate? animate?}
        on-update          #(container-animation-logic context)]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn [h & elements]
         @to-response-height @changed
         (into [animated-view {:style (st/container h @input-margin)}] elements))})))

(defview suggestion-container []
  (let [h (anim/create-value chat-consts/input-height)]
    [container h
     [header h]
     [suggestions-view]
     [view {:height chat-consts/input-height}]]))
