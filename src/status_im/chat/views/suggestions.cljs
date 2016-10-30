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
            [status-im.chat.constants :as c]
            [taoensso.timbre :as log]))

(defn set-command-input [command]
  (dispatch [:set-chat-command command]))

(defview request-item [{:keys [type message-id]}]
  [{:keys [color icon description] :as response} [:get-response type]]
  [touchable-highlight
   {:on-press #(dispatch [:set-response-chat-command message-id type])}
   [view st/request-container
    [view st/request-icon-container
     [view (st/request-icon-background color)
      (if icon
        [image {:source {:uri icon}
                :style  st/request-icon}])]]
    [view st/request-info-container
     [text {:style st/request-info-description} description]
     ;; todo stub
     [text {:style st/request-message-info}
      "By console, today at 14:50"]]]])

(defn render-request-row
  [{:keys [chat-id message-id] :as row} _ _]
  (list-item
    ^{:key [chat-id message-id]}
    [request-item row]))

(defn suggestion-list-item
  [[command {:keys [description]
             name  :name
             :as   suggestion}]]
  (let [label (str "!" name)]
    [touchable-highlight
     {:onPress #(set-command-input command)
      :style   st/suggestion-highlight}
     [view st/suggestion-container
      [view st/suggestion-sub-container
       [view st/command-description-container
        [text {:style st/value-text} label]
        [text {:style st/description-text} description]]
       [view st/command-label-container
        [view (st/suggestion-background suggestion)
         [text {:style st/suggestion-text} label]]]]]]))

(defn render-row [row _ _]
  (list-item [suggestion-list-item row]))

(defn title [s]
  [view st/title-container
   [text {:style st/title-text} s]])

(defview suggestions-view []
  [suggestions [:get-suggestions]
   requests [:get-requests]]
  [scroll-view {:keyboardShouldPersistTaps true}
   ;; todo translations
   (when (seq requests) [title "Requests"])
   (when (seq requests)
     [view
      [list-view {:dataSource                (to-datasource requests)
                  :keyboardShouldPersistTaps true
                  :renderRow                 render-request-row}]])
   ;; todo translations
   [title "Commands"]
   [view
    [list-view {:dataSource                (to-datasource suggestions)
                :keyboardShouldPersistTaps true
                :renderRow                 render-row}]]])

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

(defn container [h & elements]
  (let [;; todo to-response-height, cur-response-height must be specific
        ;; for each chat
        to-response-height (subscribe [:command-suggestions-height])
        input-margin       (subscribe [:input-margin])
        changed            (subscribe [:animations :commands-height-changed])
        animate?           (subscribe [:animate?])
        staged-commands    (subscribe [:get-chat-staged-commands])
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
         (into [animated-view {:style (st/container h @input-margin @staged-commands)}] elements))})))

(defview suggestion-container [any-staged-commands?]
  (let [h (anim/create-value c/input-height)]
    [container h
     [header h]
     [suggestions-view]
     [view {:height c/input-height}]]))
